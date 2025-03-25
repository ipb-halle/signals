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

import de.ipb_halle.signals.materials.Library;
import de.ipb_halle.signals.materials.LibraryDbService;
import de.ipb_halle.signals.materials.Material;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Migration tool for the InhouseDB
 *
 * @author fbroda
 */
public class Compounds {

    public final static String COMPOUNDS_ACCESS = "compounds.access";
    public final static String COMPOUNDS_BATCH_NAME = "compounds.batchName";
    public final static String COMPOUNDS_CHEMICAL_DRAWING = "compounds.chemicalDrawing";
    public final static String COMPOUNDS_FILENAME = "compounds.filename";
    public final static String COMPOUNDS_LIBRARY_ID = "compounds.libraryId";
    public final static String COMPOUNDS_OWNER = "compounds.owner";
    public final static String COMPOUNDS_REJECTFILE = "compounds.rejectfile";
    public final static String COMPOUNDS_SYNONYMS = "compounds.synonyms";
    public final static String COMPOUNDS_FIELD_ACCESS = "compounds.fields.access";
    public final static String COMPOUNDS_FIELD_ASSET_NAME = "compounds.fields.assetName";
    public final static String COMPOUNDS_FIELD_BATCH_NAME = "compounds.fields.batchName";
    public final static String COMPOUNDS_FIELD_CASRN = "compounds.fields.casrn";
    public final static String COMPOUNDS_FIELD_CHEMICAL_DRAWING = "compounds.fields.chemicalDrawing";
    public final static String COMPOUNDS_FIELD_IPBCODE = "compounds.fields.ipbcode";
    public final static String COMPOUNDS_FIELD_MOLID = "compounds.fields.molid";

    /**
     *
     * @param inhouseDB
     * @throws Exception
     */
    private void importCompounds(InhouseDB inhouseDB) throws Exception {
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


        String linePattern = "^([^;]*);"         //  1 Structure (SMILES)
                + "([^;]*)?;"                    //  2 SciFinderDoneAt
                + "([^;]*)?;"                    //  3 CAS-RN
                + "([^;]*)?;"                    //  4 StructureCheckedAt
                + "([^;]*)?;"                    //  5 StructureCheckedBy
                + "([^;]*)?;"                    //  6 Correction
                + "\"?([^\";]*)?\"?;"            //  7 MolTableRemarks
                + "(IPB\\d+)?;"                  //  8 IPBCode
                + "(\\d+(,\\d+)?)?;"             //  9 MolWeight
                + "([^;]*)?;"                    // 10 Formula
                + "([^;]*)?;"                    // 11 HighResolutionMS
                + "([^;]*)?;"                    // 12 Reliability
                + "([^;]*)?;"                    // 13 Date
                + "(\\d+);"                      // 14 Mol_ID
                + "([^;]*)?;"                    // 15 temporary mark
                + "([^;]*)?$";                   // 16 modelling

        Pattern pat = Pattern.compile(linePattern);
        BufferedReader reader = new BufferedReader(new FileReader(inhouseDB.getConfigString(COMPOUNDS_FILENAME)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(COMPOUNDS_REJECTFILE)));
        reader.readLine(); // discard header
        while(reader.ready()) {
            String st = reader.readLine();
            Matcher matcher = pat.matcher(st);
            if (matcher.matches()) {
                InhouseCompound compound = new InhouseCompound()
                        .setMolId(Integer.parseInt(matcher.group(15)))
                        .setCasrn(matcher.group(3))
                        .setRemarks(matcher.group(7))
                        .setIpbCode(matcher.group(8))
                        .setName(String.format("AUTO molId %s", matcher.group(15)));
                inhouseDB.getInhouseDbService().save(compound);

            } else {
                // write non-accepted line to error log
                writer.append(st);
                writer.newLine();
            }
        }
        writer.close();
      }

    private void importCompoundNames(InhouseDB inhouseDB) throws Exception {
        RTF rtf = new RTF(inhouseDB);
        rtf.readCompoundSynonym(inhouseDB.getConfigString(COMPOUNDS_SYNONYMS));

    }

    public void importData(InhouseDB inhouseDB) throws Exception {
        // importCompounds(inhouseDB);
        // importCompoundNames(inhouseDB);

        Library library = inhouseDB.getLibraryDbService().loadById(
                inhouseDB.getConfigString(COMPOUNDS_LIBRARY_ID));
        List<InhouseCompound> compounds = inhouseDB.getInhouseDbService().loadCompounds();

        // sublist(x,y) - restrict to a limited number of records for testing
        for (InhouseCompound compound : compounds.subList(2,5)) {
            Material asset = compound.createAsset(inhouseDB);
            Material batch = compound.createBatch(inhouseDB);
//            Material mat = inhouseDB.getMaterialRestService().doCreateMaterial(
//                    library,
//                    asset,
//                    batch);
            System.out.printf("ASSET %s", asset.toString());
            System.out.printf("BATCH %s", batch.toString());
        }
    }
}
