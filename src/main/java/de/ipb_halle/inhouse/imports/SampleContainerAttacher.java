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

package de.ipb_halle.inhouse.imports;

import de.ipb_halle.inhouse.InhouseContainer;
import de.ipb_halle.inhouse.InhouseCorrelation;
import de.ipb_halle.inhouse.InhouseDB;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.inventory.*;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.users.UserReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleContainerAttacher {
    public static final String LOCATIONS_CSV_REJECTFILE = "locations.reject";
    public static final String LOCATIONS_TS = "locations.ts";
    public static final String LOCATIONS_TM = "locations.tm";
    public static final String LOCATIONS_TL = "locations.tl";
    public static final String LOCATIONS_TH = "locations.th";

    public static final String CONTAINER_TYPE_ID_VIAL = "container:b9f1ac14-3a84-4812-98b3-6543abc68d62:ivt";
    public static final String LOCATION_TYPE_ID_TRAY = "location:e4d8f923-d407-4bee-8121-857a86f9e59d:ivt";

    private final InhouseDB inhouseDb;

    enum FillLevel {RESTPLAETZE, VOLL, EMPTY}

    private record TraysCsvEntry(
            String key,             // e.g. "TH001"
            String storageRoom,     // e.g. "R003.K.8"
            Integer columns,        // e.g. 3
            Integer rows,           // e.g 8
            FillLevel fillLevel
    ) {
    }

    private static final Logger logger = LogManager.getLogger(SampleContainerAttacher.class);


    public SampleContainerAttacher(InhouseDB inhouseDb) {
        this.inhouseDb = inhouseDb;
    }

    /**
     * Adding container to sample
     *
     * @param sample Signals Sample ()
     * @param procId procedure_id from Inhouse
     */
    public void attachSampleContainer(Sample sample, String eid, Integer procId) throws Exception {
        logger.info("SAMPLE CONTAINER ATTACHER:=> cheking sample eid = {} \n", eid);

        // 1) Load the correlation by procedure id for structure (aim context ="molproc")
        List<InhouseCorrelation> corr = inhouseDb.getInhouseDbService().loadCorrelationByProcedureId(procId);
        if (corr == null || corr.isEmpty()) {
            logger.error("The Inhouse Correlation is empty!");
            return;
        }

        for (InhouseCorrelation c : corr) {
            // 2) filter for "molproc" correlation
            if (c.getContext().equalsIgnoreCase("molproc")) {
                // get the "molproc" corr Id
                Integer molProcId = c.getCorrId();

                // 3) load with the help of "molproc" ID the InhouseContainers (Samples from InhouseDB)
                List<InhouseContainer> containers = inhouseDb.getInhouseDbService().loadInhouseContainerByMolProcId(molProcId);
                logger.info("Containers: {}\n", Arrays.toString(containers.toArray()));

                // check if it is empty
                if (containers == null || containers.isEmpty()) {
                    logger.info("Inhouse containers are empty for procId = {}\n", procId);
                }

                Map<String, List<TraysCsvEntry>> mapOfStoragePlaceToStorageRoom = loadLocationsFromCSVForTray();

                // if not
                int counter = 1;
                for (InhouseContainer ic : containers) {
                    // Location
                    Location location;

                    // find the storage Place
                    String storagePlace = ic.getLocation();

                    // Extracting Tray Prefix TS, TM, TL, TH (small, middle, large, huge)
                    String trayPrefix = extractTrayPrefix(storagePlace);

                    List<TraysCsvEntry> traysCsvEntries = mapOfStoragePlaceToStorageRoom.get(trayPrefix);
                    for (TraysCsvEntry entr : traysCsvEntries) {
                        // e.g. key ="TH001"
                        if (entr.key.equalsIgnoreCase(storagePlace)) {

                            // todo: this is a case for production
                            // e.g R003.K8 + TH001
                            //location = inhouseDb.getLocationDbService().loadLocationByName(entry.storageRoom + storagePlace);

                            // toDo: this is a case for Test
                            // =========== begin of the test code ==============
                            location = new Location();
                            location.setLocationTypeId(LOCATION_TYPE_ID_TRAY);
                            location.setName(String.format("TS00%d", counter++));

                            LocationType locationType = new LocationType();
                            locationType.setId(LOCATION_TYPE_ID_TRAY);
                            locationType.setId("Vial" + counter);

                            // Rest call to create a location
                            String locationEid = inhouseDb.getLocationRestService().doCreateLocation(locationType, location);
                            // =========== end of the test code ==============

                            Container container = new Container();
                            container.setName(sample.getName());
                            // here the sample eid will be set
                            container.setMaterial(new MaterialReference().setId(eid));
                            // here location eid will be set
                            container.setLocation(new LocationReference().setId(locationEid));
                            container.setContainerTypeId(CONTAINER_TYPE_ID_VIAL);
                            container.setCoordinateX(ic.getRow());
                            container.setCoordinateY(ic.getColumn());
                            container.setAmount(ic.getAmount());
                            container.setUnit(Unit.getUnit("µl"));
                            container.setDescription("this is a container for sample: " + sample.getName());
                            container.setCreatedBy(new UserReference("147"));

                            ContainerType containerType = container.getContainerType();
                            inhouseDb.getContainerRestService().doCreateContainer(containerType, container);
                        }
                    }
                }
            }
        }
    }

    enum TrayPrefix {TS, TM, TL, TH}

    private Map<String, List<TraysCsvEntry>> loadLocationsFromCSVForTray() {
        // setting path to csv file
        Map<String, List<TraysCsvEntry>> mapOfTrayEntries = new HashMap<>();

        for (TrayPrefix prefix : TrayPrefix.values()) {

            switch (prefix) {
                case TS -> {
                    List<TraysCsvEntry> ts = processLoading("TS");
                    mapOfTrayEntries.put("TS", ts);
                    break;
                }
                case TM -> {
                    List<TraysCsvEntry> tm = processLoading("TM");
                    mapOfTrayEntries.put("TM", tm);
                    break;
                }
                case TL -> {
                    List<TraysCsvEntry> tl = processLoading("TL");
                    mapOfTrayEntries.put("TL", tl);
                    break;
                }
                case TH -> {
                    List<TraysCsvEntry> th = processLoading("TH");
                    mapOfTrayEntries.put("TH", th);
                    break;

                }
                default -> processLoading("TH");
            }
        }
        return mapOfTrayEntries;
    }

    private List<TraysCsvEntry> processLoading(String tPrefix) {
        TraysCsvEntry entry;

        Path csvPath;
        csvPath = resolveCsvPath(tPrefix);

        // =========logging=============
        if (csvPath == null) {
            logger.error("csvPath not found, method loadLocationsFromCsv, class SampleContainerCreation");
        }
        logger.info("Starting import of CSV: {} (tray prefix {})\n", csvPath, tPrefix);
        // =========logging=============

        // set path for rejection
        Path rejectPath = resolveRejectPath();

        List<TraysCsvEntry> entries = new ArrayList<>();

        // starting io stream for reading and writing
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             BufferedWriter reject = Files.newBufferedWriter(rejectPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            // skip header
            String header1 = reader.readLine(); // "Traygröße: TH;;;;;;;"
            String header2 = reader.readLine(); // "Nummer;Standort/Person;Datum Ausgabe;Datum Rückgabe;Füllstand;Spalten (Zahlen);Zeilen (Buchstaben);"

            // starting line number
            int lineNo = 2;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                String raw = line.trim();

                // skip empty lines and ";;;;;;;"
                if (raw.isEmpty() || raw.replace(";", "").isEmpty()) continue;

                //Split the values even the empty values will be considered (limit -1)
                String[] parts = raw.split(";", -1);

                // required number of columns
                final int REQUIRED = 7;

                // here it will be checked if it has 7 parts
                if (parts.length < REQUIRED) {
                    writeReject(reject, lineNo, "Wrong column count (<7)", raw);
                    continue;
                }

                // set reading results in record (int line number, key = tray_number, place or person/,rows, columns, fill level)
                entry = toEntry(parts, reject, lineNo, raw, tPrefix);
                entries.add(entry);
            }

        } catch (IOException e) {
            logger.error("SampleContainerAttacher:-> method loadLocationsFromCSVForTray() caught an exception");
            throw new RuntimeException(e);
        }
        return entries;
    }

    private static TraysCsvEntry toEntry(String[] parts, BufferedWriter reject, int lineNo, String raw, String trayPrefix) throws IOException {
        String numberOfTray = parts[0].trim();      // "001" -> Nummer
        String storageRoom = parts[1].trim();      // "R003.K.8"  -> Standort/Person
        String giveAwayDate = parts[2].trim();      // never used
        String giveBackDate = parts[3].trim();      // never used
        String fillLevel = parts[4].trim();      // "Restpätze" | "Voll" | ""
        String columnNumber = parts[5].trim();      // "3" | "2"
        String rowNumber = parts[6].trim();      // "8 (A-H)" | "5 (A-E)" | "4 (A-D)"


        if (numberOfTray.isEmpty()) {
            // number are continuously and are never empty
            writeReject(reject, lineNo, "Empty Number", raw);
        }

        Integer columns = tryParseInt(columnNumber);
        Integer rows = parseLeadingInt(rowNumber);

        String key = trayPrefix + to3(numberOfTray); //"TH001"

        FillLevel level = switch (fillLevel.toLowerCase(Locale.ROOT)) {
            case "restplätze", "restplaetze" -> FillLevel.RESTPLAETZE;
            case "voll" -> FillLevel.VOLL;
            case "" -> FillLevel.EMPTY;
            default -> {
                writeReject(reject, lineNo, "Unknown fill level: " + fillLevel, raw);
                yield FillLevel.EMPTY;
            }
        };
        return new TraysCsvEntry(key, storageRoom, columns, rows, level);
    }

    /* ====================== helpers ====================== */
    private Path resolveCsvPath(String trayPrefix) {

        String specificKey = switch (trayPrefix) {
            case "TS" -> LOCATIONS_TS;
            case "TM" -> LOCATIONS_TM;
            case "TL" -> LOCATIONS_TL;
            case "TH" -> LOCATIONS_TH;
            default -> null;
        };

        String dir = specificKey != null ? inhouseDb.getConfigString(specificKey) : null;

        if (dir != null && !dir.isBlank()) {
            return Paths.get(dir);
        }
        return null;
    }

    private Path resolveRejectPath() {
        String dir = inhouseDb.getConfigString(LOCATIONS_CSV_REJECTFILE);

        if (dir != null && !dir.isBlank()) {
            return Paths.get(dir);
        }
        return null;
    }

    private static String extractTrayPrefix(String name) {
        // "TH006" -> "TH", "TM12" -> "TM"
        Matcher m = Pattern.compile("(\\d+)").matcher(name);
        return m.find() ? m.group(1) : "";
    }

    private static String extractTrayNumber(String name) {
        // "TH006" -> "006", "TM12" -> "012"
        Matcher m = Pattern.compile("^([A-Z]{2})").matcher(name);
        if (!m.find()) return "000";
        String num = m.group(1);
        return to3(num);
    }

    private static String to3(String n) {
        if (n == null || n.isBlank()) return "000";
        if (n.matches("\\d+")) {
            return String.format("%03d", Integer.parseInt(n));
        }
        return n;
    }

    private static void writeReject(BufferedWriter reject, int lineNo, String reason, String raw) throws IOException {
        reject.write("Line " + lineNo + " [" + reason + "]: " + raw);
        reject.newLine();
    }

    private static Integer tryParseInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseLeadingInt(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("^(\\d+)").matcher(s.trim());
        if (m.find()) {
            try {
                return Integer.valueOf(m.group(1));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}