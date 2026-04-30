/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class FileInspectorTest {

    @Test
    public void testMode() {
        FileInspector inspector = FileInspector.getInstance();
        Set<PosixFilePermission> perms = new HashSet<> ();
        Assertions.assertEquals(Integer.valueOf(0), inspector.getMode(perms));

        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Assertions.assertEquals(Integer.valueOf(0111), inspector.getMode(perms));

        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.OWNER_READ);
        Assertions.assertEquals(Integer.valueOf(0777), inspector.getMode(perms));
    }

    @Test
    public void testDigest() {
        try {
            URL url = this.getClass().getResource("digestFile.txt");
            Path p = Paths.get(url.toURI());
            FileInspector inspector = FileInspector.getInstance();
            byte[] digest = inspector.digest(p, DigestAlgorithm.MD5);
            byte[] expected = HexFormat.of().parseHex("b5f3b2db7d97a68cae8c4cbf1ff66c7a");
            Assertions.assertArrayEquals(expected, digest);
        } catch (URISyntaxException | NoSuchAlgorithmException | IOException e) {
            Assertions.fail(e);
        }
    }
}
