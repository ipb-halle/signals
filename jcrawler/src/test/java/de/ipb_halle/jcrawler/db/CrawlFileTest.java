/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is f project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import de.ipb_halle.jcrawler.db.CrawlFile.FileType;
import java.sql.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class CrawlFileTest {

    public CrawlFile createCrawlFile() {
        Timestamp ts1 = new Timestamp(1234567890L);

        CrawlFile f = new CrawlFile();
        f.setId(8L);
        f.setAclId(1L);
        f.setAtime(ts1);
        f.setCtime(ts1);
        f.setDigest(new byte[] {0,0,0,0});
        f.setGid(5L);
        f.setLinkTarget("/usr");
        f.setMode(0444);
        f.setMtime(ts1);
        f.setName("test");
        f.setPathId(5L);
        f.setSize(555L);
        f.setType(FileType.REGULAR_FILE);
        f.setUid(2L);
        return f;
    }

    @Test
    public void testEquals() {
        CrawlFile orig = createCrawlFile();
        CrawlFile other = null;

        Assertions.assertFalse(orig.equals(other));
        Assertions.assertFalse(orig.equals((Object) "different class object"));

        other = createCrawlFile();
        Assertions.assertEquals(orig, other);

        other.setPathId(6L);
        Assertions.assertFalse(orig.equals(other));
        Assertions.assertNotEquals(orig.hashCode(), other.hashCode());

        other = createCrawlFile();
        other.setName("test1");
        Assertions.assertFalse(orig.equals(other));
        Assertions.assertNotEquals(orig.hashCode(), other.hashCode());
    }

    @Test
    public void testDeepEquals() {
        Timestamp ts2 = new Timestamp(1239999999L);

        CrawlFile orig = createCrawlFile();
        CrawlFile other = createCrawlFile();

        other.setId(9L);
        other.setAtime(ts2);
        Assertions.assertTrue(orig.deepEquals(other));

        other = createCrawlFile();
        other.setAclId(2L);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setCtime(ts2);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setDigest(new byte[] {1, 2, 3});
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setGid(100L);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setLinkTarget("/var");
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setMtime(ts2);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setMissing(true);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setMode(0666);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setName("foo");
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setSize(1234L);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setType(FileType.OTHER);
        Assertions.assertFalse(orig.deepEquals(other));

        other = createCrawlFile();
        other.setUid(1000L);
        Assertions.assertFalse(orig.deepEquals(other));
    }
}
