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

import de.ipb_halle.inhouse.imports.InhouseExperimentFilter;
import de.ipb_halle.signals.experiments.Experiment;
import jakarta.persistence.NoResultException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Experiments {

    public final static String EXPERIMENTS_FILENAME = "experiments.filename";
    public final static String EXPERIMENTS_REJECTFILE = "experiments.rejectfile";
    public static final String EXPERIMENTS_FIELD_THREELC = "experiments.fields.threelc";
    public static final String EXPERIMENTS_FIELD_INDIVIDUAL_CODE = "experiments.fields.individualCode";
    public static final String EXPERIMENTS_FIELD_JOURNAL = "experiments.fields.journal";
    public static final String EXPERIMENTS_FIELD_PROCEDURE_ID = "experiments.fields.procId";
    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "sample:0174e78c-0b95-49f9-8a57-39061bbc0050";

    private final Logger logger = LogManager.getLogger(Experiments.class);

    public record ChemDrawData(Integer molId, String fieldValueCdxml) {
    }

    private final Map<InhouseImportType, InhouseImportStrategy> strategyMap = Map.of(InhouseImportType.STRUCTURE, new StructureImportStrategy(), InhouseImportType.ORGANISM, new OrganismImportStrategy()
            //   InhouseImportType.EXTRACT, new ExtractImportStrategy()
    );

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
        try (BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(EXPERIMENTS_FILENAME))); BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(EXPERIMENTS_REJECTFILE)))) {
            reader.readLine(); // discard header
            int line = 1;
            while (reader.ready()) {
                String st = reader.readLine();
                line++;
                Matcher matcher = pattern.matcher(st);
                if (matcher.matches()) {
                    Matcher remarkMatcher = quotePattern.matcher(matcher.group(5));
                    InhouseExperiment exp = new InhouseExperiment().setJournal(matcher.group(1)).setThreelc(matcher.group(2)).setIndividualCode(matcher.group(3))
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
            writer.close();
            reader.close();
        }
    }


    public void importData() throws Exception {
        List<InhouseExperiment> inhouseExperiments = loadInhouseExperiments();
        logger.info("Total experiments loaded = {}", inhouseExperiments.size());

        Map<Integer, List<Optional<ChemDrawData>>> cdxmlCache = new HashMap<>();
        Map<Integer, List<InhouseCorrelation>> correlationCache = new HashMap<>();

        InhouseExperimentFilter filter = new InhouseExperimentFilter(inhouseDB);
        ErrorLogger errorLogger = new ErrorLogger("error_log_filter_experiments.txt");
        Map<InhouseImportType, List<InhouseExperiment>> filtered = filter.filter(
                inhouseExperiments, cdxmlCache, correlationCache, errorLogger
        );

        for (InhouseImportType type : InhouseImportType.values()) {
            List<InhouseExperiment> list = filtered.getOrDefault(type, List.of());
            Map<String, List<InhouseExperiment>> grouped = groupByThreeLC(list, ImportMode.TESTING);
            importGroupedExperiments(grouped, cdxmlCache, type);
        }
    }


    private Map<InhouseImportType, List<InhouseExperiment>> filterExperiments(List<InhouseExperiment> experiments, Map<Integer, List<Optional<ChemDrawData>>> cdxmlCache, // empty hashMap
                                                                              Map<Integer, List<InhouseCorrelation>> correlationCache // empty hashMap
    ) throws Exception {

        ErrorLogger errorLogger = new ErrorLogger("error_log_filter_experiments.txt");

        int missingThreeLc = 0;
        int missingProceedId = 0;
        int missingChemDraw = 0;

        List<InhouseExperiment> structureExperiments = new ArrayList<>();
        List<InhouseExperiment> organismExperiments = new ArrayList<>();
        List<InhouseExperiment> structureExperimentsWithoutCdxml = new ArrayList<>();

        for (InhouseExperiment exp : experiments) {

            if (exp.getThreelc() == null) {
                // errorLogger.log("Missing threeLC for experiment: " + exp);
                missingThreeLc++;
                continue;
            }
            if (exp.getProcId() == 0) {
                // errorLogger.log("Missing procedure ID for experiment: " + exp);
                missingProceedId++;
                continue;
            }

            int procId = exp.getProcId();

            // Get us all experiments which have cdxml upon procId
            List<Optional<ChemDrawData>> cdxmlList = cdxmlCache.get(procId);
            if (cdxmlList == null) {
                cdxmlList = loadCDXML_StringForGivenExperimentUponMolID_Cached(procId, correlationCache);
                cdxmlCache.put(procId, cdxmlList);
            }

            // Check wherever experiment structure without cdxml or organism
            if (cdxmlList.isEmpty() || cdxmlList.stream().allMatch(Optional::isEmpty)) {
                // errorLogger.log("Missing ChemDrawData for procedureId=" + procId);
                missingChemDraw++;
                // It is possible that one procedureId has two organismId
                if (doesTheExperimentWithoutCdxmlHasOrgId(exp)) {
                    organismExperiments.add(exp);
                } else {
                    structureExperimentsWithoutCdxml.add(exp);
                }
            } else {
                structureExperiments.add(exp);
            }
        }

        errorLogger.log(String.format("Experiments with missing threeLetterCode = %d", missingThreeLc));
        errorLogger.log(String.format("Experiments with missing missingProceedId = %d", missingProceedId));
        errorLogger.log(String.format("Experiments with missing missingChemDraw = %d", missingChemDraw));

        return Map.of(InhouseImportType.STRUCTURE, structureExperiments, InhouseImportType.UNKNOWN, structureExperimentsWithoutCdxml, InhouseImportType.ORGANISM, organismExperiments);
    }

    private boolean doesTheExperimentWithoutCdxmlHasOrgId(InhouseExperiment exp) {
        List<Integer> integers = loadOrgId(exp.getProcId());
        if (integers.isEmpty()) {
            return false;
        }
        Integer orgId = integers.get(0);
        return orgId != null;
    }

    private List<Integer> loadOrgId(int procId) {
        List<InhouseCorrelation> inhouseCorrelations = inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId);
        List<Integer> list = inhouseCorrelations.stream().map(InhouseCorrelation::getOrganismId).toList();
        return list;
    }


    /**
     * Imports grouped inhouse experiments into the Signals platform.
     *
     * <p>For each 3LC group: </p>
     * <ul>
     *     <li>Splits the experiments into chunks (default: 10 items).</li>
     *     <li>Create a new Signals experiment per chunk.</li>
     *     <li>For each procedure in the chunk:
     *      <ul>
     *          <li>Retrieves ChemDraw structure (CDXML) and molId.</li>
     *          <li>Creates a chemical drawing (reaction) in Signals.</li>
     *          <li>Adds the CDXML structure as product to the reaction.</li>
     *          <li>Creates a sample for the chemical drawing.</li>
     *          <li>Updates inhouse experiment with Signals EID and import status in local DB</li>
     *      </ul>
     *     </li>
     * </ul>
     *
     * <p>Logs missing ChemDrawData and skips invalid items.</p>
     *
     * @param threeLcGroupedMap grouped valid experiments by 3LC code
     * @param cdxmlCache        cache mapping procedure ID to ChemDrawData
     */
    private void importGroupedExperiments(Map<String, List<InhouseExperiment>> threeLcGroupedMap, Map<Integer, List<Optional<ChemDrawData>>> cdxmlCache, InhouseImportType importType) {

        for (Map.Entry<String, List<InhouseExperiment>> entry : threeLcGroupedMap.entrySet()) {
            String threeLc = entry.getKey();
            List<InhouseExperiment> experimentsBy3lc = entry.getValue();

            try {
                if (!cdxmlCache.isEmpty()) {
                    logger.info("GEHE IN Structure import Strategy\n");
                    strategyMap.get(importType).importGroup(inhouseDB, threeLc, experimentsBy3lc, cdxmlCache);
                } else {
                    logger.info("GEHE IN ORGANISM import Strategy\n");
                    strategyMap.get(importType).importGroup(inhouseDB, threeLc, experimentsBy3lc);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

//            int chunkSize = 10;
//            int experimentCounter = 1;
//
//            // Split experiments into chunks of fixed size (10 per experiment)
//            for (int i = 0; i < experimentsBy3lc.size(); i += chunkSize) {
//                int toIndex = Math.min(i + chunkSize, experimentsBy3lc.size());
//                List<InhouseExperiment> chunk = experimentsBy3lc.subList(i, toIndex);
//
//                // Use first experiment in the chunk to create Signals Experiment entity
//                InhouseExperiment main = chunk.get(0);
//                String experimentName = threeLc + "-" + experimentCounter++;
//                String eid = createExperimentUpon3LC(experimentName, main);
//
//                // Loop over each inhouse experiment in chunk
//                for (InhouseExperiment exp : chunk) {
//                    Integer procId = exp.getProcId();
//                    if (procId == 0) {
//                        logger.error("No precedure Id foe exepriment  = {}\n", exp);
//                        continue;
//                    }
//
//                    List<Optional<ChemDrawData>> optionals = cdxmlCache.get(procId);
//                    if (optionals == null || optionals.isEmpty()) {
//                        errorLogger.log("Empty ChemDraw list for procId=" + procId);
//                        continue;
//                    }
//
//                    // Create a ChemDraw entry for each structure
//                    for (Optional<ChemDrawData> chemDrawDataOptional : optionals) {
//                        if (chemDrawDataOptional.isEmpty()) {
//                            errorLogger.log("Missing ChemDrawData (should not happen after filtering) for procedureId=" + procId);
//                            continue;
//                        }
//                        ChemDrawData data = chemDrawDataOptional.get();
//
//                        // Create child chemical drawing entity in Signals
//                        String chemDrawId = createChemDrawForInhouseExperiment(data.molId, eid);
//
//                        // Add chemical drawing as product
//                        // POSITIONS-> = reactants|products|reagents|grid
//                        if (!data.fieldValueCdxml.isEmpty()) {
//                            inhouseDB.getExperimentRestService().addReactionToExperiment(chemDrawId, "products", data.fieldValueCdxml);
//                        }
//
//                        // Prepare description and create sample entity
//                        String desc = String.format("MolId: %s, Experiment: %s%s, Journal: %s",
//                                data.molId, threeLc, procId, exp.getJournal());
//                        createSampleForChemicalDrawing(chemDrawId, "1", eid, desc);
//
//                        // Mark Experiment as successfully imported
//                        exp.setEid(eid);
//                        exp.setImportSuccessful(true);
//                        inhouseDB.getInhouseDbService().markAsSuccessfullyImported(exp);
//                    }
//                }
//            }
        }
    }

    private InhouseImportType determineImportType(InhouseExperiment exp) {
        boolean hasProc = exp.getProcId() != 0;

        //toDo: implement
//        if (molId != null && procId != null) return EXPERIMENT;
//        if (organismId != null && procId != null && molId == null) return ORGANISM;
//        if (organismId != null && procId != null &&  extract в tblExtract) return EXTRACT;
        return null;
    }


    /**
     * Group a list of {@link InhouseExperiment} objects by their Three Letter Code (3lC).
     * <p>
     * In TESTING mode, only the first encountered 3LC group will be returned
     * (useful for focused development and debugging).
     * In PRODUCTION mode, all valid experiments will be grouped by their 3LC.
     *
     * @param validExperiments the list of experiments to group (must be non-null)
     * @param mode             the import mode (TESTING or PRODUCTION)
     * @return a map where each key is a 3LC and the value is a list of experiments with that code
     */
    private Map<String, List<InhouseExperiment>> groupByThreeLC(List<InhouseExperiment> validExperiments, ImportMode mode) throws Exception {
        ErrorLogger errorLogger = new ErrorLogger("import_error.log"); // toDo set filepath

        Map<String, List<InhouseExperiment>> threeLcGroupedMap = new HashMap<>();
        String firstKey = null; // used in TESTING mode only

        for (InhouseExperiment inhouseExperiment : validExperiments) {
            String key = inhouseExperiment.getThreelc();

            // Skip experiments without 3LC, log them for debugging
            if (key == null) {
                errorLogger.log("Missing 3LC: " + inhouseExperiment.toString());
                logger.error("This procedure doesn't have 3LC InhouseExperiment = {}\n", inhouseExperiment.toString());
                continue;
            }

            switch (mode) {
                case TESTING:
                    // Keep only experiments with the first encountered 3LC
                    if (firstKey == null) {
                        firstKey = key;
                        threeLcGroupedMap.put(firstKey, new ArrayList<>());
                    }

                    if (key.equals(firstKey)) {
                        threeLcGroupedMap.get(firstKey).add(inhouseExperiment);
                    } else {
                        // Stop as soon as a different 3LC is encountered
                        return threeLcGroupedMap;
                    }
                    break;

                case PRODUCTION:
                    // Production mode: Full grouping: collect all experiments by their 3LC
                    threeLcGroupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(inhouseExperiment);
                    break;
            }
        }
        return threeLcGroupedMap;
    }


    public String createExperimentUpon3LC(String experimentName, InhouseExperiment experiment) {
        // 1) Create Inhouse Experiment DTO
        InhouseExperimentDTO dto = new InhouseExperimentDTO(experiment);
        dto.setInhouseDB(inhouseDB);
        // 2) Create a Signals Experiment JavaObject
        Experiment experimentSignals = dto.createExperiment(experimentName);
        // 3) Make a Rest Call to signals API in order to create an experiment entity in signals Notebook
        // with field values for an Experiment Template InhouseExperiment (Template ID = experiment:834e6aee-0d59-4732-89d7-925edca09844)
        experimentSignals = inhouseDB.getExperimentRestService().createNewExperiment(experimentSignals);
        // 4) Set Id to Inhose Experiment
        String id = experimentSignals.getId();
        experiment.setEid(id);
        return experiment.getEid();
    }

    private List<InhouseExperiment> loadInhouseExperiments() {
        List<InhouseExperiment> experiments = inhouseDB.getInhouseDbService().loadExperiments();
        logger.info("Experiments ARRAY SIZE = {}", experiments.size());
        return experiments;
    }

    public String createChemDrawForInhouseExperiment(Integer molId, String idOfCreatedExperimentInSignals) {
        // Make a Rest Call for Creation of an Experiment Child: empty ChemDrawing Entity for further import of a Structure
        //logger.trace("CHEM_DRAW WAS CREATED AND ITS ID IS= {}\n", chemDrawId);
        String fileName = "molId_" + molId;
        return inhouseDB.getExperimentRestService().createNewChemicalDrawingAsExperimentChild(idOfCreatedExperimentInSignals, fileName, "");
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
    public List<Optional<ChemDrawData>> loadCDXML_StringForGivenExperimentUponMolID_Cached(Integer procedureId, Map<Integer, List<InhouseCorrelation>> correlationCache) {

        List<InhouseCorrelation> correlations = correlationCache.computeIfAbsent(procedureId, this::loadCorrelationByExperimentProcedureId);

        List<Optional<ChemDrawData>> list = new ArrayList<>();
        if (correlations == null) return list;

        for (InhouseCorrelation correlation : correlations) {

            // 2) Extract the molId from the correlation object
            if (correlation == null || correlation.getMolId() == null) {
                list.add(Optional.empty());
                continue;
            }
            Integer molId = correlation.getMolId();
            // 3) Resolve the full path to the CDXML file using the configured file path pattern and molId
            Path filePath = Path.of(String.format(inhouseDB.getConfigString(Compounds.COMPOUNDS_CHEMICAL_DRAWING), molId));
            // 4) If the file does not exist, log a warning and return an empty string
            if (!Files.exists(filePath)) {
                try (ErrorLogger errorLogger = new ErrorLogger("import_error_cdxml.log")) {
                    errorLogger.log(String.format("CDXML file does not exist for molId=%s, skipping file: %s\n", molId, filePath));
                    // toDo -> just implement here the addition of alternative cached List for organisms with org_id and proc_id
                    list.add(Optional.empty());
                    continue;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                // 5) Read the file content as UTF-8 string
                //String fieldValueCdxml = Files.readString(filePath, StandardCharsets.UTF_8);
                byte[] bytes = Files.readAllBytes(filePath);
                String fieldValueCdxml = new String(bytes, StandardCharsets.ISO_8859_1);

                // 6) Return the CDXML content
                list.add(Optional.of(new ChemDrawData(molId, fieldValueCdxml)));
            } catch (IOException e) {
                // Rethrow any unexpected IO exception as a RuntimeException
                throw new RuntimeException("Error reading CDXML file: " + filePath, e);
            }
        }
        return list;
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
    private List<InhouseCorrelation> loadCorrelationByExperimentProcedureId(int procId) {
        if (procId == 0) return null;
        try {

            return inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId);
        } catch (NoResultException e) {
            return null;
        }
    }


    public Logger getLogger() {
        return logger;
    }
}
