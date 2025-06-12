/*
 * Signals Tool
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.inhouse;

import de.ipb_halle.signals.experiments.Experiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration tool for the InhouseDB
 * <p>
 * Note: the procedures table needs extensive cleaning (invalid 3 letter codes, ...)
 *
 * @author fbroda
 */
public class Experiments {

    public final static String EXPERIMENTS_FILENAME = "experiments.filename";
    public final static String EXPERIMENTS_REJECTFILE = "experiments.rejectfile";
    public static final String EXPERIMENTS_FIELD_THREELC = "experiments.fields.threelc";
    public static final String EXPERIMENTS_FIELD_INDIVIDUAL_CODE = "experiments.fields.individualCode";
    public static final String EXPERIMENTS_FIELD_JOURNAL = "experiments.fields.journal";
    public static final String EXPERIMENTS_FIELD_PROCEDURE_ID = "experiments.fields.procId";
    private final Logger logger = LogManager.getLogger(Experiments.class);

    private InhouseDB inhouseDB;

    public Experiments(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    private void importExperiments() throws Exception {
        System.out.println("Importing experiments");
/*
        // pattern of 2014 export
        Pattern pattern = Pattern.compile("^'([A-Z]{2,3})';"    // 'RefProducerID';
                + "'([0-9]{3}[^']*)';"                          // 'IndividualCode';
                + "'(.*)';"                                     // 'LabJournal';
                + "([0-9]+);"                                   // ProcedureID;
                + "([0-9]*);"                                   // RefMol_ID;
                + ";"                                           // RefOrganismID;
                + ";"                                           // TransferDate;
                + "(\\d+\\.\\d+\\.\\d+ 00:00:00)?;"             // Date;
                + "('(.*)')?;"                                  // ProcedureRemarks;
                + ";"                                           // TLC;
                + "('(.*)')?$");                                // FileNamePublication

        // date column got disconnected in 2015 / 2015 upon refactoring of ChemFinder form
        // for ChemFinder 2015ff
        Pattern datePattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+)");
*/
        // LabJournal;RefProducerID;IndividualCode;FileNamePublication;ProcedureRemarks;ProcedureID
        Pattern pattern = Pattern.compile("^(.*);"    // 1 LabJournal
                + "(.*);"                                   // 2 RefProducerId (=ThreeLC)
                + "(.*);"                                   // 3 IndividualCode (number)
                + "(.*);"                                   // 4 FileNamePublication (never used)
                + "(.*);"                                   // 5 ProcedureRemarks
                + "(.*)$");                                 // 6 Procedure

        Pattern quotePattern = Pattern.compile("\"(.*)\"");
        BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(EXPERIMENTS_FILENAME)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(EXPERIMENTS_REJECTFILE)));
        reader.readLine(); // discard header
        int line = 1;
        while (reader.ready()) {
            String st = reader.readLine();
            line++;
            Matcher matcher = pattern.matcher(st);
            if (matcher.matches()) {
                Matcher remarkMatcher = quotePattern.matcher(matcher.group(5));
                InhouseExperiment exp = new InhouseExperiment()
                        .setJournal(matcher.group(1))
                        .setThreelc(matcher.group(2))
                        .setIndividualCode(matcher.group(3))
                        // file name never used
                        .setProcId(Integer.parseInt(matcher.group(6)));
                if (remarkMatcher.matches()) {
                    exp.setRemarks(remarkMatcher.group(1));
                } else {
                    exp.setRemarks(matcher.group(5));
                }
                inhouseDB.getInhouseDbService().save(exp);
            } else {
                writer.append(st);
                writer.newLine();
            }
            if ((line % 1000) == 0) {
                System.out.printf("imported %d experiments\n", line);
            }
        }
        reader.close();
        writer.close();
    }

    public void importData() throws Exception {
        //   importExperiments();

        List<InhouseExperiment> experiments = loadInhouseExperiments();

        for (InhouseExperiment experiment : selectSubset(experiments)) {
            importExperiment(experiment);
        }
    }

    private List<InhouseExperiment> loadInhouseExperiments() {

        List<InhouseExperiment> experiments = inhouseDB.getInhouseDbService().loadExperiments();
        logger.info("Experiments ARRAY SIZE = {}", experiments.size());

        return experiments;
    }

    private List<InhouseExperiment> selectSubset(List<InhouseExperiment> experiments) {
        return experiments.subList(2, 5);
    }

    private void importExperiment(InhouseExperiment experiment) {

        // 1) Create Inhouse Experiment DTO
        InhouseExperimentDTO experimentDTO = new InhouseExperimentDTO(experiment);
        experimentDTO.setInhouseDB(inhouseDB);

        // 2) Create a Signals Experiment JavaObject
        Experiment experimentSignals = experimentDTO.createExperiment();

        // 3) Make a Rest Call to signals API in order to create an experiment entity in signals Notebook
        // with field values for an Experiment Template InhouseExperiment (Template ID = experiment:834e6aee-0d59-4732-89d7-925edca09844)
        experimentSignals = inhouseDB.getExperimentRestService().createNewExperiment(experimentSignals);

        // 4) Set Id to Inhose Experiment
        experiment.setEid(experimentSignals.getId());

        // 5) Make a Rest Call for Creation of an Experiment Child: empty ChemDrawing Entity for further import of a Structure
        String chemDrawId = inhouseDB.getExperimentRestService()
                .createNewChemicalDrawingAsExperimentChild(
                        experimentSignals.getId(),
                        "empty_structure.cdxml",
                        "");
        //logger.trace("CHEM_DRAW WAS CREATED AND ITS ID IS= {}\n", chemDrawId);

        //ToDo next step upon stoicRef add a reaction arrow to chemDraw as well as cdxml file
        //ToDo first we need to map a Structure = InhouseCompound to Experiment through the InhouseCorrelation ProcedureID to MOL_ID

        // 6) Map a cdxml from Compound file to an experiment
        String cdxmlString = loadCDXML_StringForGivenExperimentUponMolID(experimentDTO);

        // 7) Make Rest Call to add a cdxml Structure as a product to reaction in chemicalDrawing entity
        // POSITIONS-> = reactants|products|reagents|grid
        if (!cdxmlString.isEmpty()) {
            inhouseDB.getExperimentRestService().addReactionToExperiment(chemDrawId, "products", cdxmlString);
        }
    }

    /**
     * Loads the content of a CDXML file (chemical drawing) for a given experiment based on its molId,
     * which is determined by looking up the corresponding {@link InhouseCorrelation} entry.
     *
     * <p>If the file does not exist on disk, the method logs a warning and returns an empty string.
     * This allows the import process to continue even if no chemical structure is available for a given compound.</p>
     *
     * @param experimentDTO the DTO representing the inhouse experiment; must contain a valid procedure ID
     * @return the CDXML content as a String, or an empty string if no corresponding file was found
     * @throws RuntimeException if an unexpected I/O error occurs while reading the file
     */
    private String loadCDXML_StringForGivenExperimentUponMolID(InhouseExperimentDTO experimentDTO) {
        // 1) Load the correlation entry to resolve the molId for the given experiment
        InhouseCorrelation correlation = loadCorrelationByExperimentProcedureId(experimentDTO.getProcId());

        // 2) Extract the molId from the correlation object
        Integer molId = correlation.getMolId();

        // 3) Resolve the full path to the CDXML file using the configured file path pattern and molId
        Path filePath = Path.of(String.format(
                inhouseDB.getConfigString(Compounds.COMPOUNDS_CHEMICAL_DRAWING),
                molId));

        // 4) If the file does not exist, log a warning and return an empty string
        if (!Files.exists(filePath)) {
            logger.warn("CDXML file does not exist for molId={}, skipping file: {}\n", molId, filePath);
            return "";
        }

        try {
            // 5) Read the file content as UTF-8 string
            String fieldValueCdxml = Files.readString(filePath, StandardCharsets.UTF_8);
            //logger.trace("EXPERIMENTS => fieldValueCdxml = {}", fieldValueCdxml);

            // 6) Return the CDXML content
            return fieldValueCdxml;

        } catch (IOException e) {

            // Rethrow any unexpected IO exception as a RuntimeException
            throw new RuntimeException("Error reading CDXML file: " + filePath, e);
        }
    }

    /**
     * Retrieves the {@link InhouseCorrelation} entry associated with a given procedure ID
     * by delegating the call to the inhouse database service.
     *
     * <p>This method acts as a simple wrapper that resolves the correlation between an experiment
     * and its associated molecule IDs via the procedure ID. It is used during the import
     * process to locate relevant CDXML or metadata for the experiment.</p>
     *
     * @param procId the procedure ID of the inhouse experiment
     * @return the corresponding {@link InhouseCorrelation} object
     * @throws jakarta.persistence.NoResultException if no correlation entry is found
     */
    private InhouseCorrelation loadCorrelationByExperimentProcedureId(int procId) {
        return inhouseDB.
                getInhouseDbService().
                loadCorrelationByProcedureId(procId);
    }

}
