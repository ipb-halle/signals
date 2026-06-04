/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import de.ipb_halle.jcrawler.Config;
import de.ipb_halle.jcrawler.acl.AclConverter;
import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import de.ipb_halle.jcrawler.db.SqlConnection;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fblocal
 */
@ExtendWith(PostgresqlContainerExtension.class)
public class AclCacheTest {

    private AclConverter converter;

    public AclCacheTest() throws SQLException {
        setup();
        converter = AclConverter.getInstance();
    }

    private void setup() throws SQLException {
        Config config = Config.getInstance().reset();
        InputStream is = this.getClass().getResourceAsStream("../crawler.json");
        config.setConfigStream(is);
        Properties props = System.getProperties();
        String dir = props.getProperty("user.dir");
        config.setGlobalPrefix(dir);
        DbPrincipalCache principalCache = DbPrincipalCache.getInstance();
        if (! principalCache.isReady()) {
            principalCache.setup(SqlConnection.getConnection());
        }
        AclCache aclCache = AclCache.getInstance();
        if (! aclCache.isReady()) {
            aclCache.setup(SqlConnection.getConnection());
        }
    }

    public List<AclEntry> allBut(String name) {
        List<AclEntry> acl = new ArrayList<> ();

        DbPrincipal fred = new DbPrincipal(name);
        AclEntry.Builder builder = AclEntry.newBuilder();
        builder.setType(AclEntryType.DENY);
        builder.setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT);
        builder.setPermissions(AclEntryPermission.READ_DATA,
                AclEntryPermission.READ_ACL,
                AclEntryPermission.READ_ATTRIBUTES,
                AclEntryPermission.READ_NAMED_ATTRS,
                AclEntryPermission.EXECUTE);
        builder.setPrincipal(fred);
        acl.add(builder.build());

        DbPrincipal everyone = new DbPrincipal("EVERYONE@");
        builder = AclEntry.newBuilder();
        builder.setType(AclEntryType.ALLOW);
        builder.setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT);
        builder.setPermissions(AclEntryPermission.READ_DATA,
                AclEntryPermission.WRITE_DATA,
                AclEntryPermission.APPEND_DATA,
                AclEntryPermission.READ_ACL,
                AclEntryPermission.READ_ATTRIBUTES,
                AclEntryPermission.READ_NAMED_ATTRS,
                AclEntryPermission.EXECUTE);
        builder.setPrincipal(everyone);
        acl.add(builder.build());
        return acl;
    }

    @Test
    public void testCache() throws IOException {
        AclCache cache = AclCache.getInstance();
        Acl acl1 = new Acl();
        acl1.setRawAttribute(
                converter.buildRawAttribute(
                        allBut("fred")));
        acl1 = cache.lookup(acl1);
        Assertions.assertTrue(acl1.getId() > 0);
        Acl acl2 = new Acl();
        acl2.setRawAttribute(
                converter.buildRawAttribute(
                        allBut("lissy")));
        acl2 = cache.lookup(acl2);
        Assertions.assertNotEquals(acl1.getId(), acl2.getId());
    }
}
