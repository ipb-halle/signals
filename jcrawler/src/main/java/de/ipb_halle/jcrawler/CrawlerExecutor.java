/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author fblocal
 */
public class CrawlerExecutor {

    private final CrawlerFactory crawlerFactory;
    private final CrawlJobFactory jobFactory;
    private LinkedList<Crawler> crawlers;
    private final Statistics statistics;

    // crawlJobs will see concurrent access!
    private ConcurrentLinkedQueue<CrawlPath> crawlJobs;


    public CrawlerExecutor(CrawlJobFactory jobFactory,
            CrawlerFactory crawlerFactory) {
        this.jobFactory = jobFactory;
        this.crawlerFactory = crawlerFactory;
        this.statistics = new Statistics();

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
                crawlers.removeFirst();
            } catch (InterruptedException ex) {
                System.getLogger(CrawlerExecutor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        return statistics;
    }

    public Statistics getStatistics() {
        return statistics;
    }
}
