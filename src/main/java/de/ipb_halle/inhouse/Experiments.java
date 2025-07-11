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

import de.ipb_halle.inhouse.imports.*;
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
        var loader = new InhouseExperimentLoader(inhouseDB);
        var experiments = loader.loadExperiments();

        var chemDrawCache = new ChemDrawCacheService(inhouseDB);
        var filter = new InhouseExperimentFilter(inhouseDB);
        var errorLogger = new ErrorLogger("error_log_filter_experiments.txt");

        var filtered = filter.filter(experiments, chemDrawCache, errorLogger);

        var manager = new InhouseImportManager(inhouseDB);
        manager.importAll(filtered, chemDrawCache);
    }
}
