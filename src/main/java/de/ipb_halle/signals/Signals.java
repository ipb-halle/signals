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

import de.ipb_halle.signals.users.AccessManager;
import de.ipb_halle.signals.users.LdapClient;
import de.ipb_halle.signals.users.IUser;

import java.util.Iterator;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.api.LocalClient;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
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
    private SignalsEntityManager signalsMgr;

    @Inject
    private AccessManager accessManager;

    @Inject
    private LdapClient ldapClient;

    private boolean dryRun;
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
    private static final Option userMgrOpt = Option.builder("u")
    .longOpt("manage-users")
    .desc("Perform user,  group and role management")
    .build();

    @SuppressWarnings("static-acces")
    private static final Option dryRunOpt = Option.builder("n")
    .longOpt("dry-run")
    .desc("Don't modify - dry run")
    .build();

    @SuppressWarnings("static-acces")
    private static final Option debugOpt = Option.builder("d")
    .longOpt("debug")
    .hasArg()
    .argName("LEVEL")
    .desc("Set log level to DEBUG")
    .build();



    /**
     * default constructor
     */
    public Signals() {
        dryRun = false;
        logger = LoggerFactory.getLogger(Signals.class);
    }

    public void doIt() {

//          signalsMgr.doGet("location");

/*
            System.out.println("Users\n=====");
            Set<String> users = ldapClient.getUsers(null);
            dumpSet(users);

            Set<String> groups = ldapClient.getMemberships("SOME USER DN");
            System.out.println("Group memberships\n=================");
            dumpSet(ldapClient.filterDNs(groups, LdapClient.FilterType.GROUP));
            System.out.println("Role memberships\n================");
            dumpSet(ldapClient.filterDNs(groups, LdapClient.FilterType.ROLE));

            User u = ldapClient.getUser("SOME USER DN");
            System.out.println(u.dump());

            System.out.println("User by name\n============");
            users = ldapClient.getUsers("SOME EMAIL ADDRESS");
            dumpSet(users);

            System.out.println("Members of Group\n================");
            groups = ldapClient.getMembers("SOME GROUP DN");
            System.out.println("Members");
            dumpSet(groups);
*/
    }

    private void dumpSet(Set<String> set) {
        Iterator<String> iter = set.iterator();
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    private void manageUsers() {
        System.out.println("Managing users ...");
        accessManager.setDryRun(dryRun);
        accessManager.manageAccess();
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

        } catch(Exception e) {
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
     * @param argv the command line arguments
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

            if (! cmdline.hasOption(configOpt.getOpt())) {
                printHelp("ERROR: missing configuration file", options);
                return;
            }
            String configFile = cmdline.getOptionValue(configOpt.getOpt());
            Signals signals = getInstance(configFile);

            if (cmdline.hasOption(dryRunOpt.getOpt())) {
                signals.setDryRun();
            }

            if (cmdline.hasOption(debugOpt.getOpt())) {
                if (! signals.setLogLevel(cmdline.getOptionValue(debugOpt.getOpt()))) {
                    printHelp("ERROR: invalid log level", options);
                    return;
                }
            }

            if (cmdline.hasOption(userMgrOpt.getOpt())) {
                signals.manageUsers();
            } 

        } catch(MissingArgumentException mae) {
            printHelp("ERROR: " + mae.getMessage(), options);
        } catch(MissingOptionException moe) {
            printHelp("ERROR: " + moe.getMessage(), options);
        } catch(UnrecognizedOptionException uoe) {
            printHelp("ERROR: " + uoe.getMessage(), options);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param userLevel text representation of the log level, should be one of 
     * <code>FATAL, ERROR, WARN, INFO, DEBUG, TRACE</code>.
     * @return true if setting of log level succeeded, false otherwise
     */
    private boolean setLogLevel(String userLevel) {
        try {
            Level level = Level.valueOf(userLevel);
            Configurator.setLevel("de.ipb_halle", level); 
            return true;
        } catch (IllegalArgumentException iae) {
            logger.warn("Undefined level '{}' in setLogLevel()", userLevel);
        } catch (NullPointerException npe) {
            logger.warn("setLogLevel(userLevel) called with null argument");
        }
        return false;
    }

    private void setDryRun() {
        dryRun = true;
    }

    public static void main(String[] argv) {
        Options options = new Options();
        options.addOption(configOpt);
        options.addOption(helpOpt);
        options.addOption(debugOpt);
        options.addOption(dryRunOpt);
        options.addOption(userMgrOpt);

        processCommandLine(argv, options);
    }
}


