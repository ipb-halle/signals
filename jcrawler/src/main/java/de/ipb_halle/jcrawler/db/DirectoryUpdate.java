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
public class DirectoryUpdate extends SqlQuery<Directory> {

    private final static String QUERY = """
UPDATE directories SET new_entries=?, changed_entries=?, vanished_entries=?,
accumulated_size=?, change_time=now() WHERE id=?
""";

    @Override
    public void execute(Directory dir) throws SQLException {
        List<Object> param = new ArrayList<> ();
        param.add(dir.getNamespaceId());
        param.add(dir.getPath());
        param.add(dir.getNewEntries());
        param.add(dir.getChangedEntries());
        param.add(dir.getVanishedEntries());
        param.add(dir.getAccumlatedSizes());
        // change time provided by the database
        param.add(dir.getId());
        execute(param);
    }

    @Override
    protected Directory getRecord() throws SQLException {
        throw new UnsupportedOperationException("No record available for UPDATE");
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.DirectoryUpdate;
    }
}
