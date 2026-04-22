/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.*;
import java.sql.Connection;
import java.sql.SQLException;
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
    private DbPrincipalCache principalCache;

    public CrawlerFactoryImpl(Config config) {
        this.config = config;
        setupPrincipalCache();
    }

    @Override
    public List<Crawler> buildCrawlers() {
        ArrayList<Crawler> crawlers = new ArrayList<> ();
        try {
            Long nThreads = config.getConfigLong(CONFIG_NTHREADS, null);
            if ((nThreads != null) && (nThreads > 0) && (nThreads < MAX_THREADS)) {
                for (long i = 0; i < nThreads; i++) {
                    crawlers.add(setupCrawler());
                }
            }
        } catch(SQLException e) {
            throw new RuntimeException(
                    String.format("Crawler setup failed: %s", e.getMessage()));
        }
        return crawlers;
    }

    private Crawler setupCrawler() throws SQLException {
        Crawler crawler = new Crawler();
        crawler.setPrincipalCache(principalCache);
        addCopyConnections(crawler);
        addQueryConnections(crawler);
        addUpdateConnections(crawler);
        return crawler;
    }

    private void setupPrincipalCache() {
        try {
            Connection conn = SqlConnection.getConnection(config);
            principalCache = DbPrincipalCache.getInstance();
            principalCache.setup(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addCopyConnections(Crawler crawler) throws SQLException {
        Connection conn = SqlConnection.getConnection(config);
        addSqlQuery(crawler, conn, new CrawlFileCreate());
    }

    private void addQueryConnections(Crawler crawler) throws SQLException {
        Connection conn = SqlConnection.getConnection(config);
        addSqlQuery(crawler, conn, new CrawlFileByDir());
        addSqlQuery(crawler, conn, new DirectoryByName());
        addSqlQuery(crawler, conn, new NamespaceByName());
    }

    private void addUpdateConnections(Crawler crawler) throws SQLException {
        Connection conn = SqlConnection.getConnection(config);
        addSqlQuery(crawler, conn, new CrawlFileUpdate());
        addSqlQuery(crawler, conn, new DirectoryUpdate());
        addSqlQuery(crawler, conn, new NamespaceCreate());
    }

    private void addSqlQuery(Crawler crawler, Connection conn, SqlQuery query) throws SQLException {
        query.prepare(conn);
        crawler.addQuery(query.getType(), query);
    }
}
