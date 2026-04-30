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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fblocal
 */
public class DbPrincipalCreate extends SqlQuery<DbPrincipal> {

    private final static String QUERY = """
                                        INSERT INTO principals (principal, is_group, is_everyone, guid)
                                        VALUES (?, ?, ?, ?) RETURNING id
                                        """;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public DbPrincipalCreate() {
        super(QUERY);
    }

    @Override
    public void execute(DbPrincipal principal) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(paramString(principal.getName()));
        arguments.add(paramBoolean(principal.isGroup()));
        arguments.add(paramBoolean(principal.isEveryone()));
        arguments.add(paramLong(principal.getGuid()));
        execute(arguments);
        if (hasNext()) {
            principal.setId(getResultSet().getLong(1));
            logger.debug("created principal {} -->{}",
                    principal.getName(),
                    principal.getId());
        }
        close();
    }

    @Override
    protected DbPrincipal getRecord() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryType getType() {
        return QueryType.DbPrincipalCreate;
    }
}
