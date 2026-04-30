/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import de.ipb_halle.jcrawler.db.QueryType;
import de.ipb_halle.jcrawler.db.SqlQuery;
import java.sql.Connection;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fblocal
 */
public class Crawler implements Runnable {

    private final Statistics statistics;
    private final  List<Connection> connections;
    private final Map<QueryType, SqlQuery<?>> queries;
    private AbstractQueue<CrawlPath> queue;
    private Thread thread;

    public Crawler() {
        this.statistics = new Statistics();
        this.queries = new HashMap<> ();
        this.connections = new ArrayList<> ();
    }

    public void addConnection(Connection conn) {
        connections.add(conn);
    }

    public void addQuery(QueryType type, SqlQuery<?> query) {
        queries.put(type, query);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public SqlQuery<?> getQuery(QueryType type) {
        return queries.get(type);
    }

    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public void run() {
        while(! queue.isEmpty()) {
            CrawlPath path = queue.remove();
            if (path == null) {
                return;
            }
            path.setCrawler(this);
            path.walkDirectory();
            statistics.accumulateStatistics(path.getStatistics());
        }
    }

    public void setJobQueue(AbstractQueue<CrawlPath> q) {
        queue = q;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }


}
