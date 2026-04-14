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

/**
 * JCrawler is a project to efficiently crawl large
 * filesystems.
 */

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

    private void run() {
        System.out.printf("Run parameters\nfull scan: %s\nconfigFile: %s\n",
                Boolean.valueOf(fullScan),
                configFile);
    }

    public static void printHelp(String errorMessage, Options options) {
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
        System.exit(1);
    }

    public static void processCommandLine(Main main, String[] argv, Options options) {

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, argv);

            if (!cmdline.hasOption(configOpt.getOpt())) {
                printHelp("ERROR: missing configuration file", options);
            }

            if (cmdline.hasOption(fullScanOpt.getOpt())) {
                main.fullScan = true;
            }

            if (cmdline.hasOption(helpOpt.getOpt())) {
                printHelp(null, options);
            }

            main.configFile = cmdline.getOptionValue(configOpt.getOpt());

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
        options.addOption(fullScanOpt);
        options.addOption(helpOpt);

        Main main = new Main();
        processCommandLine(main, argv, options);
        main.run();
    }
}


