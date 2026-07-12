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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fblocal
 */
public class NamespaceCreate extends SqlQuery<Namespace> {

    private final static String QUERY = "INSERT INTO namespaces (name) VALUES (?) RETURNING id, name";

    public NamespaceCreate() {
        super(QUERY);
    }

    @Override
    public void execute(Namespace spc) throws SQLException {
        List<Object> param = new ArrayList<> ();
        param.add(spc.getName());
        execute(param);
        Namespace dbSpc = next();
        spc.setId(dbSpc.getId());
        close();
    }

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
        return QueryType.NamespaceCreate;
    }
}
