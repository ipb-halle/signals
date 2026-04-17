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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fblocal
 */
public class DirectoryCreate extends SqlQuery<Directory> {

    private final static String QUERY = "INSERT INTO directories (namespace_id, path) VALUES (?, ?) RETURNING id";

    @Override
    public void execute(Directory dir) throws SQLException {
        List<Object> param = new ArrayList<> ();
        param.add(dir.getNamespaceId());
        param.add(dir.getPath());
        execute(param);
        Directory dbDir = next();
        dir.setId(dbDir.getId());
    }

    @Override
    protected Directory getRecord() throws SQLException {
        ResultSet result = getResultSet();
        Directory dir = new Directory();
        dir.setId(result.getLong(1));
        dir.setNamespaceId(result.getInt(2));
        dir.setPath(result.getString(3));
        return dir;
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.DirectoryCreate;
    }
}
