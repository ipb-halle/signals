/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author fblocal
 */
public class NamespaceByName extends SqlQuery<Namespace> {

    private final static String QUERY = "SELECT id, name FROM namespaces WHERE name=?";

    @Override
    protected Namespace getRecord() throws SQLException {
        ResultSet rs = getResultSet();
        Namespace ns = new Namespace();
        ns.setId(rs.getInt(1));
        ns.setName(rs.getString(2));
        return ns;
    }

    @Override
    public QueryType getType() {
        return QueryType.NamespaceByName;
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }
}
