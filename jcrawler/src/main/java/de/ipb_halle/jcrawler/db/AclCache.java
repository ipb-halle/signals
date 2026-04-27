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

/**
 *
 * @author fblocal
 */
public class AclCache {
    private final Map<Acl, Acl> acls;
    private static final AclCache instance = new AclCache();
    private AclCreate create;
    private AclDetail detail;

    private AclCache() {
        acls = new HashMap<> ();
    }

    public static AclCache getInstance() {
        return instance;
    }

    public void setup(Connection conn) throws SQLException {
        AclQuery query = new AclQuery();
        query.prepare(conn);
        query.execute(new ArrayList<> ());
        while (query.hasNext()) {
            Acl acl = query.next();
            acls.put(acl, acl);
        }
        query.close();
        create = new AclCreate();
        create.prepare(conn);
        detail = new AclDetail();
        detail.prepare(conn);
    }

    public Acl lookup(Acl acl) {
        if (acl.getRawAttribute() == null) {
            throw new NullPointerException("Was erlaube AclCache.lookup()!");
            // return null;
        }
        Acl cachedAcl = acls.get(acl);
        if (cachedAcl == null) {
            createAcl(acl);
            cachedAcl = acls.get(acl);
        }
        return cachedAcl;
    }

    private synchronized void createAcl(Acl acl) {
        try {
            System.out.printf("Creating new ACL\n");
            create.execute(acl);
            if (acl.getRawAttribute() == null) {
                throw new NullPointerException("Was erlaube AclCache.createAcl()!");
            }
            if (acl.getId() != null) {
                acls.put(acl, acl);
                System.out.printf("Created Acl: id = %d\n", acl.getId());
                createDetails(acl);
            } else {
                System.out.println("execute did not create an acl");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // possible duplicate key exception ignored
        }
    }

    private void createDetails(Acl acl) throws SQLException {
        detail.begin();
        System.out.printf("createDetails: %s\n", acl.getRawAttribute().toString());
        detail.execute(acl);
        detail.close();
    }
}
