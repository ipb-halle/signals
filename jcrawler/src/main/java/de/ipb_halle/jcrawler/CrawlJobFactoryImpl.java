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
    private final static String PATHS_CONFIG = "paths";

    public CrawlJobFactoryImpl(Config config) {
        this.config = config;
    }

    @Override
    public ConcurrentLinkedQueue<CrawlPath> buildJobs() {
        ConcurrentLinkedQueue<CrawlPath> queue = new ConcurrentLinkedQueue<> ();
        if (config.isArray(PATHS_CONFIG)) {
            int size = config.getArraySize(PATHS_CONFIG);
            for (int i = 0; i < size; i++) {
                ConfigElement pathConfig = config.getArrayElement(PATHS_CONFIG, i);
                queue.add(setupPath(pathConfig));
            }
        }
        return queue;
    }

    private CrawlPath setupPath(ConfigElement element) {
        return null;
    }
}
