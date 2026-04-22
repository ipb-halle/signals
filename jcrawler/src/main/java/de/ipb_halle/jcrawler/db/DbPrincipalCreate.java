/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fblocal
 */
public class DbPrincipalCreate extends SqlQuery<DbPrincipal> {

    private final static String QUERY = """
                                        INSERT INTO principals (principal, is_group, is_everyone, guid)
                                        VALUES (?, ?, ?, ?) RETURNING id
                                        """;
    @Override
    public void execute(DbPrincipal principal) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(principal.getPrincipal().getName());
        arguments.add(principal.isGroup());
        arguments.add(principal.isEveryone());
        arguments.add(principal.getGuid());
        execute(arguments);
        if (hasNext()) {
            principal.setId(getResultSet().getLong(1));
        }
        close();
    }

    @Override
    protected DbPrincipal getRecord() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.DbPrincipalCreate;
    }
}
