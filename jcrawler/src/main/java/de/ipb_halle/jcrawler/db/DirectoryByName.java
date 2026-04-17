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
public class DirectoryByName extends SqlQuery<Directory> {

    private final static String QUERY = """
SELECT id, namespace_id, path, new_entries, changed_entries, vanished_entries
  accumulated_sizes, change_time
  FROM directories
  WHERE namespace_id=? AND path=?""";

    @Override
    protected Directory getRecord() throws SQLException {
        ResultSet result = getResultSet();
        Directory dir = new Directory();
        dir.setId(result.getLong(1));
        dir.setNamespaceId(result.getInt(2));
        dir.setPath(result.getString(3));
/*
        dir.setNewEntries(result.getLong(4));
        dir.setChangedEntries(result.getLong(5));
        dir.setVanishedEntries(result.getLong(6));
        dir.setAccumlatedSizes(result.getLong(7));
*/
        dir.setChangeTime(result.getTimestamp(8));

        return dir;
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.DirectoryByName;
    }
}
