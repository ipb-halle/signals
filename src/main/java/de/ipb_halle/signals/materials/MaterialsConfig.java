/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.materials;


import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaterialsConfig {

    @SuppressWarnings("static-access")
    private static final Option materialsSyncOpt = Option.builder("mS")
            .longOpt("materialsSync")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nSynchronize materials libraries and materials from SNB to DB.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option materialImportOpt = Option.builder("mi")
            .longOpt("materialImport")
            .hasArgs()
            .argName("ENTITY_ID")
            .optionalArg(false)
            .desc("\nImport a single material from DB into SNB (i.e. create new material in SNB).")
            .build();

    private MaterialsManager materialsManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    private Logger logger;

    public MaterialsConfig(SignalsConfig signalsConfig, RuntimeConfig runtimeConfig, MaterialsManager materialsManager) {
        this.logger = LoggerFactory.getLogger(MaterialsConfig.class);
        this.materialsManager = materialsManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageMaterials(Date[] dateRange) {
        logger.info("""

                ******************************************************
                *
                * Manage Materials
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());

        materialsManager.manageLibraries(runtimeConfig);
        materialsManager.manageMaterials(dateRange);
    }

    public void importMaterials(String id) {
        logger.info("""

                ******************************************************
                *
                * Import single Material
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), id);
        materialsManager.importMaterial(runtimeConfig, id);
    }

    public static void registerOptions(Options options) {
        options.addOption(materialsSyncOpt);
        options.addOption(materialImportOpt);
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

        if (cmdline.hasOption(materialsSyncOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(materialsSyncOpt.getOpt());
            signals.getMaterialsConfig().manageMaterials(DateRangeParser.parseDateRange(dateRangeArgs));
        }

        if (cmdline.hasOption(materialImportOpt.getOpt())) {
            signals.getMaterialsConfig().importMaterials(
                    cmdline.getOptionValue(materialImportOpt.getOpt()));
        }
    }
}
