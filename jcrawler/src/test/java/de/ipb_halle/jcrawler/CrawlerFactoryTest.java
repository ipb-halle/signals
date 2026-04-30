/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.CrawlFileCreate;
import de.ipb_halle.jcrawler.db.DirectoryByName;
import de.ipb_halle.jcrawler.db.DirectoryUpdate;
import de.ipb_halle.jcrawler.db.NamespaceCreate;
import de.ipb_halle.jcrawler.db.QueryType;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class CrawlerFactoryTest {

    private CrawlerFactory factory;

    public CrawlerFactoryTest() {
        setup();
    }

    private void setup() {
        Config config = Config.getInstance();
        InputStream is = this.getClass().getResourceAsStream("crawler.json");
        config.setConfigStream(is);
        Properties props = System.getProperties();
        String dir = props.getProperty("user.dir");
        config.setGlobalPrefix(dir);
        factory = new CrawlerFactoryImpl();
    }

    public void checkQueryTypes(Crawler crawler, Class<?> query, QueryType type) {
        Assertions.assertEquals(query.getName(), crawler.getQuery(type).getClass().getName());
    }

    @Test
    public void testBuildCrawlers() {
        Config config = Config.getInstance();
        Path pom = Paths.get(config.getGlobalPrefix(), "pom.xml");
        Path fail = Paths.get(config.getGlobalPrefix(), "will_never_exist.txt");
        Assertions.assertTrue(Files.exists(pom, LinkOption.NOFOLLOW_LINKS));
        Assertions.assertFalse(Files.exists(fail, LinkOption.NOFOLLOW_LINKS));

        List<Crawler> crawlers = factory.buildCrawlers();
        Assertions.assertEquals(1, crawlers.size());
        Crawler crawler = crawlers.get(0);
        // just a selection for each connection type
        checkQueryTypes(crawler, CrawlFileCreate.class, QueryType.CrawlFileCreate);
        checkQueryTypes(crawler, DirectoryByName.class, QueryType.DirectoryByName);
        checkQueryTypes(crawler, DirectoryUpdate.class, QueryType.DirectoryUpdate);
        checkQueryTypes(crawler, NamespaceCreate.class, QueryType.NamespaceCreate);
    }
}
