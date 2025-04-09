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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.materials.Library;
import de.ipb_halle.signals.materials.LibraryDbService;
import de.ipb_halle.signals.materials.MaterialRestService;
import de.ipb_halle.signals.rest.RestHelper;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

/*
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.structure.MoleculeEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlInsertBuilder;
*/

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Migration tool for the InhouseDB
 * This is work in progress.
 * <p>
 * DO NOT FORGET TO DEFINE INDEX TYPES Mol_ID and IPBCode!
 * <p>
 * Example config file:
 * <pre>
 *  {
 *  ACLIST_ID:      1,
 *  DATABASE_URL:   "jdbc:postgresql://localhost:5432/lbac?charSet=UTF-8&user=lbac&password=lbac",
 *
 *  INITIAL_SQL: ["DROP ...", "INSERT ...", ...],
 *  INPUT_STRUCTURE_NAMES: "/dataPOOL/fblocal/inhouse/tblCompoundSynonym_20140325.txt",
 *  INPUT_STRUCTURES:      "/dataPOOL/fblocal/inhouse/Structure_20140325.SDF",
 *
 *  ... lots of other config ...
 *
 *  MOLECULE_MATERIAL_TYPE_ID: 1,
 *  OWNER_ID:       1,
 *  PROJECT_ID:     1
 * }
 * </pre>
 * <p>
 * After mvn test-compile, this tool will be usually run from the REPO/ui direcory using the following command:
 *
 * <pre>
 * java -cp "target/classes:target/test-classes:target/ui-1.3.0/WEB-INF/lib/*" de.ipb_halle.migration.InhouseDB
 * </pre>
 *
 * @author fbroda
 */
@Local
public class InhouseDB {

    @Inject
    private InhouseDbService inhouseDbService;

    @Inject
    private LibraryDbService libraryDbService;

    @Inject
    private MaterialRestService materialRestService;

    @SuppressWarnings("static-access")
    private static final Option inhouseOpt = Option.builder("inhouse")
            .longOpt("importInhouse")
            .hasArgs()
            .argName("inhouseConfig")
            .desc("\nImport data from InhouseDB.")
            .build();

    private JsonObject jsonConfig;

    public Integer getConfigInt(String path) {
        return RestHelper.getPrimitiveFromPath(jsonConfig, path).getAsInt();
    }

    public String getConfigString(String path) {
        return RestHelper.getPrimitiveFromPath(jsonConfig, path).getAsString();
    }

    public InhouseDbService getInhouseDbService() {
        return inhouseDbService;
    }

    public LibraryDbService getLibraryDbService() {
        return libraryDbService;
    }

    public MaterialRestService getMaterialRestService() {
        return materialRestService;
    }

    private void importData(String configFile) throws Exception {
        readConfig(configFile);
        Compounds compounds = new Compounds(this);
        Experiments experiments = new Experiments(this);
        Taxonomy taxonomy = new Taxonomy(this);
        Correlation correlation = new Correlation(this);
        Samples samples = new Samples(this);

//        compounds.importData(this);
//        experiments.importData(this);

//        taxonomy.importData();
          correlation.importData();
//        samples.importData();
    }


    private void readConfig(String fileName) throws Exception {
        JsonElement element = JsonParser.parseReader(
                new FileReader(fileName));
        if (!element.isJsonObject()) {
            throw new Exception("readConfig() could not parse Json object");
        }
        this.jsonConfig = element.getAsJsonObject();
    }

    public void saveTriple(String sql, Integer id, Integer other, String value) throws SQLException {
/*
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setInt(1, id);
        statement.setInt(2, other);
        statement.setString(3, value);
        statement.execute();
*/
    }

    public static void registerOptions(Options options) {
        options.addOption(inhouseOpt);
    }

    /**
     * process the command line and perform requested jobs.
     *
     * @param cmdline the parsed command line
     * @param options the defined options
     * @param signals the current Signals instance
     */
    public static void processCommandLine(CommandLine cmdline, Options options, Signals signals)
            throws MissingArgumentException, MissingOptionException, UnrecognizedOptionException, ParseException {

        if (cmdline.hasOption(inhouseOpt.getOpt())) {
            String configFile = cmdline.getOptionValue(inhouseOpt.getOpt());
            try {
                signals.getInhouseDB().importData(configFile);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


}
