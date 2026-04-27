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
public class AclQuery extends SqlQuery<Acl> {

    private final String QUERY = "SELECT id, raw_attribute FROM acls";


    @Override
    protected Acl getRecord() throws SQLException {
        ResultSet result = getResultSet();
        Acl acl = new Acl();
        acl.setId(result.getLong(1));
        acl.setRawAttribute(result.getBytes(2));
        return acl;
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.AclQuery;
    }

}
