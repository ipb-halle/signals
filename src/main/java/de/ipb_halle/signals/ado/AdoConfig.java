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

package de.ipb_halle.signals.ado;

import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.Option;

import java.text.ParseException;
import java.util.Date;

public class AdoConfig {
    private AdoManager adoManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    private final static Logger logger = LogManager.getLogger(AdoConfig.class);

    @SuppressWarnings("static-access")
    private static final Option adoSyncOpt = Option.builder("adoS")
            .longOpt("adosSync")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nSynchronize ADOs from Signals Notebook to local DB.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option adoImportOpt = Option.builder("adoI")
            .longOpt("adoImport")
            .hasArgs()
            .argName("ENTITY_ID")
            .optionalArg(false)
            .desc("\n Import a single ADO object into Signals Notebook.")
            .build();

    public AdoConfig(AdoManager adoManager, RuntimeConfig runtimeConfig, SignalsConfig signalsConfig) {
        this.adoManager = adoManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageAdos(Date[] dateRange) {
        logger.info("""

                ******************************************************
                *
                * Manage Ados
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());

        adoManager.manageAdos(dateRange);
    }

    public void importAdos(String id) {
        logger.info("""

                ******************************************************
                *
                * Import single Ado
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), id);
        adoManager.importAdo(id);
    }

    public static void registerOptions(Options options) {
        options.addOption(adoSyncOpt);
        options.addOption(adoImportOpt);
    }

    public static void processCommandLine(CommandLine cmdLine, Options options, Signals signals) throws ParseException {
        if (cmdLine.hasOption(adoSyncOpt.getOpt())) {
            String[] datRangeArgs = cmdLine.getOptionValues(adoSyncOpt.getOpt());
            signals.manageAdos(DateRangeParser.parseDateRange(datRangeArgs));
        }
        if (cmdLine.hasOption(adoImportOpt.getOpt())) {
            signals.getAdoConfig().importAdos(cmdLine.getOptionValue(adoImportOpt.getOpt()));
        }
    }
}
