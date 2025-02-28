/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals;


import org.apache.commons.cli.*;

public class RuntimeConfig {

    @SuppressWarnings("static-access")
    private static final Option dryRunOpt = Option.builder("n")
            .longOpt("dry-run")
            .desc("\nDry run - don't modify anything (includes --noUpdateSNB and --noUpdateFromLDAP).")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noMailOpt = Option.builder("m")
            .longOpt("noMail")
            .desc("\nDo not send any reports by email")
            .build();

    @SuppressWarnings("static-access")
    private static final Option discoverOpt = Option.builder("discover")
            .longOpt("allow-discover")
            .desc("\nAllow discovery of new types (DynEnums). Otherwise the program is aborted upon discovery of an unknown type.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noUpdateSnbOpt = Option.builder("noSNB")
            .longOpt("noUpdateSNB")
            .desc("\nDo not perform updates to Signals Notebook")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noUpdateFromLdapOpt = Option.builder("noLDAP")
            .longOpt("noUpdateFromLDAP")
            .desc("\nDo not perform updates from LDAP")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noSyncDbFromSNBOpt = Option.builder("noSyncSNB")
            .longOpt("noSyncDbFromSNB")
            .desc("\nDo not synchronize database from Signals Notebook")
            .build();

    @SuppressWarnings("static-access")
    private static final Option debugOpt = Option.builder("d")
            .longOpt("debug")
            .hasArg()
            .argName("LEVEL")
            .desc("\nSet log level to the selected LEVEL (one of FATAL, ERROR, WARN, INFO, DEBUG, TRACE)")
            .build();

    @SuppressWarnings("static-access")
    private static final Option trustStoreOpt = Option.builder("ts")
            .longOpt("trustStore")
            .hasArg()
            .argName("FILE")
            .desc("\nSet the truststore for startSSL. The trustStore should contain certificates for both: LDAP and SNB API.")
            .build();


    /**
     * Decides, whether an update of the relational database
     * should be made. If set to false, no inserts, updates
     * or deletes should be made, effectively putting the
     * database into read only mode.
     *
     * Default: true
     */
    public boolean updateDb;

    /**
     * Selects, whether updates to Signals notebook will be
     * made. If set to false, no POST or PATCH calls will
     * be made, restricting the interaction to read access.
     *
     * Default: true
     */
    public boolean updateSNB;

    /**
     * Perform database update from LDAP server. Data on the
     * LDAP server will never be updated (always read only).
     *
     * Default: true
     */
    public boolean updateFromLdap;

    /**
     * Synchronize user and group database from Signals Notebook.
     * If set to false, changes in the database (obtained e.g. from LDAP)
     * will be propagated to Signals Notebook.
     *
     * Default: true
     */
    public boolean syncDbFromSNB;

    /**
     * If true, do not send a mail on the most synchronization results
     *
     * Default: false
     */
    public boolean noMail;

    public RuntimeConfig() {
        this(true, true, true,false, true);
    }

    public RuntimeConfig(boolean db, boolean snb, boolean ldap, boolean noMail, boolean syncSNB) {
        this.updateDb = db;
        this.updateSNB = snb;
        this.updateFromLdap = ldap;
        this.syncDbFromSNB = syncSNB;
        this.noMail = noMail;
    }

    public static void registerOptions(Options options) {
        options.addOption(debugOpt);
        options.addOption(discoverOpt);
        options.addOption(dryRunOpt);
        options.addOption(noMailOpt);
        options.addOption(noSyncDbFromSNBOpt);
        options.addOption(noUpdateSnbOpt);
        options.addOption(noUpdateFromLdapOpt);
        options.addOption(trustStoreOpt);
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

        RuntimeConfig config = signals.getRuntimeConfig();

        if (cmdline.hasOption(dryRunOpt.getOpt())) {
            config.updateDb = false;
            config.updateSNB = false;
            config.updateFromLdap = false;
        }

        if (cmdline.hasOption(noMailOpt.getOpt())) {
            config.noMail = true;
        }

        if (cmdline.hasOption(noUpdateSnbOpt.getOpt())) {
            config.updateSNB = false;
        }

        if (cmdline.hasOption(noUpdateFromLdapOpt.getOpt())) {
            config.updateFromLdap = false;
        }

        if (cmdline.hasOption(noSyncDbFromSNBOpt.getOpt())) {
            config.syncDbFromSNB = false;
        }

        if (cmdline.hasOption(trustStoreOpt.getOpt())) {
            System.setProperty("javax.net.ssl.trustStore", cmdline.getOptionValue(trustStoreOpt.getOpt()));
        }


        if (cmdline.hasOption(debugOpt.getOpt())) {
            if (!signals.getLogConfig().setLogLevel(cmdline.getOptionValue(debugOpt.getOpt()))) {
                signals.printHelp("ERROR: invalid log level", options);
            }
        }

        if (cmdline.hasOption(discoverOpt.getOpt())) {
            signals.getDynEnumMgr().allowEnumDiscovery();
        }
    }
}
