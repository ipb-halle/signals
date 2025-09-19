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
import de.ipb_halle.inhouse.InhouseExtract;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.inventory.*;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.users.User;
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

    public static final String CONTAINER_TYPE_ID_VIAL = "b9f1ac14-3a84-4812-98b3-6543abc68d62";
    public static final String CONTAINER_OBLIGATORY_FIELD_SECURITY = "b50d782a-8247-4adb-8dc9-9ea69aff987e";
    public static final String CONTAINER_OBLIGATORY_FIELD_WEIGHT = "739151dc-bd74-407f-8d04-16d4b193a13d";
    public static final String LOCATION_TYPE_ID_TRAY = "e4d8f923-d407-4bee-8121-857a86f9e59d"; //tray
    public static final String LOCATION_ANCESTOR_ROOM_ID_TEST = "2af549e1-e596-44d9-b800-a63e258a58e0"; // room R301.S1
    private static final String LOCATION_ID_ROOM = "b9fab5b8-6c26-47f8-8694-320c7c439879";
    // Laborbank ("Bench) e.g. P1
    private static final String LOCATION_TYPE_ID_P = "81ff1644-2a27-4fb1-a597-5d83e1920c0d";
    // Schrank ("Cabinet") e.g. S1
    private static final String LOCATION_TYPE_ID_S = "017929f3-cd0e-466c-ac94-c21ce5fe8c31";
    // Kühlschrank ("Refrigerator") e.g. K1
    private static final String LOCATION_TYPE_ID_K = "4211988a-03bf-4103-a8d9-aff5dcf16067";
    // Kühlschrankfach e.g. sh4 /dr4
    private static final String LOCATION_TYPE_ID_sh = "3fd2af9d-e066-405c-8a64-d67c4a781e5d";
    private static final String LOCATION_TYPE_ID_dr = "3fd2af9d-e066-405c-8a64-d67c4a781e5d";


    private final InhouseDB inhouseDb;

    enum FillLevel {RESTPLAETZE, VOLL, EMPTY}

    private record TraysCsvEntry(
            String key,             // e.g. "TH067"
            String storageRoom,     // e.g. "R003.K8"
            Integer columns,        // e.g. 3
            Integer rows,           // e.g 8
            FillLevel fillLevel
    ) {
    }

    record GridForContainerAndAmount(
            Integer containerCoordinateX,
            Integer containerCoordinateY,
            Double amount
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
     */                                                                                 //molproc or orgproc
    public void attachSampleContainer(Sample sample, String eid, Integer procId, String correlationContext) throws Exception {
        logger.info("SAMPLE CONTAINER ATTACHER:=> checking sample eid = {} \n", eid);

        // 1) Load the correlation by procedure id for structure (aim context ="molproc" or "orgproc")
        List<InhouseCorrelation> corr = inhouseDb.getInhouseDbService().loadCorrelationByProcedureId(procId);
        if (corr == null || corr.isEmpty()) {
            logger.error("The Inhouse Correlation is empty!");
            return;
        }

        for (InhouseCorrelation c : corr) {
            // Load containers location of inhouseDB from csv
            Map<String, List<TraysCsvEntry>> mapOfStoragePlaceToStorageRoom = loadLocationsFromCSVForTray();

            // get the "molproc" corr Id
            Integer corrId = c.getCorrId();

            // filter for "molproc" correlation or "orgproc"
            if (c.getContext().equalsIgnoreCase("molproc")) {

                //  load with the help of "molproc"  ID the InhouseContainers (Samples from InhouseDB)
                List<InhouseContainer> containers = inhouseDb.getInhouseDbService().loadInhouseContainerByMolProcId(corrId);
                logger.info("Containers: {}\n", Arrays.toString(containers.toArray()));

                // check if it is empty
                if (containers == null || containers.isEmpty()) {
                    logger.info("Inhouse containers are empty for procId = {}\n", procId);
                    continue;
                }

                // if not
                for (InhouseContainer ic : containers) {
                    // find the storage Place e. g TS005
                    String storagePlace = ic.getLocation();
                    Integer containerCoordinateX = ic.getRow();
                    Integer containerCoordinateY = ic.getColumn();
                    Double amount = ic.getAmount();

                    GridForContainerAndAmount gcm = new GridForContainerAndAmount(containerCoordinateX, containerCoordinateY, amount);

                    createContainer(sample, eid, storagePlace, mapOfStoragePlaceToStorageRoom, gcm);
                }
            } else if (c.getContext().equalsIgnoreCase("orgproc")) {
                logger.info("SAMPLE_CONTAINER_ATTACHER:=> inhouse container c get correlation context = {}\n", c.getContext());

                // Here we create Signals containers in case of InhouseExtracts
                List<InhouseExtract> extracts = inhouseDb.getInhouseDbService().loadExtractByCorrOrgProcId(corrId);

                // check if it is empty
                if (extracts == null || extracts.isEmpty()) {
                    logger.info("Inhouse extracts are empty for procId = {}\n", procId);
                    continue;
                }
                for (InhouseExtract ix : extracts) {
                    String storagePlaceArray[] = ix.getStoragePlace().trim().split("\\.");
                    String storagePlace = storagePlaceArray[0];// TM018
                    String coordinates = storagePlaceArray[1];
                    Integer coordinatesXY[] = parseCoordinate(coordinates);
                    Integer containerCoordinateX = coordinatesXY[0];
                    Integer containerCoordinateY = coordinatesXY[1];
                    Double amount = ix.getAmount();

                    GridForContainerAndAmount gcm = new GridForContainerAndAmount(containerCoordinateX, containerCoordinateY, amount);

                    createContainer(sample, eid, storagePlace, mapOfStoragePlaceToStorageRoom, gcm);
                }

            } else {
                logger.error("SAMPLE_CONTAINER_ATTACHER:=>UNKNOWN CONTEXT!!!\n inhouse container c get correlation context = {}\n", c.getContext());
                continue;
            }
        }
    }

    private Integer[] parseCoordinate(String input) {
        input = input.toUpperCase();
        String letters = input.replaceAll("[0-9]", "");
        String numbers = input.replaceAll("[^0-9]", "");

        int col = letters.charAt(0) - 'A';
        int row = Integer.parseInt(numbers) - 1;

        return new Integer[]{col, row};
    }

    private void createContainer(Sample sample, String eid, String storagePlace, Map<String, List<TraysCsvEntry>> mapOfStoragePlaceToStorageRoom, GridForContainerAndAmount gcm) {
        // Location
        Location rootAncestor;
        Location ancestorLocation;
        Location locationTray;


        // Extracting Tray Prefix TS, TM, TL, TH (small, middle, large, huge)
        String trayPrefix = extractTrayPrefix(storagePlace);

        List<TraysCsvEntry> traysCsvEntries = mapOfStoragePlaceToStorageRoom.get(trayPrefix);
        if (traysCsvEntries == null || traysCsvEntries.isEmpty()) {
            logger.warn("No CSV entries for tray prefix '{}'", trayPrefix);
            return;
        }
        for (TraysCsvEntry entr : traysCsvEntries) {
            // e.g. key ="TH001"
            if (entr.key.equalsIgnoreCase(storagePlace)) {
                User user = new User();
                user.setId("140");

                //====================== Handling ROOT location ===============================
                //load root ancestor location e.g. R003
                String roomNumber = entr.storageRoom.trim().split("\\.")[0];
                //                                                                  R003
                rootAncestor = inhouseDb.getLocationDbService().loadLocationByName(roomNumber);
                if (rootAncestor == null) {
                    String locationTypeId = LOCATION_ID_ROOM;
                    String name = roomNumber;
                    String description = String.format("This is a room Nr. %s", roomNumber);
                    rootAncestor = createLocation(user, locationTypeId, name, description, null, entr);
                }

                //====================== Handling ANCESTOR location ===============================
                //load ancestor Location e.g. R003.K8 or R002.G1 or R2-109.P1                   R003.K8
                ancestorLocation = inhouseDb.getLocationDbService().loadLocationByName(entr.storageRoom);
                if (ancestorLocation == null) {
                    String storageDevice = entr.storageRoom.trim().split("\\.")[1];
                    Matcher m = Pattern.compile("^([A-Za-z]+)(\\d+)$").matcher(storageDevice);
                    if (!m.find()) {
                        logger.error("Cannot parse device type from '{}'", storageDevice);
                        continue;
                    }
                    String deviceType = m.group(1); // "K", "P", "S", "G", ...

                    String locationTypeID = mapDeviceTypeToLocationTypeId(deviceType);
                    if (locationTypeID == null) continue;
                    String name = entr.storageRoom;
                    String description = String.format("This is a location %s", name);

                    ancestorLocation = createLocation(user, locationTypeID, name, description, rootAncestor.getId(), entr);
                }

                //====================== Handling TRAY location ===============================
                // load storage place                                               e.g. TH001
                locationTray = inhouseDb.getLocationDbService().loadLocationByName(storagePlace);
                if (locationTray == null) {
                    logger.info("Location with name = {} is not found in the DB\n", storagePlace);
                    logger.info("Creating new location\n");
                    String name = storagePlace;
                    String description = String.format("This is a Tray %s", name);
                    // locationTypeID, name, description, ancestorLocationId
                    locationTray = createLocation(user, LOCATION_TYPE_ID_TRAY, name, description, ancestorLocation.getId(), entr);
                }

                // =========== end of the test code ==============
                logger.info("STRATING CREATING CONTAINER");
                Container container = new Container();
                container.setName(sample.getName());
                // here the sample eid will be set
                container.setMaterial(new MaterialReference().setId(eid));
                // here location eid will be set
                container.setLocation(new LocationReference().setId(locationTray.getId()));
                container.setContainerTypeId(CONTAINER_TYPE_ID_VIAL);
                container.setCoordinateX(gcm.containerCoordinateX);
                container.setCoordinateY(gcm.containerCoordinateY);
                container.setAmount(gcm.amount);
                container.setUnit(Unit.getUnit("mg"));
                container.setDescription("This is a container for sample: " + sample.getId());
                container.setCreatedBy(user);
                container.setUpdatedBy(user);

                List<FieldValue> fieldValues = new ArrayList<>();

                // Field Security
                FieldValue fvConSecurity = new FieldValue();
                Field sec = new Field();
                sec.setId(CONTAINER_OBLIGATORY_FIELD_SECURITY);
                sec.setReadOnly(false);
                sec.setCalculated(false);
                sec.setRequired(true);
                fvConSecurity.setField(sec);
                fvConSecurity.setFieldId(sec.getId());
                fvConSecurity.setValue("Default");
                fieldValues.add(fvConSecurity);

                // Field tara weight
                FieldValue fvTaraWeight = new FieldValue();
                Field weight = new Field();
                weight.setId(CONTAINER_OBLIGATORY_FIELD_WEIGHT);
                weight.setReadOnly(false);
                weight.setCalculated(false);
                weight.setRequired(true);
                fvTaraWeight.setField(weight);
                fvTaraWeight.setFieldId(weight.getId());
                //fvTaraWeight.setValue(String.valueOf("ic.getTara()")); // here is the weight of TARA = container meant
                fvTaraWeight.setValue(String.valueOf("2046")); // here is the weight of TARA = container meant
                fieldValues.add(fvTaraWeight);

                container.setFieldValues(fieldValues);

                ContainerType containerType = new ContainerType();
                containerType.setId(CONTAINER_TYPE_ID_VIAL);
                containerType.setName("Vial");
                containerType.setDescription("Standard vial");

                String containerEid = inhouseDb.getContainerRestService().doCreateContainer(containerType, container);
                container.setId(containerEid);
                logger.info("Container created eid = {}\n ", containerEid);

            }
        }
    }

    private String mapDeviceTypeToLocationTypeId(String deviceType) {
        return switch (deviceType.toUpperCase(Locale.ROOT)) {
            case "P" -> LOCATION_TYPE_ID_P;   // Bench / P1
            case "S" -> LOCATION_TYPE_ID_S;   // Cabinet / S1
            case "K" -> LOCATION_TYPE_ID_K;   // Kühlschrank / K1
            case "SH" -> LOCATION_TYPE_ID_sh;  // Kühlschrankfach sh*
            case "DR" -> LOCATION_TYPE_ID_dr;  // Kühlschrankfach dr*
            default -> {
                logger.error("Unknown deviceType '{}'", deviceType);
                yield null;
            }
        };
    }

    private Location createLocation(User user, String locationTypeId, String name, String description, String ancestorLocationId, TraysCsvEntry entry) {
        // toDo: this is a case for Test
        // =========== Create new Location begin of the test code ==============
        Location location = new Location();
        location.setLocationTypeId(locationTypeId);
        location.setName(String.format("%s", name));
        location.setDescription(description);
        location.setAncestorId(ancestorLocationId);
        location.setCreatedBy(user);
        location.setUpdatedBy(user);
        if (locationTypeId.equalsIgnoreCase(LOCATION_TYPE_ID_TRAY)) {
            location.setGrid(true);
            location.setRows(entry.rows);
            location.setColumns(entry.columns);
        }

        logger.info("Locations set ={}\n", location.toString());

        LocationType locationType = new LocationType();
        locationType.setId(locationTypeId);
        locationType.setName("Tray " + System.currentTimeMillis());
        locationType.setDescription("This is a tray");

        // Rest call to create a location
        String locationEid = inhouseDb.getLocationRestService().doCreateLocation(locationType, location);
        location.setId(locationEid);
        logger.info("Location created eid = {}\n ", locationEid);
        //WE NEED TO SAVE THE LOCATION TO OUR SIGNALS DATABASE
        logger.info("STARTING SAVING LOCATION");
        inhouseDb.getLocationDbService().saveLocation(location);
        logger.info("Created location saved = {}\n", location.toString());

        return location;
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
        // "TH006" -> "TH"
        Matcher m = Pattern.compile("^([A-Z]{2})").matcher(name.trim());
        return m.find() ? m.group(1) : "";
    }

    private static String extractTrayNumber(String name) {
        // "TH006" -> "006", "TM12" -> "012"
        Matcher m = Pattern.compile("(\\d+)$").matcher(name.trim());
        return m.find() ? to3(m.group(1)) : "000";
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