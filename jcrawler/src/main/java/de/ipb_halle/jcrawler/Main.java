/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 *
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.MissingFilePurge;
import de.ipb_halle.jcrawler.db.MissingSubdirPurge;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
""",
                executor.getStatistics().toString());
        finish();
    }

    private void runPurge() {
        // this method shall clean all subdir trees and
        // individual files of a given namespace, which
        // have their missing flag set.
        try {
            List<Object> arguments = new ArrayList<> ();
            arguments.add(Config.getInstance().getNamespace());
            MissingSubdirPurge subdirPurge = new MissingSubdirPurge();
            MissingFilePurge filePurge = new MissingFilePurge();
            subdirPurge.execute(arguments);
            filePurge.execute(arguments);
        } catch(SQLException e) {
            logger.warn("runPurge failed: {}", e.getMessage());
        }
        finish();
    }

    private void finish() {
System.out.printf("""
*****************************************************
*                                                   *
* Finished: %-32s        *
*                                                   *
*****************************************************
""",
                new Date().toString());
    }
}