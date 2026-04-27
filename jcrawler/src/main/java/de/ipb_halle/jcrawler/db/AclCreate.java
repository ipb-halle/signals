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
public class AclCreate extends SqlQuery<Acl> {

    private final static String QUERY = """
                                        INSERT INTO acls (raw_attribute)
                                        VALUES (?) RETURNING id
                                        """;
    @Override
    public void execute(Acl acl) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(paramBytes(acl.getRawAttribute()));
        execute(arguments);
        if (hasNext()) {
            acl.setId(getResultSet().getLong(1));
            System.out.printf("AclId() = %d\n",  acl.getId());
        }
        close();
    }

    @Override
    protected Acl getRecord() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.AclCreate;
    }
}
