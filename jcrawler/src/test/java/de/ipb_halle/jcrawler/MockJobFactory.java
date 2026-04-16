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
public class MockJobFactory implements CrawlJobFactory {

    @Override
    public ConcurrentLinkedQueue<CrawlPath> buildJobs() {
        return new ConcurrentLinkedQueue<> ();
    }

}
