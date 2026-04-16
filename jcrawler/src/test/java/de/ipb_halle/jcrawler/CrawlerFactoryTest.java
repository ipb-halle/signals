/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fblocal
 */
public class CrawlerFactoryTest {

    @Test
    public void testBuildCrawlers() {
        Properties props = System.getProperties();
        String dir = props.getProperty("user.dir");
        Path pom = Paths.get(dir, "pom.xml");
        Path fail = Paths.get(dir, "will_never_exist.txt");
        assertTrue(Files.exists(pom, LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(fail, LinkOption.NOFOLLOW_LINKS));
    }
}
