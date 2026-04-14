/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.util.LinkedList;

/**
 *
 * @author fblocal
 */
public class CrawlerExecutor {

    private final Config config;
    private LinkedList<Thread> threads;

    public CrawlerExecutor(Config config) {
        this.config = config;
    }

    public void start() {
        threads = new LinkedList<> ();
        for(Crawler c : new CrawlerFactory(config).buildCrawlers()) {
            Thread t = new Thread(c);
            threads.add(t);
            t.start();
        }
    }

    public void joinAll() {
        while(! threads.isEmpty()) {
            Thread t = threads.getFirst();
            try {
                t.join();
                threads.removeFirst();
            } catch (InterruptedException ex) {
                System.getLogger(CrawlerExecutor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
}
