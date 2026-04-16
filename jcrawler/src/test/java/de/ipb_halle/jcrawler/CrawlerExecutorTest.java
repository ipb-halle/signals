/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fblocal
 */
public class CrawlerExecutorTest {

    CrawlerFactory mockCrawlerFactory;
    CrawlJobFactory mockJobFactory;

    public CrawlerExecutorTest() {
        mockCrawlerFactory = new MockCrawlerFactory();
        mockJobFactory = new MockJobFactory();
    }


    /**
     * Test of start method, of class CrawlerExecutor.
     */
    @Test
    public void testStart() {
        CrawlerExecutor executor = new CrawlerExecutor(
                mockJobFactory,
                mockCrawlerFactory);
        executor.start();
        Statistics statistics = executor.joinAll();
        assertEquals(8,
                statistics.getNewEntities());
    }

}
