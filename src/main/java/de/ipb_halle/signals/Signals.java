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

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntitiesTestCall;
import de.ipb_halle.signals.entity.SignalsEntityManager;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.materials.LibraryManager;
import de.ipb_halle.signals.users.AccessManager;
import de.ipb_halle.signals.users.LdapClient;

import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;

import javax.naming.Context;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.openejb.api.LocalClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IPB Signals client is a tool for data import and export
 * into PerkinElmer (R) Signals (TM) Notebook.
 */

@LocalClient
public class Signals {

    @Resource
    private SignalsConfig signalsConfig;

    @Inject
    private DynEnumManager dynEnumMgr;

    @Inject
    private SignalsEntityManager signalsMgr;

    @Inject
    private AccessManager accessManager;

    @Inject
    private LibraryManager libraryManager;

    @Inject
    private LdapClient ldapClient;

    @Inject
    private LogConfig logConfig;

    @Inject
    private SignalsEntitiesTestCall signalsEntitiesCall;

    @Inject
    private SignalsEntityRestService signalsEntityRestService;

    private UpdateConfig updateConfig;
    private boolean noMail;

    private Logger logger;

    @SuppressWarnings("static-access")
    private static final Option configOpt = Option.builder("c")
            .longOpt("config")
            .desc("Path to a the config file containing the Signals (tm) web token, base URL as well as database and LDAP connection information.")
            .hasArg()
            .argName("FILE")
            .required(true)
            .build();

    @SuppressWarnings("static-access")
    private static final Option helpOpt = Option.builder("h")
            .longOpt("help")
            .desc("Display the help")
            .build();

    @SuppressWarnings("static-access")
    private static final Option materialsMgrOpt = Option.builder("M")
            .longOpt("manage-materials")
            .desc("Synchronize materials libraries and materials")
            .build();

    @SuppressWarnings("static-access")
    private static final Option userMgrOpt = Option.builder("u")
            .longOpt("manage-users")
            .desc("Perform user,  group and role management")
            .build();

    @SuppressWarnings("static-access")
    private static final Option dryRunOpt = Option.builder("n")
            .longOpt("dry-run")
            .desc("Dry run - don't modify anything (includes --noUpdateSNB and --noUpdateFromLDAP).")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noMailOpt = Option.builder("m")
            .longOpt("noMail")
            .desc("Do not send any reports by email")
            .build();

    @SuppressWarnings("static-access")
    private static final Option discoverOpt = Option.builder("discover")
            .longOpt("allow-discover")
            .desc("Allow discovery of new types (DynEnums). Otherwise the program is aborted upon discovery of an unknown type.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noUpdateSnbOpt = Option.builder("noSNB")
            .longOpt("noUpdateSNB")
            .desc("Do not perform updates to Signals Notebook")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noUpdateFromLdapOpt = Option.builder("noLDAP")
            .longOpt("noUpdateFromLDAP")
            .desc("Do not perform updates from LDAP")
            .build();

    @SuppressWarnings("static-access")
    private static final Option noSyncDbFromSNBOpt = Option.builder("noSyncSNB")
            .longOpt("noSyncDbFromSNB")
            .desc("Do not synchronize database from Signals Notebook")
            .build();

    @SuppressWarnings("static-access")
    private static final Option debugOpt = Option.builder("d")
            .longOpt("debug")
            .hasArg()
            .argName("LEVEL")
            .desc("Set log level to the selected LEVEL (one of FATAL, ERROR, WARN, INFO, DEBUG, TRACE)")
            .build();

    @SuppressWarnings("static-access")
    private static final Option trustStoreOpt = Option.builder("ts")
            .longOpt("trustStore")
            .hasArg()
            .argName("FILE")
            .desc("Set the truststore for startSSL. The trustStore should contain certificates for both: LDAP and SNB API.")
            .build();

    @SuppressWarnings("static-access")
    private static final Option importSignalsEntitiesOpt = Option.builder("importE")
            .longOpt("importSignalsEntities")
            .desc("Initiates a data import process from a remote REST API of signals notebook. " +
                    "The process fetches a list of entities from the specified API endpoint, " +
                    "maps the data to the 'SignalsEntity' class, and persists it to the PostgreSQL " +
                    "database using Hibernate. ")
            .build();


    /**
     * default constructor
     */
    public Signals() {
        updateConfig = new UpdateConfig();
        noMail = false;
        logger = LoggerFactory.getLogger(Signals.class);
    }

    private void dumpSet(Set<String> set) {
        Iterator<String> iter = set.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    private void manageMaterials() {
        logger.info("""

                ******************************************************
                *
                * Manage Materials
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());

        libraryManager.manageMaterials(updateConfig);
    }

    private void manageUsers() {
        logger.info("""

                ******************************************************
                *
                * Manage Users 
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());
        accessManager.manageAccess(updateConfig, noMail);
    }

    private void signalsEntityCall() {
        logger.info("""

                ******************************************************
                *
                * signals enities call
                * {} / {}
                *
                ******************************************************
                """, signalsConfig.getSnbInstanceName(), new Date().toString());
        signalsEntitiesCall.receiveTheEntitiesFromSignals();
   //     signalsEntityRestService.doGetEntities("");
    }

    public static Signals getInstance(String fname) {
        Signals signals = null;
        try {

            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.put("openejb.configuration", fname);

            EJBContainer container = EJBContainer.createEJBContainer(properties);
            Context ctx = container.getContext();

            signals = new Signals();
            ctx.bind("inject", signals);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return signals;
    }

    public static void printHelp(String errorMessage, Options options) {
        HelpFormatter writer = new HelpFormatter();
        String usage = "java -jar <JAR_WITH_DEPENDENCIES>";
        String header = "\n\nIPB Signals Tool\n(c) 2022 Leibniz Institute of Plant Biochemistry\n\nManagement tool for your PerkinElmer(r) Signals(tm) Notebook instance.\n\nOPTIONS:\n";
        StringBuilder sb = new StringBuilder();
        if (errorMessage != null) {
            sb.append("\n");
            sb.append(errorMessage);
            sb.append("\n");
        }
        String footer = sb.toString();
        writer.printHelp(usage, header, options, footer, true);
    }

    /**
     * process the command line and perform requested jobs.
     *
     * @param argv    the command line arguments
     * @param options the defined options
     */
    public static void processCommandLine(String[] argv, Options options) {

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);



            if (cmdline.hasOption(helpOpt.getOpt())) {
                printHelp(null, options);
                return;
            }

            if (!cmdline.hasOption(configOpt.getOpt())) {
                printHelp("ERROR: missing configuration file", options);
                return;
            }
            String configFile = cmdline.getOptionValue(configOpt.getOpt());
            Signals signals = getInstance(configFile);

            if (cmdline.hasOption(importSignalsEntitiesOpt.getOpt())) {
                //rest call on signals REST-Api
                signals.signalsEntityCall();
                return;
            }

            if (cmdline.hasOption(dryRunOpt.getOpt())) {
                signals.updateConfig.updateDb = false;
                signals.updateConfig.updateSNB = false;
                signals.updateConfig.updateFromLdap = false;
            }

            if (cmdline.hasOption(noMailOpt.getOpt())) {
                signals.noMail = true;
            }

            if (cmdline.hasOption(noUpdateSnbOpt.getOpt())) {
                signals.updateConfig.updateSNB = false;
            }

            if (cmdline.hasOption(noUpdateFromLdapOpt.getOpt())) {
                signals.updateConfig.updateFromLdap = false;
            }

            if (cmdline.hasOption(noSyncDbFromSNBOpt.getOpt())) {
                signals.updateConfig.syncDbFromSNB = false;
            }

            if (cmdline.hasOption(trustStoreOpt.getOpt())) {
                System.setProperty("javax.net.ssl.trustStore", cmdline.getOptionValue(trustStoreOpt.getOpt()));
            }


            if (cmdline.hasOption(debugOpt.getOpt())) {
                if (!signals.logConfig.setLogLevel(cmdline.getOptionValue(debugOpt.getOpt()))) {
                    printHelp("ERROR: invalid log level", options);
                    return;
                }
            }

            if (cmdline.hasOption(discoverOpt.getOpt())) {
                signals.dynEnumMgr.allowEnumDiscovery();
            }

            if (cmdline.hasOption(userMgrOpt.getOpt())) {
                signals.manageUsers();
            }

            if (cmdline.hasOption(materialsMgrOpt.getOpt())) {
                signals.manageMaterials();
            }



        } catch (MissingArgumentException mae) {
            printHelp("ERROR: " + mae.getMessage(), options);
        } catch (MissingOptionException moe) {
            printHelp("ERROR: " + moe.getMessage(), options);
        } catch (UnrecognizedOptionException uoe) {
            printHelp("ERROR: " + uoe.getMessage(), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        Options options = new Options();
        options.addOption(configOpt);
        options.addOption(helpOpt);
        options.addOption(debugOpt);
        options.addOption(discoverOpt);
        options.addOption(dryRunOpt);
        options.addOption(noMailOpt);
        options.addOption(noUpdateSnbOpt);
        options.addOption(noUpdateFromLdapOpt);
        options.addOption(noSyncDbFromSNBOpt);
        options.addOption(trustStoreOpt);
        options.addOption(materialsMgrOpt);
        options.addOption(userMgrOpt);
        options.addOption(importSignalsEntitiesOpt);

        processCommandLine(argv, options);
    }
}


