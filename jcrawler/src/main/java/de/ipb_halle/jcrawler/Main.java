/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 *
 */
package de.ipb_halle.jcrawler;

import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
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

// import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private final static String PROJECT_VERSION = "projectVersion";

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

    @SuppressWarnings("static-access")
    private static final Option levelOpt = Option.builder("l")
            .longOpt("level")
            .desc("""
Log level. Default level is set to INFO. Valid options
include ERROR, WARN, INFO, DEBUG and TRACE.""")
            .hasArg()
            .argName("LEVEL")
            .build();


    private final Config config;
    private final Logger logger;

    public Main() {
        logger = LoggerFactory.getLogger(this.getClass());
        config = new Config();
    }

    public static void main(String[] argv) {
        Main main = new Main();
        if (processCommandLine(main, argv)) {
            return;
        }
        main.run();
    }

    private void run() {
        logger.info("Started JCrawler {}", getProjectVersion());
        CrawlerFactoryImpl crawlerFactory = new CrawlerFactoryImpl(config);
        CrawlJobFactoryImpl jobFactory = new CrawlJobFactoryImpl(config);
        CrawlerExecutor executor = new CrawlerExecutor(jobFactory, crawlerFactory);
        executor.start();
        executor.joinAll();
        System.out.printf("""
*****************************************************
*                                                   *
* JCrawler %-8s                                 *
* %-32s                  *
*                                                   *
*****************************************************

Statistics
==========
%s""",
                getProjectVersion(),
                new Date().toString(),
                executor.getStatistics().toString());
    }


    private String getProjectVersion() {
        try {
            Manifest mf = new Manifest();
            mf.read(this.getClass().getResourceAsStream("/META-INF/MANIFEST.MF"));
            Attributes attributes = mf.getMainAttributes();
            return attributes.getValue(PROJECT_VERSION);
        } catch (Exception e) {
            return "unavail";
        }
    }

    public static boolean processCommandLine(Main main, String[] argv) {
        Options options = registerOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if (!cmdline.hasOption(configOpt.getOpt())) {
                printHelp("ERROR: missing configuration file", options);
                return true;
            }

            if (cmdline.hasOption(fullScanOpt.getOpt())) {
                main.config.setFullScan(true);
            }

            if (cmdline.hasOption(helpOpt.getOpt())) {
                printHelp(null, options);
                return true;
            }

            if (cmdline.hasOption(configOpt.getOpt())) {
                String cfgFile = cmdline.getOptionValue(configOpt.getOpt());
                if (main.config.setConfigFile(cfgFile)) {
                    System.out.println("Could not parse config file");
                    return true;
                }
            }

            if (cmdline.hasOption(levelOpt.getOpt())) {
                String userInput = cmdline.getOptionValue(levelOpt.getOpt());
                Level newLevel = Level.valueOf(userInput);
                if (newLevel != null) {
                    Configurator.setLevel("de.ipb_halle", newLevel);
                } else {
                    printHelp("ERROR: unrecognized log level", options);
                    return true;
                }
            }

        } catch (MissingArgumentException | MissingOptionException | UnrecognizedOptionException e) {
            printHelp("ERROR: " + e.getMessage(), options);
            return true;
        } catch (ParseException e) {
            printHelp("ERROR: " + e.getMessage(), options);
            return true;
        }
        return false;
    }

    private static Options registerOptions() {
        Options options = new Options();
        options.addOption(configOpt);
        options.addOption(fullScanOpt);
        options.addOption(helpOpt);
        options.addOption(levelOpt);
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


