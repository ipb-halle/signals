/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.db.Acl;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author frank
 */
@ExtendWith(PostgresqlContainerExtension.class)
public class AclHandlerTest {

    @Test
    public void testAclHandler() {
       AclHandlerProvider provider = AclHandlerProvider.getInstance();
       String platform = provider.getPlatform();
       if (platform.toLowerCase().startsWith("windows")) {
           AclHandler handler = provider.getHandler(Flavor.Windows.toString());
           try {
               URL url = this.getClass().getResource("../digestFile.txt");
               Path p = Paths.get(url.toURI());
               Acl acl = handler.getAcl(p);
               Assertions.assertTrue(acl.isValid());
               Assertions.assertFalse(acl.getAcl().isEmpty());
           } catch (URISyntaxException | IOException e) {
               Assertions.fail(e);
           }
        }
    }
}
