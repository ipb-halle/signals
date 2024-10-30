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
 */package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.UpdateConfig;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SignalsEntityConfig {

    private Logger logger;
    private SignalsEntityManager signalsEntityManager;
    private SignalsConfig signalsConfig;
    private UpdateConfig updateConfig;
    private EntityType[] includedTypes;

    @SuppressWarnings("static-access")
    private static final Option syncEntitiesOpt = Option.builder("sE")
            .longOpt("syncEntities")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nInitiates a data import process from a remote REST API of signals notebook. " +
                    "The process fetches a list of entities from the specified API endpoint, " +
                    "maps the data to the 'SignalsEntity' class, and persists it to the PostgreSQL " +
                    "database using Hibernate.\n " +
                    "If no argument is given, the call fetches all entities, which were modified " +
                    "within the last week. If 'all' is given, all entities are fetched. If a START " +
                    "and optionally an END date are given, only entities with modification dates " +
                    "after START (and optionally before END) are fetched.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option dumpEntitiesOpt = Option.builder("dE")
            .longOpt("dumpEntities")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nProduce a database dump of SignalsEntities having the specified. " +
                    "modification time. By default, entities from the last week are dumped, otherwise " +
                    "an interval or all entities can be dumped.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option includedTypesOpt = Option.builder("iET")
            .longOpt("includedEntityTypes")
            .hasArgs()
            .argName("TYPES")
            .valueSeparator(',')
            .optionalArg(false)
            .desc("\nSpecify the entity types to be processed by options --syncEntities " +
                    "or --dumpEntities. The default is to include experiment, journal, asset, " +
                    "assetType, location, batch, container, sample, text.")
            .build();

    public SignalsEntityConfig(SignalsConfig config, UpdateConfig updateConfig, SignalsEntityManager manager) {
        this.logger = LogManager.getLogger(SignalsEntityConfig.class);
        this.signalsEntityManager = manager;
        this.signalsConfig = config;
        this.updateConfig = updateConfig;
        setupDefaultIncludedTypes();
    }

    private void setupDefaultIncludedTypes() {
        includedTypes = new EntityType[]{
                EntityType.valueOf("experiment"),
                EntityType.valueOf("journal"),
                EntityType.valueOf("assetType"),
                EntityType.valueOf("asset"),
                EntityType.valueOf("location"),
                EntityType.valueOf("batch"),
                EntityType.valueOf("container"),
                EntityType.valueOf("sample"),
                EntityType.valueOf("text")
        };
    }

    private void parseIncludedTypes(String[] args) {
        includedTypes = new EntityType[args.length];
        for (int i=0; i<args.length; i++) {
            includedTypes[i] = EntityType.valueOf(args[i]);
        }
    }
    public void manageEntities(String[] dateRangeArgs) {
        logger.info("""

                ******************************************************
                *
                * Manage Signals Enities
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());

        try {
            Date[] dateRange = parseDateRange(dateRangeArgs);
            signalsEntityManager.fetchSnbEntities(dateRange,
                    includedTypes,
                    updateConfig);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public void dumpEntities(String[] dateRangeArgs) {
        try {
            Date[] dateRange = parseDateRange(dateRangeArgs);
            signalsEntityManager.listEntities(dateRange, includedTypes);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parser for additional arguments option start and end
     */
    private Date[] parseDateRange(String[] dateRange) throws ParseException {
        Date[] range = new Date[2];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        range[1] = new Date();

        if ((dateRange == null) || (dateRange.length == 0) || dateRange[0].isEmpty()) {
            range[0] = getOneWeekAgoDate();
            logger.info("Fetching last weeks data: {} - {}", range[0], range[1]);
            return range;
        }
        if (dateRange[0].equals("all")) {
            range[0] = new Date(0); // 1970-01-01
            logger.info("Fetching ALL data");
            return range;
        }

        range[0] = dateFormat.parse(dateRange[0]);
        if ((dateRange.length > 1) && (!dateRange[1].isEmpty())) {
            range[1] = dateFormat.parse(dateRange[1]);
        }
        logger.info("Fetching specified data range: {} - {}", range[0], range[1]);
        return range;
    }

    private Date getOneWeekAgoDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        return calendar.getTime();
    }

    public static void registerOptions(Options options) {
        options.addOption(includedTypesOpt);
        options.addOption(syncEntitiesOpt);
        options.addOption(dumpEntitiesOpt);

    }

    public static void processCommandLine(CommandLine cmdline, Options options, Signals signals)
                throws MissingArgumentException, MissingOptionException, UnrecognizedOptionException {

        if (cmdline.hasOption(includedTypesOpt.getOpt())) {
            String[] types = cmdline.getOptionValues(includedTypesOpt.getOpt());
            signals.getSignalsEntityConfig().parseIncludedTypes(types);
        }

        if (cmdline.hasOption(syncEntitiesOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(syncEntitiesOpt.getOpt());
            signals.manageEntities(dateRangeArgs);
        }

        if (cmdline.hasOption(dumpEntitiesOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(dumpEntitiesOpt.getOpt());
            signals.dumpEntities(dateRangeArgs);
        }
    }
}