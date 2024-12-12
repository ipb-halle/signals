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
package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.DateRangeParser;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.attachment.AttachmentManager;
import de.ipb_halle.signals.attribute.AttributeManager;
import de.ipb_halle.signals.inventory.LocationEntity;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

public class SignalsEntityConfig {

    private Logger logger;
    private AttributeManager attributeManager;
    private AttachmentManager attachmentManager;
    private SignalsEntityManager signalsEntityManager;
    private SignalsConfig signalsConfig;
    private RuntimeConfig runtimeConfig;
    private EntityType[] includedTypes;

    @SuppressWarnings("static-access")
    private static final Option syncEntitiesOpt = Option.builder("eS")
            .longOpt("entitiesSync")
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
                    "after START (and optionally before END) are fetched.\n" +
                    "Example: -eS 2020-01-01:2022-12-31")
            .build();

    @SuppressWarnings("static-access")
    private static final Option dumpEntitiesOpt = Option.builder("ed")
            .longOpt("entitiesDump")
            .hasArgs()
            .argName("=all | =START[:END]")
            .valueSeparator(':')
            .optionalArg(true)
            .desc("\nProduce a database dump of SignalsEntities having the specified. " +
                    "modification time. By default, entities from the last week are dumped, otherwise " +
                    "an interval or all entities can be dumped.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option includedTypesOpt = Option.builder("eIT")
            .longOpt("entityIncludedTypes")
            .hasArgs()
            .argName("TYPES")
            .valueSeparator(',')
            .optionalArg(false)
            .desc("\nSpecify the entity types to be processed by options --syncEntities " +
                    "or --dumpEntities. The default is to include experiment, journal, asset, " +
                    "assetType, location, batch, container, sample, text.")
            .build();

    /**
     * constructor
     *
     * @param config               SignalsConfig resource
     * @param runtimeConfig        runtime configuration, as defined by command line options.
     * @param attributeManager     management class for attributes
     * @param signalsEntityManager management class for signals entities
     */
    public SignalsEntityConfig(SignalsConfig config, RuntimeConfig runtimeConfig,
                               AttributeManager attributeManager, SignalsEntityManager signalsEntityManager) {
        this.logger = LoggerFactory.getLogger(SignalsEntityConfig.class);
        this.attributeManager = attributeManager;
        this.signalsEntityManager = signalsEntityManager;
        this.signalsConfig = config;
        this.runtimeConfig = runtimeConfig;
        setupDefaultIncludedTypes();
    }


    private void setupDefaultIncludedTypes() {
        /**
         * default included types must not include attribute,
         * because attributes cannot be fetched together with other entities
         */
        includedTypes = new EntityType[]{
                EntityType.valueOf("asset"),
                EntityType.valueOf("assetType"),
                EntityType.valueOf("batch"),
                EntityType.valueOf("container"),
                EntityType.valueOf("experiment"),
                EntityType.valueOf("journal"),
                EntityType.valueOf(LocationEntity.ENTITY_TYPE_LOCATION),
                EntityType.valueOf("sample"),
                EntityType.valueOf("text")
        };
    }

    private void parseIncludedTypes(String[] args) {
        includedTypes = new EntityType[args.length];
        for (int i = 0; i < args.length; i++) {
            includedTypes[i] = EntityType.valueOf(args[i]);
        }
    }

    public void manageEntities(Date[] dateRange) {
        logger.info("""

                        ******************************************************
                        *
                        * Manage Signals Attributes and Entities
                        * {} / {}
                        * From: {}   to: {}
                        *
                        ******************************************************
                        """, signalsConfig.getSnbInstanceName(), new Date().toString(),
                dateRange[0], dateRange[1]);

        attributeManager.manageAttributes();

        signalsEntityManager.fetchSnbEntities(dateRange,
                includedTypes,
                runtimeConfig);
    }

    public void dumpEntities(Date[] dateRange) {
        signalsEntityManager.listEntities(dateRange, includedTypes);
    }

    public static void registerOptions(Options options) {
        options.addOption(includedTypesOpt);
        options.addOption(syncEntitiesOpt);
        options.addOption(dumpEntitiesOpt);
    }

    public static void processCommandLine(CommandLine cmdline, Options options, Signals signals)
            throws MissingArgumentException, MissingOptionException, UnrecognizedOptionException, ParseException {

        if (cmdline.hasOption(includedTypesOpt.getOpt())) {
            String[] types = cmdline.getOptionValues(includedTypesOpt.getOpt());
            signals.getSignalsEntityConfig().parseIncludedTypes(types);
        }

        if (cmdline.hasOption(syncEntitiesOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(syncEntitiesOpt.getOpt());
            signals.manageEntities(DateRangeParser.parseDateRange(dateRangeArgs));
        }

        if (cmdline.hasOption(dumpEntitiesOpt.getOpt())) {
            String[] dateRangeArgs = cmdline.getOptionValues(dumpEntitiesOpt.getOpt());
            signals.dumpEntities(DateRangeParser.parseDateRange(dateRangeArgs));
        }
    }
}
