/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.DigestAlgorithm;
import de.ipb_halle.jcrawler.FileInspector;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author frank
 */
public class AclHandlerTest {

    @Test
    public void testAclHandler() {
       GenericAclHandler handler = (GenericAclHandler) GenericAclHandler.getInstance();
       GenericAclHandler.Platform platform = handler.getPlatform();
       if (platform == GenericAclHandler.Platform.Windows) {
           try {
               URL url = this.getClass().getResource("../digestFile.txt");
               Path p = Paths.get(url.toURI());
               byte[] rawAttribute = handler.getRawAttribute(p);
               List<AclEntry> acl = AclConverter.getInstance().parseAcl(rawAttribute);
               Assertions.assertFalse(acl.isEmpty());
           } catch (URISyntaxException | IOException e) {
               Assertions.fail(e);
           }
        }
    }
}
