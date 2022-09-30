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

import de.ipb_halle.signals.users.LdapClient;
import de.ipb_halle.signals.users.User;

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
    private LdapClient ldapClient;

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


    public static void main(String[] argv) {
        Options options = new Options();
        options.addOption(configOpt);
        options.addOption(helpOpt);
        options.addOption(userMgrOpt);

        processCommandLine(argv, options);
    }
}


