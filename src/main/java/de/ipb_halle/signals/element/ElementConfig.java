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
package de.ipb_halle.signals.element;


import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

public class ElementConfig {

/*
 * NOTE: this option masks the --entitiesSync option in file
 *       ../entity/SignalsEntityConfig.java
 *
    @SuppressWarnings("static-access")
    private static final Option elementsSyncOpt = Option.builder("eS")
            .longOpt("elementsSync")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nSynchronize the constituting elements of experiments, samples, etc. from SNB to DB.")
            .build();
*/
    private ElementManager elementManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    private Logger logger;

    public ElementConfig(SignalsConfig signalsConfig, RuntimeConfig runtimeConfig, ElementManager elementManager) {
        this.logger = LoggerFactory.getLogger(ElementConfig.class);
        this.elementManager = elementManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageElements(Date[] dateRange) {
        logger.info("""

                ******************************************************
                *
                * Manage Elements
                * {} / v{} / {}
                *
                ******************************************************
                """,
                signalsConfig.getSnbInstanceName(),
                runtimeConfig.projectVersion,
                new Date().toString());

        elementManager.manageElements(runtimeConfig, dateRange);
    }


    public static void registerOptions(Options options) {
//      options.addOption(elementsSyncOpt);
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

/*
        if (cmdline.hasOption(elementsSyncOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(elementsSyncOpt.getOpt());
            signals.manageElements(DateRangeParser.parseDateRange(dateRangeArgs));
        }
*/
    }
}
