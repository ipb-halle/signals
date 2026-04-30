/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.Config;
import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import de.ipb_halle.jcrawler.db.SqlConnection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.AclEntry;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class AclConverterTest {

    private final AclConverter converter;

    public AclConverterTest() throws SQLException {
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
    }

    @Test
    public void testConversion() throws IOException {
        byte[] rawBuffer;
        try (InputStream st = this.getClass().getResourceAsStream("testACL.bin")) {
            rawBuffer = st.readAllBytes();
            List<AclEntry> acl = converter.parseAcl(rawBuffer);
            Assertions.assertEquals(4, acl.size());
            byte[] reverseBuffer = converter.buildRawAttribute(acl);
            Assertions.assertArrayEquals(rawBuffer, reverseBuffer);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }
}
