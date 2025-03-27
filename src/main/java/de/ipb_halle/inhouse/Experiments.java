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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
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

    private void importExperiments(InhouseDB inhouseDB) throws Exception {
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
                        .setCode(matcher.group(3))
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

    public void importData(InhouseDB inhouseDB) throws Exception {
        importExperiments(inhouseDB);
    }

}
