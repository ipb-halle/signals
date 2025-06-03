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
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.structure.MoleculeEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlInsertBuilder;
*/

import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.materials.Library;
import de.ipb_halle.signals.materials.LibraryDbService;
import de.ipb_halle.signals.materials.Material;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Compound migration tool for Signals platform.
 *
 * <p>This class handles the import of chemical compound data from a legacy in-house database
 * into the Signals system. It parses structured data files, loads compound metadata,
 * binds them with predefined field definitions, and creates corresponding assets (materials)
 * and batches within a specified Signals library.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Parsing legacy compound data files (CSV-like format with semicolon separators)</li>
 *   <li>Importing synonym data from RTF files</li>
 *   <li>Resolving and mapping field definitions for each compound</li>
 *   <li>Creating new Signals material entries via REST services</li>
 *   <li>Storing resulting entity IDs back into the local in-house database</li>
 * </ul>
 *
 * <p>This tool is intended to assist with controlled one-time migrations and should be
 * adapted before use in production (e.g., replacing hardcoded sublist ranges with
 * configuration options).</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *     Compounds compoundsImporter = new Compounds(inhouseDbInstance);
 *     compoundsImporter.importData();
 * </pre>
 *
 * <p><strong>Note for developers:</strong> See methods {@code importCompounds()},
 * {@code importCompoundNames()}, and {@code importData()} for the high-level
 * processing flow.</p>
 */
public class Compounds {

    // Configuration keys for reading compound-related files and fields
    public final static String COMPOUNDS_ACCESS = "compounds.access";
    public final static String COMPOUNDS_BATCH_NAME = "compounds.batchName";
    public final static String COMPOUNDS_CHEMICAL_DRAWING = "compounds.chemicalDrawing";
    public final static String COMPOUNDS_FILENAME = "compounds.filename";
    public final static String COMPOUNDS_LIBRARY_ID = "compounds.libraryId";
    public final static String COMPOUNDS_OWNER = "compounds.owner";
    public final static String COMPOUNDS_REJECTFILE = "compounds.rejectfile";
    public final static String COMPOUNDS_SYNONYMS = "compounds.synonyms";

    // Configuration keys for compound field mappings
    public final static String COMPOUNDS_FIELD_ACCESS = "compounds.fields.access";
    public final static String COMPOUNDS_FIELD_ASSET_NAME = "compounds.fields.assetName";
    public final static String COMPOUNDS_FIELD_BATCH_NAME = "compounds.fields.batchName";
    public final static String COMPOUNDS_FIELD_CASRN = "compounds.fields.casrn";
    public final static String COMPOUNDS_FIELD_CHEMICAL_DRAWING = "compounds.fields.chemicalDrawing";
    public final static String COMPOUNDS_FIELD_IPBCODE = "compounds.fields.ipbcode";
    public final static String COMPOUNDS_FIELD_MOLID = "compounds.fields.molid";

    private final Logger logger = LogManager.getLogger(Compounds.class);
    private InhouseDB inhouseDB;

    public Compounds(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    /**
     * Parses the raw compounds data file and stores valid entries into the inhouse DB.
     */
    private void importCompounds() throws Exception {
        System.out.println("Importing compounds");

        // 01 Structure                 -> ignored
        // 02 SciFinderDoneAt
        // 03 CAS-RN                    -> Field CAS/RN
        // 04 StructureCheckedAt
        // 05 StructureCheckedBy
        // 06 Correction
        // 07 MolTableRemarks           -> Description
        // 08 IPBCode                   -> Field IPB-Code
        // 09 MolWeight
        // 10 Formula
        // 11 HighResolutionMS
        // 12 Reliability
        // 13 Date
        // 14 Mol_ID                    -> Field Mol_ID; Pointer to CDXML file
        // 15 temporary mark
        // 16 modelling

        // O=C1C=CN(C2=CC=CC=C2)N=C1C(NC3=CC=CC=C3)=O;01.01.1001;;;;no;"Do NOT edit or delete!!";
        // IPB002897;291,31;C17H13N3O2;1.234567000000000;sure;18.11.2020;62;<o>;25.05.2007
        // 19.07.2004;710298-89-8;;;;;;;;;;24.11.2005;63;;25.05.2007

        // Regex pattern to parse each line of the data file with semicolon-separated fields
        Pattern pattern = Pattern.compile("^([^;]*);"         //  1 Structure (SMILES)
                + "([^;]*)?;"                    //  2 SciFinderDoneAt
                + "([^;]*)?;"                    //  3 CAS-RN
                + "([^;]*)?;"                    //  4 StructureCheckedAt
                + "([^;]*)?;"                    //  5 StructureCheckedBy
                + "([^;]*)?;"                    //  6 Correction
                + "([^;]*)?;"                  //  7 MolTableRemarks
                + "(IPB\\d+)?;"                  //  8 IPBCode
                + "(\\d+(,\\d+)?)?;"             //  9 MolWeight
                + "([^;]*)?;"                    // 10 Formula
                + "([^;]*)?;"                    // 11 HighResolutionMS
                + "([^;]*)?;"                    // 12 Reliability
                + "([^;]*)?;"                    // 13 Date
                + "(\\d+);"                      // 14 Mol_ID
                + "([^;]*)?;"                    // 15 temporary mark
                + "([^;]*)?$");                  // 16 modelling

        // to strip quotes
        Pattern quotePattern = Pattern.compile("^\"(.*)\"$");

        // Setup readers and writers for input and rejected entries
        BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(COMPOUNDS_FILENAME)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(COMPOUNDS_REJECTFILE)));
        reader.readLine(); // Skip header

        while (reader.ready()) {
            String st = reader.readLine();
            Matcher matcher = pattern.matcher(st);

            if (matcher.matches()) {
                // Create compound entry and populate its fields from regex groups
                InhouseCompound compound = new InhouseCompound()
                        .setMolId(Integer.parseInt(matcher.group(15)))
                        .setCasrn(matcher.group(3))
                        .setIpbCode(matcher.group(8))
                        .setName(String.format("AUTO molId %s", matcher.group(15)));

                // Strip quotes from remarks field if present
                Matcher remarkMatcher = quotePattern.matcher(matcher.group(7));
                compound.setRemarks(remarkMatcher.matches() ? remarkMatcher.group(1) : matcher.group(7));

                // Save compound to inhouse DB
                inhouseDB.getInhouseDbService().save(compound);
            } else {
                // If line doesn't match expected pattern, write it to the reject file
                writer.append(st);
                writer.newLine();
            }
        }
        writer.close();
    }

    /**
     * Imports compound synonyms from a separate RTF file using RTF utility class.
     */
    private void importCompoundNames() throws Exception {
        RTF rtf = new RTF(inhouseDB);
        rtf.readCompoundSynonym(inhouseDB.getConfigString(COMPOUNDS_SYNONYMS));
        logger.info("COMPOUND NAMES SUCCESSFULLY IMPORTED");
    }

    /**
     * Main import procedure for all compounds.
     * Parses data, loads configuration, and creates materials in Signals.
     */
    public void importData() throws Exception {
        importCompounds();          // Step 1: parse and store compounds
        importCompoundNames();      // Step 2: import synonyms

        Library library = loadTargetLibrary();                      // Step 3: load Signals Compound library
        List<InhouseCompound> compounds = loadInhouseCompounds();   // Step 4: load parsed compounds

        // Step 5: loop through compounds (for testing: restrict to sublist)
        for (InhouseCompound compound : selectSubset(compounds)) {
            importCompound(compound, library);
        }
    }

    /**
     * Loads the configured Signals library (target for material import).
     */
    private Library loadTargetLibrary() {
        // Load target library for the compound import
        String configString = String.format("assetType:%s", inhouseDB.getConfigString(COMPOUNDS_LIBRARY_ID));
        logger.info("LIBRARY FOR COMPOUND IS = {}", configString);
        return inhouseDB.getLibraryDbService().loadById(configString);
    }

    /**
     * Loads all compound records already parsed and stored in the inhouse database.
     */
    private List<InhouseCompound> loadInhouseCompounds() {
        // Load all parsed compound entries
        List<InhouseCompound> compounds = inhouseDB.getInhouseDbService().loadCompounds();
        logger.info("COMPOUNDS ARRAY SIZE = {}", compounds.size());
        return compounds;
    }

    /**
     * Selects a subset of compounds (currently hardcoded for testing purposes).
     */
    private List<InhouseCompound> selectSubset(List<InhouseCompound> compounds){
        return compounds.subList(2,5);
    }

    /**
     * Imports a single compound by transforming it into Signals Material and saving it via REST.
     */
    private void importCompound(InhouseCompound compound, Library library) throws IOException {
        Material asset = compound.createAsset(inhouseDB);
        Material batch = compound.createBatch(inhouseDB);

        // Load all field definitions from DB and index by ID
        Map<String, Field> fieldMap = inhouseDB.getFieldDbService()
                .loadFields(new HashMap<>())
                .stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));

        // Bind fields to asset and batch using fieldMap
        compound.bindFieldDefinitions(fieldMap, asset);
        compound.bindFieldDefinitions(fieldMap, batch);

        // Create material in Signals platform
        Material mat = inhouseDB.getMaterialRestService().doCreateMaterial(library, asset, batch);
        compound.setEid(mat.getId());

        // Update the inhouse DB entry with new Signals entity ID
        inhouseDB.getInhouseDbService().save(compound);
    }
}
