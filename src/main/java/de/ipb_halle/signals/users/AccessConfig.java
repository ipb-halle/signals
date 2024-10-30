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
package de.ipb_halle.signals.users;


import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.materials.LibraryManager;
import de.ipb_halle.signals.materials.MaterialsConfig;
import java.util.Date;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessConfig {

    @SuppressWarnings("static-access")
    private static final Option accessMgrOpt = Option.builder("aM")
            .longOpt("acessManagement")
            .desc("\nPerform user,  group and role management")
            .build();

    private AccessManager accessManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    private Logger logger;

    public AccessConfig(SignalsConfig signalsConfig, RuntimeConfig runtimeConfig, AccessManager accessManager) {
        this.logger = LoggerFactory.getLogger(AccessConfig.class);
        this.accessManager = accessManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageAccess() {
        logger.info("""

                ******************************************************
                *
                * Manage Users
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());
        accessManager.manageAccess(runtimeConfig);
    }

    public static void registerOptions(Options options) {
        options.addOption(accessMgrOpt);
    }

    /**
     * process the command line and perform requested jobs.
     *
     * @param cmdline the parsed command line
     * @param options the defined options
     * @param signals the current Signals instance
     */
    public static void processCommandLine(CommandLine cmdline, Options options, Signals signals)
            throws MissingArgumentException, MissingOptionException, UnrecognizedOptionException {

        if (cmdline.hasOption(accessMgrOpt.getOpt())) {
            signals.manageAccess();
        }


    }
}
