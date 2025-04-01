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

package de.ipb_halle.signals.experiments;

import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.Date;

public class ExperimentConfig {
    private ExperimentManager experimentManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    public static final Logger logger = LogManager.getLogger(ExperimentConfig.class);

    @SuppressWarnings("static-access")
    private static final Option experimentSyncOpt = Option.builder("expS")
            .longOpt("experimentSync")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nSynchronize experiments from SNB to DB.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option experimentImportOpt = Option.builder("expI")
            .longOpt("experimentImport")
            .hasArgs()
            .argName("ENTITY_ID")
            .optionalArg(false)
            .desc("\nImport an experiment from DB into SNB (i.e. create new experiment in SNB).")
            .build();


    public ExperimentConfig(ExperimentManager experimentManager, RuntimeConfig runtimeConfig, SignalsConfig signalsConfig) {
        this.experimentManager = experimentManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageExperiments(Date[] dateRange) {
        logger.info("""

                ******************************************************
                *
                * Manage Experiments
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());

        experimentManager.manageExperiments(dateRange);
    }

    public void importExperiment(String id) {
        logger.info("""

                ******************************************************
                *
                * Import single Experiment
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), id);
        experimentManager.importExperiment(runtimeConfig, id);
    }

    public static void registerOptions(Options options) {
        options.addOption(experimentSyncOpt);
        options.addOption(experimentImportOpt);
    }

    public static void processCommandLine(CommandLine cmdLine, Options options, Signals signals) throws ParseException {
        if (cmdLine.hasOption(experimentSyncOpt.getOpt())) {
            String[] dateRangeArgs = cmdLine.getOptionValues(experimentSyncOpt.getOpt());
            signals.manageExperiments(DateRangeParser.parseDateRange(dateRangeArgs));
        }
        if (cmdLine.hasOption(experimentImportOpt.getOpt())) {
            signals.getExperimentConfig().importExperiment(cmdLine.getOptionValue(experimentImportOpt.getOpt()));
        }

    }
}
