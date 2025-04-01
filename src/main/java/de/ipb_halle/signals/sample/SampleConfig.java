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

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.Date;

public class SampleConfig {
    @SuppressWarnings("static-access")
    private static final Option samplesSyncOpt = Option.builder("saS")
            .longOpt("sampleSync")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nSynchronize samples from SNB to DB.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option sampleImportOpt = Option.builder("si")
            .longOpt("sampleImport")
            .hasArgs()
            .argName("ENTITY_ID")
            .optionalArg(false)
            .desc("\nImport a single sample from DB into SNB (i.e. create new sample in SNB).")
            .build();

    private SampleManager sampleManager;
    private RuntimeConfig runtimeConfig;
    private SignalsConfig signalsConfig;
    private Logger logger;

    public SampleConfig(SampleManager sampleManager, RuntimeConfig runtimeConfig, SignalsConfig signalsConfig) {
        this.logger = LogManager.getLogger(SampleConfig.class);
        this.sampleManager = sampleManager;
        this.runtimeConfig = runtimeConfig;
        this.signalsConfig = signalsConfig;
    }

    public void manageSamples(Date[] dateRange) {
        logger.info("""

                ******************************************************
                *
                * Manage Samples
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());

        sampleManager.manageSamples(dateRange);
    }

    public void importSample(String id) {
        logger.info("""

                ******************************************************
                *
                * Import single Sample
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), id);
        sampleManager.importSample(runtimeConfig, id);
    }

    public static void registerOptions(Options options) {
        options.addOption(samplesSyncOpt);
        options.addOption(sampleImportOpt);
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

        if (cmdline.hasOption(samplesSyncOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(samplesSyncOpt.getOpt());
            signals.manageSamples(DateRangeParser.parseDateRange(dateRangeArgs));
        }

        if (cmdline.hasOption(sampleImportOpt.getOpt())) {
            signals.getSamplesConfig().importSample(
                    cmdline.getOptionValue(sampleImportOpt.getOpt()));
        }
    }
}
