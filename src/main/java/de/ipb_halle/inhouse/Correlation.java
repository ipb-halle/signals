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

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration tool for the InhouseDB
 *
 * @author fbroda
 */
public class Correlation {

    public final static String INPUT_MOLORG = "correlation.inputMolOrg";
    public final static String INPUT_MOLPROC = "correlation.inputMolProc";
    public final static String INPUT_ORGPROC = "correlation.inputOrgProc";
    public final static String REJECTFILE = "correlation.rejectFile";

    public final static String MOLORG = "molorg";
    public final static String ORGPROC = "orgproc";
    public final static String MOLPROC = "molproc";

    private InhouseDB inhouseDB;

    public Correlation(InhouseDB inhouseDB) throws Exception {
        this.inhouseDB = inhouseDB;
    }

    private void importCorrelation(String context, String filename) throws Exception {
        System.out.println("Importing correlation table organism / experiment");

        Pattern pattern = Pattern.compile("^(\\d+);(\\d+);(\\d+)$");

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(REJECTFILE)));

        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int correlationId = Integer.parseInt(matcher.group(1));
                InhouseCorrelation corr = new InhouseCorrelation()
                        .setContext(context);
                switch(context) {
                    case MOLORG:
                        corr.setMolId(Integer.parseInt(matcher.group(2)));
                        corr.setOrganismId(Integer.parseInt(matcher.group(3)));
                        break;
                    case MOLPROC:
                        corr.setMolId(Integer.parseInt(matcher.group(2)));
                        corr.setProcedureId(Integer.parseInt(matcher.group(3)));
                        break;
                    case ORGPROC:
                        corr.setOrganismId(Integer.parseInt(matcher.group(2)));
                        corr.setProcedureId(Integer.parseInt(matcher.group(3)));
                        break;
                }
                inhouseDB.getInhouseDbService().save(corr);
            } else {
                reject(writer, context, line);
            }
        }
        reader.close();
        writer.close();
    }

    private void reject(BufferedWriter writer, String context, String line) throws IOException {
        writer.append(String.format("%s %s\n", context, line));
    }

    public void importData() throws Exception {
        importCorrelation(MOLORG, inhouseDB.getConfigString(INPUT_MOLORG));
        importCorrelation(MOLPROC, inhouseDB.getConfigString(INPUT_MOLPROC));
        importCorrelation(ORGPROC, inhouseDB.getConfigString(INPUT_ORGPROC));
    }
}
