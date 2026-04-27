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
 * @author fbroda
 */
public class CrawlFileUpdate extends SqlQuery<CrawlFile> {

    private final static String QUERY = """
UPDATE files SET path_id=?, size=?, uid=?, gid=?, type=?, mode=?,
  missing=?, atime=?, ctime=?, mtime=?, digest=?, link_target=?
  WHERE id=? RETURNING id""";

    @Override
    public void execute(CrawlFile file) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(paramLong(file.getPathId()));
        arguments.add(paramLong(file.getSize()));
        arguments.add(paramLong(file.getUid()));
        arguments.add(paramLong(file.getGid()));
        arguments.add(paramInteger(file.getType().getTypeId()));
        arguments.add(paramInteger(file.getMode()));
        arguments.add(paramBoolean(file.isMissing()));
        arguments.add(paramTimestamp(file.getAtime()));
        arguments.add(paramTimestamp(file.getCtime()));
        arguments.add(paramTimestamp(file.getMtime()));
        arguments.add(paramBytes(file.getDigest()));
        arguments.add(paramString(file.getLinkTarget()));
        arguments.add(paramLong(file.getId()));
        execute(arguments);
        close();
    }

    @Override
    protected CrawlFile getRecord() throws SQLException {
        throw new UnsupportedOperationException("getRecord not implemented.");
    }

    @Override
    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(QUERY));
    }

    @Override
    public QueryType getType() {
        return QueryType.CrawlFileUpdate;
    }
}
