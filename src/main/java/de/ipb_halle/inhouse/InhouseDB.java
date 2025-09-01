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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.ado.AdoManager;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.experiments.ExperimentDbService;
import de.ipb_halle.signals.experiments.ExperimentRestService;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.inventory.ContainerRestService;
import de.ipb_halle.signals.inventory.LocationDbService;
import de.ipb_halle.signals.inventory.LocationRestService;
import de.ipb_halle.signals.materials.LibraryDbService;
import de.ipb_halle.signals.materials.MaterialRestService;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.sample.SampleRestService;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.sql.SQLException;
import java.text.ParseException;

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

    private final Logger logger = LogManager.getLogger(InhouseDB.class);

    @Inject
    private InhouseDbService inhouseDbService;

    @Inject
    private LibraryDbService libraryDbService;

    @Inject
    private MaterialRestService materialRestService;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private ExperimentRestService experimentRestService;

    @Inject
    private ExperimentDbService experimentDbService;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private SampleRestService sampleRestService;

    @Inject
    private SignalsEntityRestService signalsEntityRestService;

    @Inject
    private AdoManager adoManager;

    @Inject
    private ContainerRestService containerRestService;

    @Inject
    private LocationDbService locationDbService;

    @Inject
    private LocationRestService locationRestService;

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

    public FieldDbService getFieldDbService() {
        return fieldDbService;
    }

    public ExperimentRestService getExperimentRestService() {
        return experimentRestService;
    }

    public ExperimentDbService getExperimentDbService() {
        return experimentDbService;
    }

    public DynEnumManager getDynEnumManager() {
        return dynEnumManager;
    }

    public SampleRestService getSampleRestService() {
        return sampleRestService;
    }

    public SignalsEntityRestService getSignalsEntityRestService() {
        return signalsEntityRestService;
    }

    public AdoManager getAdoManager() {
        return adoManager;
    }

    public ContainerRestService getContainerRestService() {
        return containerRestService;
    }

    public LocationDbService getLocationDbService() {
        return locationDbService;
    }

    public LocationRestService getLocationRestService() {
        return locationRestService;
    }

    private void importData(String configFile) throws Exception {
        readConfig(configFile);

        logger.info("STARTING IMPORT OF COMPOUNDS");
        Compounds compounds = new Compounds(this);

        logger.info("STARTING IMPORT OF EXPERIMENTS");
        Experiments experiments = new Experiments(this);
//
//        logger.info("STARTING IMPORT OF Table Correlations between structure and organism");
        Correlation correlation = new Correlation(this);

        logger.info("STARTING IMPORT OF SAMPLES");
        Samples samples = new Samples(this);

        logger.info("STARTING IMPORT ORGANISMS");
        Organisms organisms = new Organisms(this);

        logger.info("STARTING IMPORT EXTRACTS");
        Extracts extracts = new Extracts(this);

//        Taxonomy taxonomy = new Taxonomy(this);

        //  compounds.importData();
        experiments.importData();
        // correlation.importData();

//        taxonomy.importData();
        //     samples.importData();

        // organisms.importOrganisms();
        //extracts.importExtracts();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
