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
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;


public class CmdLineProcessor {

   /*
    * OPTIONS
    */
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
    private static final Option jobOpt = Option.builder("j")
            .longOpt("jobType")
            .desc("""
Type of job to perform. Default job is set to 'crawl'. Currently
the only alternative job type is 'purge', which removes all
records from the files and directories, which have their missing
flag set.""")
            .hasArg()
            .argName("JOB_TYPE")
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

   /*
    * Help Texts
    */
    private final static String HELP_USAGE = """
java -jar <JAR_WITH_DEPENDENCIES>
     [-Djava.library.path=LIBRARY_PATH --enable-native-access=ALL-UNNAMED]""";

    private final static String HELP_HEADER = """
JCrawler %s
(c) 2026 Leibniz Institute of Plant Biochemistry

Crawler for large file systems.

OPTIONS:
""";

   /*
    * Code
    */
    public static boolean processCommandLine(String[] argv) {
        Options options = registerOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            /*
             * handle helpOpt first to avoid error messages
             * from other options
             */
            if (cmdline.hasOption(helpOpt.getOpt())) {
                printHelp(null, options);
                return true;
            }

            if (cmdline.hasOption(fullScanOpt.getOpt())) {
                Config.getInstance().setFullScan(true);
            }

            if (cmdline.hasOption(configOpt.getOpt())) {
                String cfgFile = cmdline.getOptionValue(configOpt.getOpt());
                if (Config.getInstance().setConfigFile(cfgFile)) {
                    System.out.println("Could not parse config file");
                    return true;
                }
            } else {
                printHelp("ERROR: missing configuration file", options);
                return true;
            }

            if (cmdline.hasOption(jobOpt.getOpt())) {
                try {
                    Config.getInstance().setJobType(JobType.valueOf(
                            cmdline.getOptionValue(jobOpt.getOpt())));
                } catch(IllegalArgumentException e) {
                    System.out.println("Unknown job type.");
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
        options.addOption(jobOpt);
        options.addOption(levelOpt);
        return options;
    }

    private static void printHelp(String errorMessage, Options options) {
        HelpFormatter writer = new HelpFormatter();
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
        writer.printHelp(HELP_USAGE,
                HELP_HEADER.formatted(Config.getProjectVersion()),
                options,
                footer,
                true);
    }
}


