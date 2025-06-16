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

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.experiments.Experiment;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import de.ipb_halle.signals.sample.StoicRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "sample:0174e78c-0b95-49f9-8a57-39061bbc0050";
    private final Logger logger = LogManager.getLogger(Experiments.class);

    private record ChemDrawData(Integer molId, String fieldValueCdxml) {
    }

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

    /**
     * Imports an {@link InhouseExperiment} into the Signals platform by creating the corresponding
     * experiment entity, assigning template-based field values, and attaching a chemical drawing
     * as a child structure, including the ability to add a chemical structure to a reaction.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Wraps the given {@code InhouseExperiment} into a DTO for data preparation and mapping.</li>
     *   <li>Creates a new {@link Experiment} Java object using a predefined experiment template
     *       (e.g., "InhouseExperiment" template).</li>
     *   <li>Sends a REST request to the Signals API to create this experiment in the Signals Notebook backend.</li>
     *   <li>Sets the returned experiment ID to the original inhouse experiment object for reference.</li>
     *   <li>Creates an empty {@code chemicalDrawing} child entity for the experiment to hold the structure later.</li>
     *   <li>Loads a CDXML structure file based on the experiment's associated molId via the correlation table.</li>
     *   <li>If the CDXML is found and non-empty, it is appended to the experiment’s chemicalDrawing as a
     *       reaction product via an API POST call to Signals.</li>
     * </ol>
     *
     * <p><strong>Note:</strong> This method assumes a working mapping between
     * {@code procedureId ↔ molId} via {@code InhouseCorrelation}, and the existence of a
     * CDXML file on disk named accordingly (e.g., {@code mol_21248.cdxml}).
     *
     * @param experiment the {@link InhouseExperiment} instance to be imported into Signals
     * @throws RuntimeException if any I/O or REST communication error occurs
     */
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
        String id = experimentSignals.getId();
        logger.info("EXPERIMENTS ======*************==id = {}\n", id);
        experiment.setEid(id);

        // 5) Make a Rest Call for Creation of an Experiment Child: empty ChemDrawing Entity for further import of a Structure
        String chemDrawId = inhouseDB.getExperimentRestService()
                .createNewChemicalDrawingAsExperimentChild(
                        experimentSignals.getId(),
                        String.format("Procedure with id = %s", experimentDTO.getProcId()),
                        "");
        //logger.trace("CHEM_DRAW WAS CREATED AND ITS ID IS= {}\n", chemDrawId);

        // 6) Map a cdxml from Compound file to an experiment
        Optional<ChemDrawData> optionalData = loadCDXML_StringForGivenExperimentUponMolID(experimentDTO);

        if (optionalData.isPresent()) {
            ChemDrawData chemDrawData = optionalData.get();

            String cdxmlString = chemDrawData.fieldValueCdxml;
            // 7) Make Rest Call to add a cdxml Structure as a product to reaction in chemicalDrawing entity
            // POSITIONS-> = reactants|products|reagents|grid
            if (!cdxmlString.isEmpty()) {
                inhouseDB.getExperimentRestService().addReactionToExperiment(chemDrawId, "products", cdxmlString);
            }

            // 8) Create SampleContainer with sample from the chemical drawing. Will be added as stoicRef upon POST create entity type Sample
            String rowId = "1";
            createSampleForChemicalDrawing(chemDrawId, rowId, id, chemDrawData.molId);

        } else {
            logger.error("No data in ChemDrawData -> check loadCDXML_StringForGivenExperimentUponMolID()");
        }
    }

    private void createSampleForChemicalDrawing(String chemDrawId, String rowId, String ancestorId, Integer molId) {
        Sample sample = new Sample();
        sample.setTemplateId(CHEMICAL_SAMPLE_TEMPLATE_ID);

        SignalsEntity signals = new SignalsEntity();
        signals.setEid(ancestorId);
        sample.addAncestor(signals);
        sample.setAncestorId(ancestorId);


        StoicRef stoicRef = new StoicRef();
        stoicRef.setEid(chemDrawId);
        stoicRef.setRowId(rowId);
        sample.setStoicRef(stoicRef);

        SamplePropertyValue fvMolId = new SamplePropertyValue();
        fvMolId.setPropertyId("110");
        fvMolId.setPropertyValue(String.valueOf(molId));

        sample.addPropertyValue(fvMolId);
        inhouseDB.getSampleRestService().createNewSample(sample);
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
    private Optional<ChemDrawData> loadCDXML_StringForGivenExperimentUponMolID(InhouseExperimentDTO experimentDTO) {
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
            return Optional.empty();
        }

        try {
            // 5) Read the file content as UTF-8 string
            String fieldValueCdxml = Files.readString(filePath, StandardCharsets.UTF_8);
            //logger.trace("EXPERIMENTS => fieldValueCdxml = {}", fieldValueCdxml);

            // 6) Return the CDXML content
            return Optional.of(new ChemDrawData(molId, fieldValueCdxml));

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
