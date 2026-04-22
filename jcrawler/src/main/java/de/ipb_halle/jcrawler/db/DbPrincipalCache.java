/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fblocal
 */
public class DbPrincipalCache {
    private final Map<String, DbPrincipal> principals;
    private static final DbPrincipalCache instance = new DbPrincipalCache();
    private DbPrincipalCreate create;

    private DbPrincipalCache() {
        principals = new HashMap<> ();
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
        System.out.printf("Looking up principal %s\n", principal.getName());
        if (p == null) {
            createPrincipal(principal);
            p = principals.get(principal.getName());
        }
        return p;
    }

    private synchronized void createPrincipal(DbPrincipal principal) {
        try {
            System.out.printf("Creating new principal: %s\n", principal.getName());
            create.execute(principal);
            if (principal.getId() != null) {
                principals.put(principal.getName(), principal);
                System.out.printf("Created Principal(%s) --> %d\n", principal.getName(), principal.getId());
            } else {
                System.out.println("execute did not create a principal");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // possible duplicate key exception ignored
        }
    }
}
