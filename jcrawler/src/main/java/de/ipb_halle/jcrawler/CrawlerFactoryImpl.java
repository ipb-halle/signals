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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author fblocal
 */
public class CrawlerFactoryImpl implements CrawlerFactory {

    private final static String CONFIG_NTHREADS = "threads";
    private final static long MAX_THREADS = 10L;     // requires 3*MAX_THREADS JDBC connections

    private final Config config;

    public CrawlerFactoryImpl(Config config) {
        this.config = config;
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
        addQueryConnections(crawler);
        addUpdateConnections(crawler);
        return null;
    }

    private void addQueryConnections(Crawler crawler) throws SQLException {
        Connection conn = SqlConnection.getConnection(config);
        addNamespaceByName(crawler, conn);
        addNamespaceCreate(crawler, conn);
    }

    private void addUpdateConnections(Crawler crawler) throws SQLException {
        Connection conn = SqlConnection.getConnection(config);
    }

    private void addNamespaceByName(Crawler crawler, Connection conn) throws SQLException {
        NamespaceByName n = new NamespaceByName();
        n.prepare(conn);
        crawler.addQuery(n.getType(), n);
    }

    private void addNamespaceCreate(Crawler crawler, Connection conn) throws SQLException {
        NamespaceCreate n = new NamespaceCreate();
        n.prepare(conn);
        crawler.addQuery(n.getType(), n);
    }
}
