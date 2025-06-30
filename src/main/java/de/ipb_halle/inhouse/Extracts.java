/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.inhouse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extracts {
    public static final String EXTRACTS_INPUT_FILE = "extracts.inputFile";
    public static final String EXTRACTS_REJECT_FILE = "extracts.rejectFile";
    private final Logger logger = LogManager.getLogger(Extracts.class);
    private final InhouseDB inhouseDB;

    public Extracts(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    public void importExtracts() throws Exception {
        logger.info("Importing extracts");

        Pattern pattern = Pattern.compile("^" +
                "(\\d+);" +             // group (1)	ExtractID	-> 5
                "(\\d+);" +             // group (2)	RefCorrOrganisms_ProcedureID  (correlationId)->	8
                "([^;]*);" +            // group (3)    RefLastSolventUsed	(lastSolvent)->      hexane
                "([^;]*);" +            // group (4)	StorePlace	(storagePlace)-> TM018.A1
                "([^;]*);" +            // group (5)	Extract	(extract code)-> 1H
                "(\\d+);" +             // group (6)    Tara	-> 0
                "([\\d,]*);" +          // group (7)    Amount -> 72,6
                "([^;]*);" +            // group (8)    Volume -> empty
                "([^;]*);" +            // group (9)    Concentration ->
                "([^;]*);" +            // group (10)   Solution -> N
                "([^;]*);" +            // group (11)   ExtractRemarks -> empty
                "([^;]*);" +            // group (12)   HPLC -> empty
                "([^;]*);" +            // group (13)   ExtractPlateId -> empty
                "([^;]*);" +            // group (14)   ExtractPosition -> empty
                "([^;]*);" +            // group (15)   ExtractBarcode -> empty
                "([^;]*)" +             // group (16)   IPBCode -> TM018.A1
                "$");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(EXTRACTS_INPUT_FILE)));
                BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(EXTRACTS_REJECT_FILE)))
        ) {
            reader.readLine(); // skip header

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = pattern.matcher(line);

                if (m.matches()) {
                    InhouseExtract extracts = new InhouseExtract()
                            .setExtractId(Integer.parseInt(m.group(1)))
                            .setCorrelationId(m.group(2))
                            .setLastSolvent(m.group(3))
                            .setStoragePlace(m.group(4))
                            .setExtractCode(m.group(5))
                            .setTara(m.group(6))
                            .setAmount(parseDouble(m.group(7)))
                            .setVolume(parseDouble(m.group(8)))
                            .setConcentration(parseDouble(m.group(9)))
                            .setSolution(!m.group(10).equals("N") && !m.group(10).isEmpty())
                            .setRemarks(m.group(11).isEmpty() ? "no remarks" : m.group(11))
                            .setHplc(m.group(12).isEmpty() ? "no record" : m.group(12))
                            .setExtractPlateId(m.group(13).isEmpty() ? "no record" : m.group(13))
                            .setExtractPosition(m.group(14).isEmpty() ? "no record" : m.group(14))
                            .setExtractBarcode(m.group(15).isEmpty() ? "no record" : m.group(15))
                            .setIpbCode(m.group(16).isEmpty() ? "no record" : m.group(16));

                    inhouseDB.getInhouseDbService().save(extracts);
                }else {
                    writer.write(line);
                    writer.newLine();
                }
            }

            writer.close();
            logger.info("Extracts import finished");
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
