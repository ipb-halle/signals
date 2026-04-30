/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

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
accumulated_sizes=?, change_time=now(), missing=? WHERE id=? RETURNING id
""";

    public DirectoryUpdate() {
        super(QUERY);
    }

    @Override
    public void execute(Directory dir) throws SQLException {
        List<Object> param = new ArrayList<> ();
        param.add(paramLong(dir.getNewEntries()));
        param.add(paramLong(dir.getChangedEntries()));
        param.add(paramLong(dir.getVanishedEntries()));
        param.add(paramLong(dir.getAccumulatedSizes()));
        // change time provided by the database
        param.add(paramBoolean(dir.isMissing()));
        param.add(paramLong(dir.getId()));
        execute(param);
        close();
    }

    @Override
    protected Directory getRecord() throws SQLException {
        throw new UnsupportedOperationException("No record available for UPDATE");
    }

    @Override
    public QueryType getType() {
        return QueryType.DirectoryUpdate;
    }
}
