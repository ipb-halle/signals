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
 * @author fbroda
 */
public class CrawlFileUpdate extends SqlQuery<CrawlFile> {

    private final static String QUERY = """
UPDATE files SET path_id=?, size=?, uid=?, git=?, type=?, mode=?,
  missing=?, atime=?, ctime=? mtime=?, digest=?, link_target=?
  WHERE id=?
""";

    @Override
    public void execute(CrawlFile file) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(file.getPathId());
        arguments.add(file.getSize());
        arguments.add(file.getUid());
        arguments.add(file.getGid());
        arguments.add(file.getType().getTypeId());
        arguments.add(file.getMode());
        arguments.add(file.isMissing());
        arguments.add(file.getAtime());
        arguments.add(file.getCtime());
        arguments.add(file.getMtime());
        arguments.add(file.getDigest());
        arguments.add(file.getLinkTarget());
        arguments.add(file.getId());
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
