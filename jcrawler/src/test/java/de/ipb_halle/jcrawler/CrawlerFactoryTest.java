/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fblocal
 */
public class CrawlerFactoryTest {

    private final Config config;
    private CrawlerFactory factory;

    public CrawlerFactoryTest() {
        config = new Config();
        setup();
    }

    private void setup() {
        InputStream is = this.getClass().getResourceAsStream("crawler.json");
        config.setConfigStream(is);
        Properties props = System.getProperties();
        String dir = props.getProperty("user.dir");
        config.setGlobalPrefix(dir);
        factory = new CrawlerFactoryImpl(config);
    }

    @Test
    public void testBuildCrawlers() {
        Path pom = Paths.get(config.getGlobalPrefix(), "pom.xml");
        Path fail = Paths.get(config.getGlobalPrefix(), "will_never_exist.txt");
        assertTrue(Files.exists(pom, LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(fail, LinkOption.NOFOLLOW_LINKS));

        List<Crawler> crawlers = factory.buildCrawlers();
        assertEquals(2,
                crawlers.size());
    }
}
