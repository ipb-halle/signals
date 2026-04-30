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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private final Logger logger;

    public Main() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public static void main(String[] argv) {
        Main main = new Main();
        if (CmdLineProcessor.processCommandLine(argv)) {
            return;
        }
        main.run();
    }

    private void run() {
        logger.info("Started JCrawler {}", Config.getProjectVersion());
        System.out.printf("""
*****************************************************
*                                                   *
* JCrawler %-8s                                 *
* Job start: %-32s       *
* Job type:  %-8s                               *
*                                                   *
*****************************************************
""",
                Config.getProjectVersion(),
                new Date().toString(),
                Config.getInstance().getJobType().toString());

        switch(Config.getInstance().getJobType()) {
            case crawl -> runCrawl();
            case purge -> runPurge();
        }

    }

    private void runCrawl() {
        CrawlerFactoryImpl crawlerFactory = new CrawlerFactoryImpl();
        CrawlJobFactoryImpl jobFactory = new CrawlJobFactoryImpl();
        CrawlerExecutor executor = new CrawlerExecutor(jobFactory, crawlerFactory);
        executor.start();
        executor.joinAll();
        System.out.printf("""

Statistics
==========
%s

*****************************************************
*                                                   *
* Job finished: %-32s    *
*                                                   *
*****************************************************
""",
                executor.getStatistics().toString(),
                new Date().toString());
    }

    private void runPurge() {
        // this method will clean all files and directories
        // which have their missing flag set.
        System.out.println("Currently not supported.");
    }
}