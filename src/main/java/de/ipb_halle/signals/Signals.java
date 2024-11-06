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
import de.ipb_halle.signals.entity.SignalsEntityConfig;
import de.ipb_halle.signals.entity.SignalsEntityManager;
import de.ipb_halle.signals.inventory.InventoryConfig;
import de.ipb_halle.signals.inventory.InventoryManager;
import de.ipb_halle.signals.materials.LibraryManager;
import de.ipb_halle.signals.materials.MaterialsConfig;
import de.ipb_halle.signals.users.AccessConfig;
import de.ipb_halle.signals.users.AccessManager;
import de.ipb_halle.signals.users.LdapClient;

import java.util.*;

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
    private InventoryManager inventoryManager;

    @Inject
    private LibraryManager libraryManager;

    @Inject
    private LdapClient ldapClient;

    @Inject
    private LogConfig logConfig;

    @Inject
    private SignalsEntityManager signalsEntityManager;

    private AccessConfig accessConfig;
    private InventoryConfig inventoryConfig;
    private MaterialsConfig materialsConfig;
    private RuntimeConfig runtimeConfig;
    private SignalsEntityConfig signalsEntityConfig;
    private boolean noMail;

    private Logger logger;

    @SuppressWarnings("static-access")
    private static final Option configOpt = Option.builder("c")
            .longOpt("config")
            .desc("\nPath to a the config file containing the Signals (tm) web token, base URL as well as database and LDAP connection information.")
            .hasArg()
            .argName("FILE")
            .required(true)
            .build();

    @SuppressWarnings("static-access")
    private static final Option helpOpt = Option.builder("h")
            .longOpt("help")
            .desc("\nDisplay the help")
            .build();


    /**
     * default constructor
     */
    public Signals() {
        noMail = false;
        logger = LoggerFactory.getLogger(Signals.class);
    }

    // @PostConstruct annotation did not work
    private void postConstruct() {
        runtimeConfig = new RuntimeConfig();
        accessConfig = new AccessConfig(signalsConfig,
                runtimeConfig, accessManager);
        inventoryConfig = new InventoryConfig(signalsConfig,
                runtimeConfig, inventoryManager);
        materialsConfig = new MaterialsConfig(signalsConfig,
                runtimeConfig, libraryManager);
        signalsEntityConfig = new SignalsEntityConfig(signalsConfig,
                runtimeConfig, signalsEntityManager);
    }

    public void dumpEntities(Date[] dateRange) {
        signalsEntityConfig.dumpEntities(dateRange);
    }

    private void dumpSet(Set<String> set) {
        Iterator<String> iter = set.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    protected LogConfig getLogConfig() {
        return logConfig;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    public SignalsEntityConfig getSignalsEntityConfig() {
        return signalsEntityConfig;
    }

    public DynEnumManager getDynEnumMgr() {
        return dynEnumMgr;
    }

    public void manageAccess() {
        accessConfig.manageAccess();
    }

    public void manageEntities(Date[] dateRange) {
        signalsEntityConfig.manageEntities(dateRange);
    }

    public void manageInventory(Date[] dateRange) {
        inventoryConfig.manageInventory(dateRange);
    }

    public void manageMaterials(Date[] dateRange) {
        materialsConfig.manageMaterials(dateRange);
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
            signals.postConstruct();

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
        writer.setDescPadding(60);
        writer.setOptPrefix("\n\n\u00A0\u001B[1m-");
        writer.setNewLine("\u001B[m\n");
        writer.printHelp(usage, header, options, footer, true);
        System.exit(1);
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
            }

            if (!cmdline.hasOption(configOpt.getOpt())) {
                printHelp("ERROR: missing configuration file", options);
            }

            String configFile = cmdline.getOptionValue(configOpt.getOpt());
            Signals signals = getInstance(configFile);
            RuntimeConfig.processCommandLine(cmdline, options, signals);

            AccessConfig.processCommandLine(cmdline, options, signals);
            SignalsEntityConfig.processCommandLine(cmdline, options, signals);
            MaterialsConfig.processCommandLine(cmdline, options, signals);
            InventoryConfig.processCommandLine(cmdline, options, signals);

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

        RuntimeConfig.registerOptions(options);
        AccessConfig.registerOptions(options);
        SignalsEntityConfig.registerOptions(options);
        MaterialsConfig.registerOptions(options);
        InventoryConfig.registerOptions(options);
        processCommandLine(argv, options);
    }
}


