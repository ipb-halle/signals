/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.Namespace;


/**
 *
 * @author fbroda
 */
public interface CrawlPath {
    public Statistics getStatistics();
    public void setCrawler(Crawler crawler);
    public void walkDirectory();
}
