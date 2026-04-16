/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author fblocal
 */
public class CrawlJobFactoryImpl implements CrawlJobFactory {

    private final Config config;

    public CrawlJobFactoryImpl(Config config) {
        this.config = config;
    }

    @Override
    public ConcurrentLinkedQueue<CrawlPath> buildJobs() {
        return new ConcurrentLinkedQueue<> ();
    }
}
