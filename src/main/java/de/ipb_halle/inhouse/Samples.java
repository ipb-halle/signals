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

/*
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.items.entity.ItemPositionEntity;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.container.entity.ContainerNestingEntity;
import de.ipb_halle.lbac.container.entity.ContainerNestingId;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlInsertBuilder;
*/

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class Samples {

    public record Dimension(String prefix, boolean zerobased, int rows, int colums) {
    }

    public final static String SAMPLES_INPUT = "samples.inputFile";
    public final static String SAMPLES_REJECT = "samples.rejectFile";

    public final static String CONTAINER_DEFAULT_LOCATION = "samples.defaultLocation";


    private InhouseDB inhouseDB;

    private Map<String, InhouseLocation> locations;
    private final static Map<String, Dimension> dimensions;

    private Pattern quotePattern = Pattern.compile("\"(.*)\"");
    private Pattern locationPattern = Pattern.compile("(([A-Z]{2,3})\\d{3})\\.([A-Z])(\\d)(.*)");

    /*
     * static constructor
     */
    static {
        // Intialize container dimensions.
        // The pattern given below should capture container prefixes
        // like TS, TM, TL, TH, MTP, ...
        // rows use letters, columns use digits

        dimensions = new HashMap<>();
        dimensions.put("TS", new Dimension("TS", true, 25, 10));
        dimensions.put("TM", new Dimension("TM", true, 15, 6));
        dimensions.put("TL", new Dimension("TL", true, 11, 4));
        dimensions.put("TLneu", new Dimension("TL", true, 12, 5));
        dimensions.put("TH", new Dimension("TH", true, 8, 3));
        dimensions.put("MTP", new Dimension("MTP", false, 12, 8));
    }


    public Samples(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        this.locations = new HashMap<>();
    }


    public void importData() throws Exception {
        // importSamples();
    }

    private void importExtracts(String fileName) throws Exception {
        System.out.println("Importing extract samples");

    }

    private void importSamples() throws Exception {
        System.out.println("Importing compound samples");

        // 01 SampleID                          internal id
        // 02 RefCorrProcedure_MoltableID       molProcId
        // 03 RefLastSolventUsed
        // 04 RefPermissionID                   ignored
        // 05 StorePlace
        // 06 LerbsMarker                       ignored
        // 07 Sample                            sample code
        // 08 Amount                            amount [mg]
        // 09 Tara                              tara [mg], store as remarks?
        // 10 Purity                            given as percentage
        // 11 PhysicalPhase                     store as remarks
        // 12 ClaksID                           ignored (given for 27 items)
        // 13 SampleRemarks                     store as remarks
        // 14 HPLC                              unused
        // 15 Synthesized                       unused
        // 16 Isolated                          unused
        // 17 BiolDataAvailable                 unused

        Pattern pattern = Pattern.compile("^(\\d+);"    //  1 sampleId
                + "(\\d+);"                             //  2 molProcId
                + "(.*)?;"                              //  3 last solvent
                + "(\\d?);"                             //  4 permission
                + "(.*)?;"                              //  5 storage place
                + "(.*)?;"                              //  6 "Lerbs-Marker"
                + "(.*)?;"                              //  7 sample code
                + "([0-9,\\.])?;"                       //  8 amount [mg]
                + "([0-9,\\.])?;"                       //  9 tara [mg]
                + "(\\d+)?;"                            // 10 purity
                + "(.*)?;"                              // 11 physical phase,
                + "('\\d{10}')?;"                       // 12 CLAKS-Id / KICKS-Label
                + "(.*)?;"                              // 13 remarks
                + "0;0;0;0$");                          //  - unused fields

        BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(SAMPLES_INPUT)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(SAMPLES_REJECT)));
        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();

            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                InhouseContainer container = new InhouseContainer()
                        .setSampleId(Integer.parseInt(matcher.group(1)))
                        .setCompoundCorrelationId(Integer.parseInt(matcher.group(2)))
                        .setLastSolvent(matcher.group(3))
                        .setSampleCode(matcher.group(7))
                        .setAmount(parseDecimalString(matcher.group(8)))
                        .setTara(parseDecimalString(matcher.group(9)))
                        .setPurity(Integer.parseInt(matcher.group(10)))
                        .setAppearance(matcher.group(11))
                        .setRemarks(matcher.group(13));

                parseLocation(container, matcher.group(5));
                inhouseDB.getInhouseDbService().save(container);


            } else {
                writer.append(line);
                writer.newLine();
            }
        }
        reader.close();
        writer.close();
    }

    /**
     * numbers are given in German locale (',' as decimal separator)
     *
     * @param decimal string representation of a number
     * @return the double value of the number (or 0.0)
     */
    private double parseDecimalString(String decimal) {
        if ((decimal != null) && (!decimal.isEmpty())) {
            return Double.parseDouble(decimal.replaceAll(",", "."));
        }
        return 0.0;
    }

    private void parseLocation(InhouseContainer container, String loc) {
        Matcher matcher = locationPattern.matcher(loc);
        InhouseLocation location;
        if (matcher.matches()) {
            String locationName = matcher.group(1);
            location = lookupOrCreateLocation(matcher);
            container.setLocation(locationName)
                    .setLocationId(location.getId())
                    .setRow(parseRow(matcher.group(4)))
                    .setColumn(parseColumn(location, matcher.group(5)));
        } else {
            // could not parse location, setting default location
            location = locations.get(inhouseDB.getConfigString(CONTAINER_DEFAULT_LOCATION));
            container.setLocationId(location.getId())
                    .setLocation(loc);
        }
    }

    private InhouseLocation lookupOrCreateLocation(Matcher matcher) {
        String locationName = matcher.group(1);
        InhouseLocation location = locations.get(locationName);
        if (location == null) {
            String locationType = matcher.group(2);
            Dimension dimension = dimensions.get(locationType);
            location = new InhouseLocation()
                    .setName(matcher.group(1))
                    .setRows(dimension.rows)
                    .setColumns(dimension.colums)
                    .setZeroBased(dimension.zerobased);
            location = inhouseDB.getInhouseDbService().save(location);
            locations.put(locationName, location);
        }
        return location;
    }

    private int parseRow(String row) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(row) + 1;
    }

    private int parseColumn(InhouseLocation location, String col) {
        return (location.isZeroBased() ? 1 : 0) + Integer.parseInt(col);
    }

    /**
     * return a dimensions string for a given container name
     */
    private int[] getDimension(String name) {
        String pattern = "^([A-Za-z]+).*$";
        String prefix = name.replaceAll(pattern, "$1");
        return null;
//        return this.dimensions.get(pref
    }
}
