/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fblocal
 */
public class CrawlerExecutor {

    private final Config config;
    private List<Thread> threads;

    public CrawlerExecutor(Config config) {
        this.config = config;
    }

    public void start() {
        threads = new ArrayList<> ();
        for(Crawler c : new CrawlerFactory(config).buildCrawlers()) {
            Thread t = new Thread(c);
            threads.add(t);
            t.start();
        }
    }

    public void joinAll() {
    }
}
