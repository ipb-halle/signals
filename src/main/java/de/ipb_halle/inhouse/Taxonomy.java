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

import jakarta.persistence.criteria.CriteriaBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration tool for the InhouseDB
 *
 * @author fbroda
 */
public class Taxonomy {

    public final static String INPUT_TAXONOMY_CLASSES = "taxonomy.inputClasses";
    public final static String INPUT_TAXONOMY_FAMILIES = "taxonomy.inputFamilies";
    public final static String INPUT_TAXONOMY_SPECIES = "taxonomy.inputSpecies";
    public final static String INPUT_TAXONOMY_SPECIES_SYNONYMS = "taxonomy.inputSpeciesSynonyms";
    public final static String INPUT_TAXONOMY_STRAINS = "taxonomy.inputStrains";
    public final static String INPUT_ORGANISM_ID = "taxonomy.inputOrganisms";
    public final static String TAXONOMY_REJECT_FILE = "taxonomy.rejectFile";
    public final static String DEFAULT_STRAIN_ID = "taxonomy.defaultStrainId";

    public final static String UNKNOWN_TAXONOMY_PARENT_ID = "UNKNOWN_TAXONOMY_PARENT_ID";

    private InhouseDB inhouseDB;
    private Pattern quotePattern;

    public Taxonomy(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        quotePattern = Pattern.compile("\"(.*)\"");
    }

    public void importData() throws Exception {
        importTaxonomyClasses();
        importTaxonomyFamilies();
        importTaxonomySpecies();
        importTaxonomySpeciesSynonyms();
        importTaxonomyStrains();
        importOrganismIds();
    }

    void reject(BufferedWriter writer, String where, String line) throws IOException {
        writer.write(String.format("%s\tline\n", where, line));
    }

    private void importOrganismIds() throws Exception {
        System.out.println("Importing organism Ids");
        InhouseDbService dbService = inhouseDB.getInhouseDbService();
        int defaultStrainId = inhouseDB.getConfigInt(DEFAULT_STRAIN_ID);

        // organismId
        Pattern pattern = Pattern.compile("^(\\d+);(\\d+);(\\d*);(.*)$");
        BufferedReader reader = new BufferedReader(new FileReader(this.inhouseDB.getConfigString(INPUT_ORGANISM_ID)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(TAXONOMY_REJECT_FILE)));
        reader.readLine(); // discard header
        reader.readLine(); // discard 2nd line too
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int organismId = Integer.parseInt(matcher.group(1));
                int speciesId = Integer.parseInt(matcher.group(2));
                String strain = matcher.group(3);
                int strainId = strain.isEmpty() ? 0 : Integer.parseInt(strain);
                strainId = (strainId == defaultStrainId) ? 0 : strainId;

                // remarks is never used in the database
                // String remarks = matcher.group(4);

                InhouseTaxon taxon;
                if (strainId != 0) {
                    taxon = dbService.loadTaxonByInhouseId(InhouseTaxon.TAXONOMY_STRAIN, strainId);
                } else {
                    taxon = dbService.loadTaxonByInhouseId(InhouseTaxon.TAXONOMY_SPECIES, speciesId);
                }
                if (taxon == null) {
                    reject(writer, "organismId",
                            String.format("speciesId=%d strainId=%d line=%s", speciesId, strainId, line));
                } else {
                    taxon.setOrganismId(organismId);
                    dbService.save(taxon);
                }
            } else {
                reject(writer, "organismId", line);
            }
        }
        reader.close();
        writer.close();
    }

    private void importTaxonomyClasses() throws Exception {
        System.out.println("Importing taxonomic classes");
        InhouseDbService dbService = inhouseDB.getInhouseDbService();

        // classId;className;parentName
        Pattern pattern = Pattern.compile("^(\\d+);(.*);(.*)$");
        BufferedReader reader = new BufferedReader(new FileReader(this.inhouseDB.getConfigString(INPUT_TAXONOMY_CLASSES)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(TAXONOMY_REJECT_FILE)));
        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int classId = Integer.parseInt(matcher.group(1));
                String name = matcher.group(2);
                Matcher nameMatcher = quotePattern.matcher(name);
                if (nameMatcher.matches()) {
                    name = nameMatcher.group(1);
                }
                String parent = matcher.group(3);

                InhouseTaxon taxon = new InhouseTaxon()
                        .setLevel(InhouseTaxon.TAXONOMY_CLASS)
                        .setInhouseId(classId)
                        .setParent(parent)
                        .setName(name);
                dbService.save(taxon);
            } else {
                reject(writer, "classes", line);
            }
        }
        reader.close();
        writer.close();
    }

    private void importTaxonomyFamilies() throws Exception {
        System.out.println("Importing taxonomic families");
        InhouseDbService dbService = inhouseDB.getInhouseDbService();

        // FamilyId, RefClassId, FamilyName
        Pattern pattern = Pattern.compile("^(\\d+);(\\d+);(.*)$");
        BufferedReader reader = new BufferedReader(new FileReader(this.inhouseDB.getConfigString(INPUT_TAXONOMY_FAMILIES)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(TAXONOMY_REJECT_FILE)));
        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int familyId = Integer.parseInt(matcher.group(1));
                int classId = Integer.parseInt(matcher.group(2));
                String name = matcher.group(3);
                Matcher nameMatcher = quotePattern.matcher(name);
                if (nameMatcher.matches()) {
                    name = nameMatcher.group(1);
                }

//                int parentId = this.inhouseDB.loadRefId(sql, classId, TAXONOMY_CLASS_REF);
//                int unknownParentId = -1;
                InhouseTaxon taxon = new InhouseTaxon()
                        .setInhouseId(familyId)
                        .setInhouseParentId(classId)
                        .setLevel(InhouseTaxon.TAXONOMY_FAMILY)
                        .setName(name);
                dbService.save(taxon);

            } else {
                reject(writer, "families", line);
            }
        }
        reader.close();
        writer.close();
    }

    private void importTaxonomySpecies() throws Exception {
        System.out.println("Importing taxonomic species");
        InhouseDbService dbService = inhouseDB.getInhouseDbService();

        // SpeciesId, FamilyId
        Pattern pattern = Pattern.compile("^([0-9]+);([0-9]+)$");
        BufferedReader reader = new BufferedReader(new FileReader(this.inhouseDB.getConfigString(INPUT_TAXONOMY_SPECIES)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(TAXONOMY_REJECT_FILE)));
        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int speciesId = Integer.parseInt(matcher.group(1));
                int familyId = Integer.parseInt(matcher.group(2));

                InhouseTaxon species = new InhouseTaxon()
                        .setInhouseId(speciesId)
                        .setLevel(InhouseTaxon.TAXONOMY_SPECIES)
                        .setInhouseParentId(familyId);
                dbService.save(species);
            } else {
                reject(writer, "species", line);
            }
        }
        reader.close();
        writer.close();
    }

    private void importTaxonomySpeciesSynonyms() throws Exception {
        System.out.println("Reading species synonyms");
        InhouseDbService dbService = inhouseDB.getInhouseDbService();

        Map<Integer, Set<String>> allSynonyms = new HashMap<>();
        // synonymId, speciesId, synonym, prio flag
        Pattern pattern = Pattern.compile("^([0-9]+);([0-9]+);(.*);[NYny]$");

        BufferedReader reader = new BufferedReader(new FileReader(this.inhouseDB.getConfigString(INPUT_TAXONOMY_SPECIES_SYNONYMS)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(TAXONOMY_REJECT_FILE)));
        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                Integer speciesId = Integer.valueOf(matcher.group(2));
                String name = matcher.group(3);
                Matcher nameMatcher = quotePattern.matcher(name);
                if (nameMatcher.matches()) {
                    name = nameMatcher.group(1);
                }

                InhouseSynonym synonym = new InhouseSynonym()
                        .setInhouseId(speciesId)
                        .setType(InhouseSynonym.SYNONYM_ORGANISM)
                        .setSynonym(name);
                dbService.save(synonym);
            } else {
                reject(writer, "synonyms", line);
            }
        }
        reader.close();
        writer.close();
    }

    private void importTaxonomyStrains() throws Exception {
        System.out.println("Importing strains");
        InhouseDbService dbService = inhouseDB.getInhouseDbService();

        Pattern pattern = Pattern.compile("^([0-9]+);([0-9]+);(.*)$");
        BufferedReader reader = new BufferedReader(new FileReader(this.inhouseDB.getConfigString(INPUT_TAXONOMY_STRAINS)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inhouseDB.getConfigString(TAXONOMY_REJECT_FILE)));
        reader.readLine(); // discard header
        while (reader.ready()) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int inhouseId = Integer.parseInt(matcher.group(1));
                int inhouseParentId = Integer.parseInt(matcher.group(2));
                String name = matcher.group(3);
                Matcher nameMatcher = quotePattern.matcher(name);
                if (nameMatcher.matches()) {
                    name = nameMatcher.group(1);
                }

                InhouseTaxon taxon = new InhouseTaxon()
                        .setLevel(InhouseTaxon.TAXONOMY_STRAIN)
                        .setInhouseId(inhouseId)
                        .setInhouseParentId(inhouseParentId)
                        .setName(name);
                dbService.save(taxon);
            } else {
                reject(writer, "strains", line);
            }
        }
        reader.close();
        writer.close();
    }

}
