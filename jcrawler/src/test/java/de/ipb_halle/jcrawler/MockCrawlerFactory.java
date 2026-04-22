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
public class MockCrawlerFactory implements CrawlerFactory {

    @Override
    public LinkedList<Crawler> buildCrawlers() {
        LinkedList<Crawler> crawlers = new LinkedList<> ();
        crawlers.add(new MockCrawler(3));
        crawlers.add(new MockCrawler(5));
        return crawlers;
    }
}
