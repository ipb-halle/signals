/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 *
 */
package de.ipb_halle.jcrawler;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    @SuppressWarnings("static-access")
    private static final Option configOpt = Option.builder("c")
            .longOpt("config")
            .desc("""
The configuration file that controls the program's actions.
Use the -H option to view information about the format of
this file. Mandatory option.
""")
            .hasArg()
            .argName("FILE")
            .required(true)
            .build();

    @SuppressWarnings("static-access")
    private static final Option fullScanOpt = Option.builder("f")
            .longOpt("full")
            .desc("""
Do not skip inactive subdirectories. Normally, subdirectories
that have not been modified for a configurable period of time
are skipped during the scan. This flag triggers a full scan.
""")
            .build();

    @SuppressWarnings("static-access")
    private static final Option helpOpt = Option.builder("h")
            .longOpt("help")
            .desc("\nDisplay this help")
            .build();



    private Logger logger;
    private String configFile;
    private boolean fullScan = false;

    public Main() {
        logger = LoggerFactory.getLogger(Main.class);
    }

    public static void main(String[] argv) {

        Main main = new Main();
        if (processCommandLine(main, argv)) {
            main.run();
        }
    }

    private void run() {
        System.out.printf("Run parameters\nfull scan: %s\nconfigFile: %s\n",
                fullScan,
                configFile);
    }

    public static boolean processCommandLine(Main main, String[] argv) {
        Options options = registerOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if (!cmdline.hasOption(configOpt.getOpt())) {
                printHelp("ERROR: missing configuration file", options);
                return false;
            }

            if (cmdline.hasOption(fullScanOpt.getOpt())) {
                main.fullScan = true;
            }

            if (cmdline.hasOption(helpOpt.getOpt())) {
                printHelp(null, options);
                return false;
            }

            main.configFile = cmdline.getOptionValue(configOpt.getOpt());

        } catch (MissingArgumentException | MissingOptionException | UnrecognizedOptionException e) {
            printHelp("ERROR: " + e.getMessage(), options);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static Options registerOptions() {
        Options options = new Options();
        options.addOption(configOpt);
        options.addOption(fullScanOpt);
        options.addOption(helpOpt);
        return options;
    }

    private static void printHelp(String errorMessage, Options options) {
        HelpFormatter writer = new HelpFormatter();
        String usage = "java -jar <JAR_WITH_DEPENDENCIES>";
        String header = "\n\nJCrawler\n(c) 2026 Leibniz Institute of Plant Biochemistry\n\nCrawler for large file systems.\n\nOPTIONS:\n";
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
    }
}


