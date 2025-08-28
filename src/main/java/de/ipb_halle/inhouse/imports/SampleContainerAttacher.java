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
import de.ipb_halle.inhouse.InhouseLocation;
import de.ipb_halle.signals.inventory.Container;
import de.ipb_halle.signals.inventory.ContainerType;
import de.ipb_halle.signals.inventory.Location;
import de.ipb_halle.signals.inventory.LocationReference;
import de.ipb_halle.signals.sample.Sample;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleContainerAttacher {
    public static final String LOCATIONS_CSV_FILENAME = "locations";
    public static final String LOCATIONS_CSV_REJECTFILE = "locations.reject";
    public static final String LOCATIONS_TS = "locations.ts";
    public static final String LOCATIONS_TM = "locations.tm";
    public static final String LOCATIONS_TL = "locations.tl";
    public static final String LOCATIONS_TH = "locations.th";
    private final InhouseDB inhouseDb;
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
        List<InhouseCorrelation> corr = inhouseDb.getInhouseDbService().loadCorrelationByProcedureId(procId);
        if (corr == null || corr.isEmpty()) {
            logger.error("The Inhouse Correlation is empty!");
            return;
        }

        for (InhouseCorrelation c : corr) {
            if (c.getContext().equalsIgnoreCase("molproc")) {
                Integer molProcId = c.getCorrId();
                List<InhouseContainer> containers = inhouseDb.getInhouseDbService().loadInhouseContainerByMolProcId(molProcId);
                logger.info("Containers: {}\n", Arrays.toString(containers.toArray()));
                if (containers == null || containers.isEmpty()) {
                    logger.info("Inhouse containers are empty for procId = {}\n", procId);
                }

                for (InhouseContainer ic : containers) {
                    Container container = createSignalsContainer(ic);
                    ContainerType containerType = container.getContainerType();
                    inhouseDb.getContainerRestService().attachContainerToSample(sample, eid, container, containerType);
                }
            }
        }
    }

    private Container createSignalsContainer(InhouseContainer ic) throws Exception {
        Location location = findLocation(ic);
        String locationId = location.getId();
        String locationTypeId = location.getLocationTypeId();
        Container container = new Container();
        container.setLocation(new LocationReference().setId(locationId));
        //toDo: setter and getter

        return container;
    }

    private Location findLocation(InhouseContainer ic) throws Exception {
        InhouseLocation il = inhouseDb.getInhouseDbService().loadInhouseLocationById(ic.getLocationId());
        if (il.getSignalsLocation() == null) {
            String iLocationName = il.getName();
            Map<String, Location> locationMap = loadLocationsFromCSVForTray(iLocationName);
            return locationMap.get(iLocationName);
        }
        return il.getSignalsLocation();
    }


    private Map<String, Location> loadLocationsFromCSVForTray(String trayPrefix) throws Exception {
        Path csvPath = resolveCsvPath(trayPrefix);
        if (csvPath == null) {
            logger.error("csvPath not found, method loadLocationsFromCsv, class SampleContainerCreation");
            throw new FileNotFoundException("Не настроен путь к CSV для префикса " + trayPrefix);
        }

        logger.info("Starting import of CSV: {} (tray prefix {})\n", csvPath, trayPrefix);

        Map<String, Location> locationMap = new HashMap<>();
        Path rejectPath = resolveRejectPath();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             BufferedWriter reject = Files.newBufferedWriter(rejectPath, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            String header1 = reader.readLine(); // "Traygröße: TH;;;;;;;"
            String header2 = reader.readLine(); // "Nummer;Standort/Person;Datum Ausgabe;Datum Rückgabe;Füllstand;Spalten (Zahlen);Zeilen (Buchstaben);"

            int lineNo = 2;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String raw = line.trim();
                // skip empty lines and ";;;;;;;"
                if (raw.isEmpty() || raw.replace(";", "").isEmpty()) continue;

                String[] parts = raw.split(";", -1);
                if (parts.length < 7) {
                    writeReject(reject, lineNo, "Wrong column count (<7)", raw);
                    continue;
                }

                String nummer = parts[0].trim(); // "001" -> Nummer
                String standortPerson = parts[1].trim(); // "R003.K.8"  -> Standort/Person
                String datumAusgabe = parts[2].trim();
                String datumRückgabe = parts[3].trim();
                String feullstand = parts[4].trim(); // "Restpätze" | "Voll" | ""
                String spaltenZahlStr = parts[5].trim(); // "3" | "2"
                String zeilenBuchstStr = parts[6].trim(); // "8 (A-H)" | "5 (A-E)" | "4 (A-D)"

                if (nummer.isEmpty()) {
                    writeReject(reject, lineNo, "Empty Number", raw);
                }

                Integer columns = tryParseInt(spaltenZahlStr);
                Integer rows = parseLeadingInt(zeilenBuchstStr);


            }
        }


        /*
        // pattern of Location
        Pattern pattern = Pattern.compile(
                "^'([A-Z]{2,3})';"                              // 'Nummer';
                + "'([0-9]{3}[^']*)';"                          // 'Standort/Person';
                + "'(.*)';"                                     // 'Datum Ausgabe';
                + "([0-9]+);"                                   // Datum Rückgabe;
                + "([0-9]*);"                                   // Füllstand;
                + ";"                                           // Spalten (Zahlen);
                + ";"                                           // Zeilen (Buchstaben);
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
        Map<String, Location> locationMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inhouseDb.getConfigString(LOCATIONS_CSV_FILENAME)));
             BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDb.getConfigString(LOCATIONS_CSV_REJECTFILE)))) {
            reader.readLine(); // discard header
            int line = 1;
            while (reader.ready()) {
                String st = reader.readLine();
                line++;
                Matcher matcher = pattern.matcher(st);
                if (matcher.matches()) {
                    String containerName = matcher.group(1); // e.g. TM006

                    Location location = new Location();
                    //toDo:setter and getter
                    locationMap.put(containerName, location);
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
        return locationMap;
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


}
