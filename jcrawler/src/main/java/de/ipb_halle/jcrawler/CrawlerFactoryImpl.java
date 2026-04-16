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

    public CrawlerFactoryImpl(Config config) {
        this.config = config;
    }

    @Override
    public List<Crawler> buildCrawlers() {
        return new ArrayList<> ();
    }
}
