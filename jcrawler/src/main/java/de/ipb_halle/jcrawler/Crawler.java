/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.util.AbstractQueue;

/**
 *
 * @author fblocal
 */
public class Crawler implements Runnable {

    private final Statistics statistics = new Statistics();
    private Thread thread;

    public Statistics getStatistics() {
        return statistics;
    }

    public void run() {

    }

    public void setJobQueue(AbstractQueue<CrawlPath> q) {

    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }


}
