/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author fblocal
 */
public class DbPrincipalsQuery extends SqlQuery<DbPrincipal> {

    private final static String QUERY = "SELECT id, principal, is_group, is_everyone, guid FROM principals";

    public DbPrincipalsQuery() {
        super(QUERY);
    }

    @Override
    protected DbPrincipal getRecord() throws SQLException {
        ResultSet result = getResultSet();
        String name = result.getString(2);
        DbPrincipal p = new DbPrincipal(name);
        p.setId(result.getLong(1));
        p.setGroup(result.getBoolean(3));
        p.setEveryone(result.getBoolean(4));
        p.setGuid(result.getLong(5));
        return p;
    }

    @Override
    public QueryType getType() {
        return QueryType.DbPrincipalsQuery;
    }
}
