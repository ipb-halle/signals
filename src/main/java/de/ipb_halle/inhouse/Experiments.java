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

import de.ipb_halle.inhouse.imports.ChemDrawCacheService;
import de.ipb_halle.inhouse.imports.InhouseExperimentFilter;
import de.ipb_halle.inhouse.imports.InhouseExperimentLoader;
import de.ipb_halle.inhouse.imports.InhouseImportManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles importing experiment records from the old Inhouse database into
 * the Signals platform.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Reading experiment data from a CSV-like export file of the old
 *       Inhouse database</li>
 *   <li>Parsing each line using regular expressions and mapping it to an
 *       {@link InhouseExperiment} object</li>
 *   <li>Saving successfully parsed experiments into the InhouseDB service</li>
 *   <li>Writing any lines that fail parsing into a reject file for review</li>
 *   <li>Filtering loaded experiments and importing them into Signals
 *       using appropriate strategies</li>
 * </ul>
 *
 * <p>The import process can run in two main steps:
 * <ol>
 *   <li>{@link #importExperiments()} – Reads a raw text file and stores
 *       parsed experiments in the local database</li>
 *   <li>{@link #importData()} – Loads experiments from the database, applies
 *       filtering, and imports them into Signals using
 *       {@link InhouseImportManager}</li>
 * </ol>
 *
 * <p>Import behavior for different entity types (e.g. STRUCTURE, ORGANISM)
 * is controlled by strategies stored in {@code strategyMap}.
 *
 *
 * <p>Typical usage:
 * <pre>{@code
 * InhouseDB db = new InhouseDB(config);
 * Experiments importer = new Experiments(db);
 * importer.importData();
 * }</pre>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>{@link InhouseDB} – Database configuration and services</li>
 *   <li>{@link InhouseExperimentLoader} – Loads inhouse experiments from the DB</li>
 *   <li>{@link InhouseExperimentFilter} – Applies filtering rules</li>
 *   <li>{@link ChemDrawCacheService} – Caches ChemDraw structure data (rpocId, molId, cdxml)</li>
 *   <li>{@link InhouseImportManager} – Executes the import into Signals</li>
 * </ul>
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

    /**
     * A simple record for holding ChemDraw structure data linked to a molecule.
     *
     * @param molId           the molecule ID from the Inhouse database
     * @param fieldValueCdxml the ChemDraw structure as CDXML text
     */
    public record ChemDrawData(Integer molId, String fieldValueCdxml) {
    }

    private final Map<InhouseImportType, InhouseImportStrategy> strategyMap = Map.of(InhouseImportType.STRUCTURE, new StructureImportStrategy(), InhouseImportType.ORGANISM, new ExtractImportStrategy()
            //   InhouseImportType.EXTRACT, new ExtractImportStrategy()
    );

    private InhouseDB inhouseDB;

    public Experiments(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    /**
     * Imports experiment records from a configured file into the Inhouse database.
     *
     * <p>This method:
     * <ol>
     *   <li>Opens the experiments file defined in {@link #EXPERIMENTS_FILENAME}</li>
     *   <li>Parses each line using a regular expression to extract:
     *       <ul>
     *         <li>Lab Journal name</li>
     *         <li>Three-letter code (ThreeLC)</li>
     *         <li>Individual experiment code</li>
     *         <li>Procedure remarks</li>
     *         <li>Procedure ID</li>
     *       </ul>
     *   </li>
     *   <li>Creates an {@link InhouseExperiment} object for each valid line</li>
     *   <li>Saves valid experiments into the database via {@link InhouseDB#getInhouseDbService()}</li>
     *   <li>Writes invalid or unparseable lines into the reject file defined in {@link #EXPERIMENTS_REJECTFILE}</li>
     *   <li>Logs progress every 1000 imported experiments</li>
     * </ol>
     *
     * @throws Exception if file reading/writing fails or if parsing errors occur
     */
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
        try (BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(EXPERIMENTS_FILENAME)));
             BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(EXPERIMENTS_REJECTFILE)))) {
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


    /**
     * Loads experiments from the Inhouse database, applies filtering rules,
     * and imports them into the Signals platform.
     *
     * <p>Steps:
     * <ol>
     *   <li>Uses {@link InhouseExperimentLoader} to retrieve a batch of experiments</li>
     *   <li>Initializes a {@link ChemDrawCacheService} for caching structure data (procId, molId, cdxml)</li>
     *   <li>Applies {@link InhouseExperimentFilter} to separate experiments
     *       into categories by {@link InhouseImportType}</li>
     *   <li>Logs filtering errors to an {@link ErrorLogger}</li>
     *   <li>Uses {@link InhouseImportManager#importAll(Map, ChemDrawCacheService)} to
     *       import the filtered experiments into Signals</li>
     * </ol>
     *
     * @throws Exception if loading, filtering, or importing fails
     */
    public void importData() throws Exception {
        // importExperiments();
        InhouseExperimentLoader loader = new InhouseExperimentLoader(inhouseDB);
        List<InhouseExperiment> experiments = loader.loadExperiments(500);
        logger.info("Loader size-> {}\n", experiments.size());

        ChemDrawCacheService chemDrawCache = new ChemDrawCacheService(inhouseDB);
        InhouseExperimentFilter filter = new InhouseExperimentFilter(inhouseDB);

        ErrorLogger errorLogger = new ErrorLogger("error_log_filter_experiments.txt");

        Map<InhouseImportType, List<InhouseExperiment>> filtered = filter.filter(experiments, chemDrawCache, errorLogger);

        InhouseImportManager manager = new InhouseImportManager(inhouseDB);
        manager.importAll(filtered, chemDrawCache);
    }
}
