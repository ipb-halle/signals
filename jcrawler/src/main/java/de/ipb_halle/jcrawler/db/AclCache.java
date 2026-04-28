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
public class AclCache {
    private final Map<Acl, Acl> acls;
    private static final AclCache instance = new AclCache();
    private final Logger logger;
    private AclCreate create;
    private AclDetail detail;

    private AclCache() {
        acls = new HashMap<> ();
        logger = LoggerFactory.getLogger(this.getClass());
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
            throw new NullPointerException("lookup() called with empty attribute!");
        }
        Acl cachedAcl = acls.get(acl);
        if (cachedAcl == null) {
            createAcl(acl);
            cachedAcl = acls.get(acl);
            if ((cachedAcl == null) || (cachedAcl.getId() == null)) {
                throw new NullPointerException("caching mechanism failure");
            }
        }
        return cachedAcl;
    }

    private synchronized void createAcl(Acl acl) {
        try {
            create.execute(acl);
            if (acl.getId() != null) {
                acls.put(acl, acl);
                createDetails(acl);
            } else {
                logger.debug("createAcl did not create an acl");
            }
        } catch (SQLException ex) {
            // possible duplicate key exception ignored
        }
    }

    private void createDetails(Acl acl) throws SQLException {
        detail.begin();
        detail.execute(acl);
        detail.close();
    }
}
