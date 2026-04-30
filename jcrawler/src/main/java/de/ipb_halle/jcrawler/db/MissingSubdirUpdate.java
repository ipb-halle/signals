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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author fbroda
 */
public class MissingSubdirUpdate extends SqlQuery<MissingSubdir> {

    private final static String QUERY = """
WITH upd_files AS (
        WITH filepath (id, path, namespace_id) AS
        (SELECT f.id, d.path, d.namespace_id
          FROM files AS f JOIN directories AS d ON f.path_id=d.id)
      UPDATE files AS fu SET missing=true FROM filepath AS fp
        WHERE fu.id=fp.id
            AND fp.namespace_id=?
            AND (fp.path = ?
                OR starts_with(fp.path, ? || ?))
        RETURNING fu.size AS bytes),
    upd_directories AS (
      UPDATE directories SET missing=true WHERE namespace_id=?
            AND (path = ?
                OR starts_with(path, ? || ?))
        RETURNING 0 AS bytes)
    SELECT sum(bytes) AS bytes, sum(entities) AS entities FROM (
        SELECT sum(bytes) AS bytes, count(*) AS entities FROM upd_files
      UNION
        SELECT sum(bytes) AS bytes, 0 AS entities FROM upd_directories) AS summary""";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public MissingSubdirUpdate() {
        super(QUERY);
    }

    @Override
    public void execute(MissingSubdir subdir) throws SQLException {
        logger.debug("Flagging missing subdir {}", subdir.getPath());
        List<Object> arguments = new ArrayList<> ();
        arguments.add(paramInteger(subdir.getNamespaceId()));
        arguments.add(paramString(subdir.getPath()));
        arguments.add(paramString(subdir.getPath()));
        arguments.add(paramString(subdir.getSeparator()));
        arguments.add(paramInteger(subdir.getNamespaceId()));
        arguments.add(paramString(subdir.getPath()));
        arguments.add(paramString(subdir.getPath()));
        arguments.add(paramString(subdir.getSeparator()));
        execute(arguments);
        MissingSubdir result = next();
        subdir.setBytes(result.getBytes());
        subdir.setEntities(result.getEntities());
        logger.debug("    total entities {}", subdir.getEntities());
        logger.debug("    total bytes {}", subdir.getBytes());
        close();
    }

    @Override
    protected MissingSubdir getRecord() throws SQLException {
        ResultSet result = getResultSet();
        MissingSubdir subdir = new MissingSubdir();
        subdir.setBytes(result.getLong(1));
        subdir.setEntities(result.getLong(2));
        return subdir;
    }

    @Override
    public QueryType getType() {
        return QueryType.MissingSubdirUpdate;
    }
}
