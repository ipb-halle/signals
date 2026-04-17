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

/**
 *
 * @author fblocal
 */
public class CrawlerFactoryImpl implements CrawlerFactory {

    private final static String CONFIG_NTHREADS = "threads";
    private final static long MAX_THREADS = 10L;     // requires 3*MAX_THREADS JDBC connections

    private final Config config;

    public CrawlerFactoryImpl(Config config) {
        this.config = config;
    }

    @Override
    public List<Crawler> buildCrawlers() {
        ArrayList<Crawler> crawlers = new ArrayList<> ();
        Long nThreads = config.getConfigLong(CONFIG_NTHREADS, null);
        if ((nThreads != null) && (nThreads > 0) && (nThreads < MAX_THREADS)) {
            for (long i = 0; i < nThreads; i++) {
                crawlers.add(setupCrawler());
            }
        }
        return crawlers;
    }

    private Crawler setupCrawler() {
        return null;
    }
}
