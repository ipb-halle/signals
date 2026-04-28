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
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author fblocal
 */
public class DbPrincipalCache {
    private final Map<String, DbPrincipal> principals;
    private static final DbPrincipalCache instance = new DbPrincipalCache();
    private final Logger logger;
    private DbPrincipalCreate create;

    private DbPrincipalCache() {
        principals = new HashMap<> ();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public static DbPrincipalCache getInstance() {
        return instance;
    }

    public void setup(Connection conn) throws SQLException {
        DbPrincipalsQuery query = new DbPrincipalsQuery();
        query.prepare(conn);
        query.execute(new ArrayList<> ());
        while (query.hasNext()) {
            DbPrincipal p = query.next();
            principals.put(p.getName(), p);
        }
        query.close();
        create = new DbPrincipalCreate();
        create.prepare(conn);
    }

    public DbPrincipal lookup(DbPrincipal principal) {
        DbPrincipal p = principals.get(principal.getName());
        logger.trace("Looking up principal {}", principal.getName());
        if (p == null) {
            createPrincipal(principal);
            p = principals.get(principal.getName());
            if ((p == null) || (p.getId() == null)) {
                throw new NullPointerException("caching mechanism failure");
            }
        }
        return p;
    }

    private synchronized void createPrincipal(DbPrincipal principal) {
        try {
            create.execute(principal);
            if (principal.getId() != null) {
                principals.put(principal.getName(), principal);
                logger.debug("Created principal {} --> {}", principal.getName(), principal.getId());
            } else {
                logger.debug("createPrincipal() did not create a principal");
            }
        } catch (SQLException ex) {
            // possible duplicate key exception ignored
        }
    }
}
