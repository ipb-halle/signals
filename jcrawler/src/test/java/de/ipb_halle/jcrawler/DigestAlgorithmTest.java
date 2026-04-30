/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class DigestAlgorithmTest {
    @Test
    public void testByName() {
        Assertions.assertEquals(DigestAlgorithm.MD5, DigestAlgorithm.byName("MD5"));
        Assertions.assertEquals(DigestAlgorithm.SHA1, DigestAlgorithm.byName("SHA1"));
        Assertions.assertEquals(DigestAlgorithm.SHA1, DigestAlgorithm.byName("SHA-1"));
        Assertions.assertEquals(DigestAlgorithm.SHA256, DigestAlgorithm.byName("SHA256"));
        Assertions.assertEquals(DigestAlgorithm.SHA256, DigestAlgorithm.byName("SHA-256"));
        Assertions.assertThrows(RuntimeException.class, () -> DigestAlgorithm.byName("invalid"));
    }
}
