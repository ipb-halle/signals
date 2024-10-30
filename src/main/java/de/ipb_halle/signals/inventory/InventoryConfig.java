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


import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.materials.LibraryManager;
import de.ipb_halle.signals.materials.MaterialsConfig;
import org.apache.commons.cli.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class InventoryConfig {

    @SuppressWarnings("static-access")
    private static final Option locationSyncOpt = Option.builder("lS")
            .longOpt("locationSync")
            .desc("\nSynchronize location types and locations from SNB to DB.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option containerSyncOpt = Option.builder("cS")
            .longOpt("containerSync")
            .desc("\nSynchronize container types and containers values from SNB to DB.")
            .build();

    public InventoryConfig() {
    }
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

    public void manageInventory() {
        logger.info("""

                ******************************************************
                *
                * Manage Inventory
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());
    }

    public static void registerOptions(Options options) {
        options.addOption(locationSyncOpt);
        options.addOption(containerSyncOpt);
    }

    /**
     * process the command line and perform requested jobs.
     *
     * @param cmdline the parsed command line
     * @param options the defined options
     * @param signals the current Signals instance
     */
    public static void processCommandLine(CommandLine cmdline, Options options, Signals signals) {

        if (cmdline.hasOption(locationSyncOpt.getOpt())) {
            System.out.println("***************** Location Sync *********************");
        }

        if (cmdline.hasOption(containerSyncOpt.getOpt())) {
            System.out.println("***************** Container Sync ********************");
        }
    }
}
