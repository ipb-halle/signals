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
package de.ipb_halle.signals.inventory;


import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import org.apache.commons.cli.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

public class InventoryConfig {

    @SuppressWarnings("static-access")
    private static final Option inventorySyncOpt = Option.builder("iS")
            .longOpt("inventorySync")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("""
                    \nSynchronize location types, container types, locations and containers
                    from SNB to DB. Date interval can be specified optionally, see --entitiesSync for details.""")
            .build();

    @SuppressWarnings("static-access")
    private static final Option locationImportOpt = Option.builder("li")
            .longOpt("locationImport")
            .hasArgs()
            .argName("ENTITY_ID")
            .optionalArg(false)
            .desc("\nImport a single location from DB into SNB (i.e. create new location in SNB).")
            .build();

    @SuppressWarnings("static-access")
    private static final Option containerImportOpt = Option.builder("ci")
            .longOpt("containerImport")
            .hasArgs()
            .argName("ENTITY_ID")
            .optionalArg(false)
            .desc("\nImport a single container from DB into SNB (i.e. create new container in SNB).")
            .build();

    private InventoryManager inventoryManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    private Logger logger;


    public InventoryConfig(SignalsConfig signalsConfig, RuntimeConfig runtimeConfig, InventoryManager inventoryManager) {
        this.logger = LoggerFactory.getLogger(InventoryConfig.class);
        this.inventoryManager = inventoryManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageInventory(Date[] dateRange) {
        logger.info("""

                ******************************************************
                *
                * Manage Inventory
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());
        inventoryManager.manageInventory(dateRange);
    }

    public void importLocations(String id) {
        logger.info("""

                ******************************************************
                *
                * Import single Location
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), id);
        inventoryManager.importLocation(runtimeConfig, id);
    }

    public void importContainers(String id) {
        logger.info("""

                ******************************************************
                *
                * Import single Container
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), id);
        inventoryManager.importContainer(runtimeConfig, id);
    }

    public static void registerOptions(Options options) {
        options.addOption(inventorySyncOpt);
        options.addOption(locationImportOpt);
        options.addOption(containerImportOpt);
    }

    /**
     * process the command line and perform requested jobs.
     *
     * @param cmdline the parsed command line
     * @param options the defined options
     * @param signals the current Signals instance
     */
    public static void processCommandLine(CommandLine cmdline, Options options, Signals signals) throws ParseException {

        if (cmdline.hasOption(inventorySyncOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(inventorySyncOpt.getOpt());
            signals.manageInventory(DateRangeParser.parseDateRange(dateRangeArgs));
        }

        if (cmdline.hasOption(locationImportOpt.getOpt())) {
            signals.getInventoryConfig().importLocations(
                    cmdline.getOptionValue(locationImportOpt.getOpt()));
        }

        if (cmdline.hasOption(containerImportOpt.getOpt())) {
            signals.getInventoryConfig().importContainers(
                    cmdline.getOptionValue(containerImportOpt.getOpt()));
        }
    }
}
