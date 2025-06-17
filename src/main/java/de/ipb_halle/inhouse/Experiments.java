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

import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.experiments.Experiment;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import de.ipb_halle.signals.sample.StoicRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration tool for the InhouseDB
 * <p>
 * Note: the procedures table needs extensive cleaning (invalid 3 letter codes, ...)
 *
 * @author fbroda
 */
//toDo -> make all experiments to be in one upon 3LC
//toDO ->  MolId: 27883, Experiment: WSE001, Journal: 01.023 as description in sample
public class Experiments {

    public final static String EXPERIMENTS_FILENAME = "experiments.filename";
    public final static String EXPERIMENTS_REJECTFILE = "experiments.rejectfile";
    public static final String EXPERIMENTS_FIELD_THREELC = "experiments.fields.threelc";
    public static final String EXPERIMENTS_FIELD_INDIVIDUAL_CODE = "experiments.fields.individualCode";
    public static final String EXPERIMENTS_FIELD_JOURNAL = "experiments.fields.journal";
    public static final String EXPERIMENTS_FIELD_PROCEDURE_ID = "experiments.fields.procId";
    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "sample:0174e78c-0b95-49f9-8a57-39061bbc0050";
    public static final String CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION = "2";
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
        //  1) import Experiments from csv table of inhouse data base
        //   importExperiments();

        // 2) Load all procedures imported from inhouse db
        List<InhouseExperiment> experiments = loadInhouseExperiments();

        // 3) Create an empty HashMap for sorting key = threeLC, value = List<InhouseExperiment> with selected threeLC
        Map<String, List<InhouseExperiment>> threeLcGroupedMap = new HashMap<>();
        String firstKey = null; //****test**** toDo delete

        // 4) Iterate each instance in the experiments List
        for (InhouseExperiment inhouseExperiment : experiments) {
            // Get threeLc from experiment
            String key = inhouseExperiment.getThreelc();
            //logger.trace("EXPERIMENTS: -> importData() -> key (threeLc) = {}\n", key);

            // 5) Check if experiment has 3LC and jump over if it not a case
            if (key == null) {
                logger.error("This procedure doesn't have 3LC InhouseExperiment = {}\n", inhouseExperiment.toString());
                continue;
            }

            //****test**** toDo delete
            if (firstKey == null) {
                firstKey = key;
                threeLcGroupedMap.put(firstKey, new ArrayList<>());
            }

//            // 6) Check if the key (threeLc) is already added to a Map
//            if (!threeLcGroupedMap.containsKey(key)) {
//                // If not, then add the key and new ArrayList
//                threeLcGroupedMap.put(key, new ArrayList<>());
//            }
//
//            // 7) Add an Experiment to a List for given key (threeLc)
//            threeLcGroupedMap.get(key).add(inhouseExperiment);
//            //logger.trace("Experiments -> importData() added experiment = {}\n", inhouseExperiment.toString());

            //****test**** toDo delete
            if (key.equals(firstKey)) {
                threeLcGroupedMap.get(firstKey).add(inhouseExperiment);
            }

            //****test**** toDo delete
            if (!key.equals(firstKey)) {
                break;
            }
        }

        // 8) Go through the map and create an experiment for each 3LC
        for (Map.Entry<String, List<InhouseExperiment>> entry : threeLcGroupedMap.entrySet()) {
            String threeLc = entry.getKey();
            List<InhouseExperiment> listOfInhouseExperiments = entry.getValue();

            // 9) Create Signals Experiment entity upon three-letter code
            String idOfCreatedExperimentInSignals = createExperimentUpon3LC(threeLc, listOfInhouseExperiments);

            // 10) Create ChemDrawing Entity For each inhouseExperiment (procedure)
            String chemDrawId;
            int count = 0;
            for (InhouseExperiment inhouseExperiment : listOfInhouseExperiments) {
                logger.info("EXPERIMENTS-> inouseExperiment {} of {} = {}\n", ++count, threeLc, listOfInhouseExperiments.size());

                // 11) Receive a cdxml as String from compound, using experiment procedure id as a correlation key
                // from class InhouseCorrelation between classes InhouseExperiment and InhouseCompound
                Integer procedureId = inhouseExperiment.getProcId();
                Optional<ChemDrawData> optionalData = loadCDXML_StringForGivenExperimentUponMolID(procedureId);
                ChemDrawData chemDrawData;

                if (optionalData.isPresent()) {
                    chemDrawData = optionalData.get();
                    String cdxml = chemDrawData.fieldValueCdxml;
                    Integer molId = chemDrawData.molId;
                    chemDrawId = createChemDrawForInhouseExperiment(molId, idOfCreatedExperimentInSignals);

                    // 12) Make Rest Call to add a cdxml Structure as a product to reaction in chemicalDrawing entity
                    // POSITIONS-> = reactants|products|reagents|grid
                    if (!cdxml.isEmpty()) {
                        inhouseDB.getExperimentRestService().addReactionToExperiment(chemDrawId, "products", cdxml);
                    }

                    // 13) Create SampleContainer with sample from the chemical drawing. Will be added as stoicRef upon POST create entity type Sample
                    String rowId = "1";
                    String propertyValueDescription = String.format("MolId: %s, Experiment: %s%s, Journal: %s", molId, threeLc, procedureId, inhouseExperiment.getJournal());

                    createSampleForChemicalDrawing(chemDrawId, rowId, idOfCreatedExperimentInSignals, propertyValueDescription);

                } else {
                    logger.error("No data in ChemDrawData -> check loadCDXML_StringForGivenExperimentUponMolID()");
                }
            }
        }
    }


    private String createExperimentUpon3LC(String threeLc, List<InhouseExperiment> listOfInhouseExperiments) {
        // 1) Load first experiment from the List (Fields -> threeLc, individual_code, Journal, procedure_id) last three values goes eventually to sample container
        InhouseExperiment experiment = listOfInhouseExperiments.get(0);

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
        return id;
    }

    private String createChemDrawForInhouseExperiment(Integer molId, String idOfCreatedExperimentInSignals) {
        // Make a Rest Call for Creation of an Experiment Child: empty ChemDrawing Entity for further import of a Structure
        //logger.trace("CHEM_DRAW WAS CREATED AND ITS ID IS= {}\n", chemDrawId);
        return inhouseDB.getExperimentRestService()
                .createNewChemicalDrawingAsExperimentChild(
                        idOfCreatedExperimentInSignals,
                        String.format("MolId = %s", molId),
                        "");
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
     * Creates a new chemical sample in the Signals platform based on a given chemical drawing.
     * <p>
     * The method performs the following steps:
     * <ul>
     *     <li>Initializes a new {@link Sample} object with the correct chemical sample template.</li>
     *     <li>Sets the ancestor (parent) container or experiment by its Signals EID.</li>
     *     <li>Assigns the stoichiometry reference (stoicRef) linking the sample to the chemical drawing and its row.</li>
     *     <li>Sets initial sample property values such as molecule ID (molId).</li>
     *     <li>Creates the sample via the REST API and retrieves the generated sample ID.</li>
     *     <li>Updates the sample's properties using a PATCH request to the sample API endpoint.</li>
     * </ul>
     * <p>
     * This method is used when importing data from the legacy database and linking it to a chemical drawing in Signals.
     *
     * @param chemDrawId the EID of the chemical drawing to which the sample should be linked
     * @param rowId      the row index of the drawing’s stoichiometry table
     * @param ancestorId the EID of the container or experiment to act as the sample's parent
     */
    private void createSampleForChemicalDrawing(String chemDrawId, String rowId, String ancestorId, String propertyValueDescription) {
        // Create a new Sample and assign the chemical sample template
        Sample sample = new Sample();
        sample.setTemplateId(CHEMICAL_SAMPLE_TEMPLATE_ID);

        // Set the ancestor relationship (usually a sample container or experiment)
        SignalsEntity signals = new SignalsEntity();
        signals.setEid(ancestorId);
        sample.addAncestor(signals);
        sample.setAncestorId(ancestorId);

        // Create the stoichiometry reference pointing to a row in the chemical drawing
        StoicRef stoicRef = new StoicRef();
        stoicRef.setEid(chemDrawId);
        stoicRef.setRowId(rowId);
        sample.setStoicRef(stoicRef);

        // Add the molecule ID as a property to the sample
        SamplePropertyValue fvDescription = new SamplePropertyValue();
        fvDescription.setPropertyId(CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION);
        fvDescription.setPropertyValue(propertyValueDescription);

        // Create the sample via REST and store the returned Signals sample ID
        sample.addPropertyValue(fvDescription);
        String sampleId = inhouseDB.getSampleRestService().createNewSample(sample);
        sample.setId(sampleId);
        fvDescription.setSampleId(sampleId);

        // Prepare properties as key-value pairs for PATCH update
        HashMap<String, String> propertyKeyToValue = new HashMap<>();
        for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
            // id                                  // value
            propertyKeyToValue.put(samplePropertyValue.getPropertyId(), samplePropertyValue.getPropertyValue());
        }

        // Perform a PATCH request to update the properties of the newly created sample
        inhouseDB.getSampleRestService().updateSamplePropertyValues(propertyKeyToValue, sampleId);
    }

    /**
     * Loads the content of a CDXML file (chemical drawing) for a given experiment based on its molId,
     * which is determined by looking up the corresponding {@link InhouseCorrelation} entry.
     *
     * <p>If the file does not exist on disk, the method logs a warning and returns an empty string.
     * This allows the import process to continue even if no chemical structure is available for a given compound.</p>
     *
     * @return the CDXML content as a String, or an empty string if no corresponding file was found
     * @throws RuntimeException if an unexpected I/O error occurs while reading the file
     */
    private Optional<ChemDrawData> loadCDXML_StringForGivenExperimentUponMolID(Integer procedureId) {
        // 1) Load the correlation entry to resolve the molId for the given experiment
        InhouseCorrelation correlation = loadCorrelationByExperimentProcedureId(procedureId);

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
