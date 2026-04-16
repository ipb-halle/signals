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
public class CrawlerFactoryImpl implements CrawlerFactory {

    private final Config config;
    private final static String CRAWLER_CONFIG = "crawlers";

    public CrawlerFactoryImpl(Config config) {
        this.config = config;
    }

    @Override
    public List<Crawler> buildCrawlers() {
        ArrayList<Crawler> crawlers = new ArrayList<> ();
        if (config.isArray(CRAWLER_CONFIG)) {
            int size = config.getArraySize(CRAWLER_CONFIG);
            for (int i = 0; i < size; i++) {
                ConfigElement crawlerConfig = config.getArrayElement(CRAWLER_CONFIG, i);
                crawlers.add(setupCrawler(crawlerConfig));
            }
        }
        return crawlers;
    }

    private Crawler setupCrawler(ConfigElement cfg) {
        return null;
    }
}
