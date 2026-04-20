/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

/**
 *
 * @author fblocal
 */
public enum QueryType {
    CrawlFileByDir,
    CrawlFileCreate,
    DirectoryByName,
    DirectoryCreate,
    DirectoryUpdate,
    NamespaceByName,
    NamespaceCreate,
    DbPrincipalsQuery,
    DbPrincipalCreate;
}
