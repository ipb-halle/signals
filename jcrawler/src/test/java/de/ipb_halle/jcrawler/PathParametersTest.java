/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.acl.AclHandlerProvider;
import de.ipb_halle.jcrawler.acl.Flavor;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fbroda
 */
public class PathParametersTest {

    @Test
    public void testPathParameters() {
        Timestamp t = Timestamp.from(
                Instant.now().minusSeconds(10));

        if (File.separatorChar == '/') {
            PathParameters p = new PathParameters("/foo/bar", "/foo", "example.com");
            AclHandlerProvider provider = AclHandlerProvider.getInstance();
            p.setAclHandler(provider.getHandler(Flavor.Null.toString()));
            p.setAlgorithm(DigestAlgorithm.SHA256);
            Assertions.assertFalse(p.isFullScan());
            Assertions.assertTrue(p.getScanCutOff().after(t));
            Assertions.assertEquals("/bar", p.getLogicalPath());
            p.setFullScan(true);
            p.setScanThreshold(3600);

            PathParameters q = p.createPathParameters("/foo/bar/batz");
            Assertions.assertTrue(q.isFullScan());
            Assertions.assertTrue(q.getScanCutOff().before(t));
            Assertions.assertTrue(Objects.equals(p.getAlgorithm(), q.getAlgorithm()));
            Assertions.assertTrue(Objects.equals(p.getAclHandler(), q.getAclHandler()));
            Assertions.assertEquals("/bar/batz", q.getLogicalPath());
            Assertions.assertEquals("/foo/bar/batz", q.getPath());
            Assertions.assertEquals("example.com", q.getNamespaceName());

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> q.createPathParameters("/foo/bar/botz/mupf"));
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> new PathParameters("/foo/bar", "/fuh", "example.com"));
        }
    }
}
