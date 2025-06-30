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

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Organisms {
    public static final String ORGANISMS_FILENAME = "organisms.fileName";
    public static final String ORGANISMS_REJECT_FILE = "organisms.rejectFile";
    private final Logger logger = LogManager.getLogger(Organisms.class);
    private final InhouseDB inhouseDB;

    public Organisms(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    public void importOrganisms() throws IOException {
        logger.trace("Importing organisms");

        Pattern pattern = Pattern.compile(
                "^"              // Start of the line
                        + "(\\d+);"      // Group 1: one or more digits followed by a semicolon (e.g., "123;")
                        + "([^;]*);"     // Group 2: any characters except semicolon (0 or more), then semicolon (e.g., "abc;")
                        + "([^;]*);"     // Group 3: again, any characters except semicolon (0 or more), then semicolon (e.g., "xyz;")
                        + "\"?(.*?)\"?"  // Group 4: optional starting quote, then any characters (non-greedy), then optional closing quote
                        + "$"            // End of the line
        );

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(ORGANISMS_FILENAME)));
                BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(ORGANISMS_REJECT_FILE)))
        ) {

            reader.readLine(); // skip header

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    InhouseOrganism organism = new InhouseOrganism()
                            .setOrgId(Integer.parseInt(m.group(1)))
                            .setSpeciesScriptId(m.group(2))
                            .setStrainScriptId(m.group(3))
                            .setRemarks(m.group(4));
                    inhouseDB.getInhouseDbService().save(organism);
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }

            writer.close();
            logger.info("Organisms import finished");
        }
    }
}