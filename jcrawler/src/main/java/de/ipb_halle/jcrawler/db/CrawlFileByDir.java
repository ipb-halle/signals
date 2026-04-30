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
 * @author fbroda
 */
public class CrawlFileByDir extends SqlQuery<CrawlFile> {

    private final static String QUERY = """
SELECT id, path_id, size, uid, gid, type, mode, missing,
  atime, ctime, mtime, name, digest, link_target
  FROM files
  WHERE path_id=?
  ORDER BY name
""";

    public CrawlFileByDir() {
        super(QUERY);
    }

    @Override
    protected CrawlFile getRecord() throws SQLException {
        CrawlFile c = new CrawlFile();
        ResultSet result =getResultSet();
        c.setId(result.getLong(1));
        c.setPathId(result.getLong(2));
        c.setSize(result.getLong(3));
        c.setUid(result.getLong(4));
        c.setGid(result.getLong(5));
        c.setType(CrawlFile.FileType.getById(result.getInt(6)));
        c.setMode(result.getInt(7));
        c.setMissing(result.getBoolean(8));
        c.setAtime(result.getTimestamp(9));
        c.setCtime(result.getTimestamp(10));
        c.setMtime(result.getTimestamp(11));
        c.setName(result.getString(12));
        c.setDigest(result.getBytes(13));
        c.setLinkTarget(result.getString(14));
        return c;
    }

    @Override
    public QueryType getType() {
        return QueryType.CrawlFileByDir;
    }
}
