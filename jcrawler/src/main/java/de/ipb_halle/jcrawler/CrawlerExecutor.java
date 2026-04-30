/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.SqlConnection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author fblocal
 */
public class CrawlerExecutor {

    private final CrawlerFactory crawlerFactory;
    private final CrawlJobFactory jobFactory;
    private LinkedList<Crawler> crawlers;
    private final Statistics statistics;
    private final Logger logger;

    // crawlJobs will see concurrent access!
    private ConcurrentLinkedQueue<CrawlPath> crawlJobs;


    public CrawlerExecutor(CrawlJobFactory jobFactory,
            CrawlerFactory crawlerFactory) {
        this.jobFactory = jobFactory;
        this.crawlerFactory = crawlerFactory;
        this.statistics = new Statistics();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void start() {
        crawlers = crawlerFactory.buildCrawlers();
        crawlJobs = jobFactory.buildJobs();
        for(Crawler c : crawlers) {
            c.setJobQueue(crawlJobs);
            Thread t = new Thread(c);
            c.setThread(t);
            t.start();
        }
    }

    public Statistics joinAll() {
        while(! crawlers.isEmpty()) {
            Crawler c = crawlers.getFirst();
            try {
                c.getThread().join();
                statistics.accumulateStatistics(c.getStatistics());
                SqlConnection.closeConnections(c.getConnections());
                crawlers.removeFirst();
            } catch (InterruptedException ex) {
                logger.debug("joinAll() was interrupted.");
            } catch (SQLException ex) {
                logger.warn("SQLException in joinAll: {}", ex.getMessage());
            }
        }
        return statistics;
    }

    public Statistics getStatistics() {
        return statistics;
    }
}
