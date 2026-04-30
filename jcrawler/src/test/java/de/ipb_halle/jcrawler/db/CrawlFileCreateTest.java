/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import de.ipb_halle.jcrawler.Config;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class CrawlFileCreateTest {

    private CrawlFileCreate create;

    public CrawlFileCreateTest() {
        create = new CrawlFileCreate();
        setup();
    }

    private void setup() {
        Config config = Config.getInstance().reset();
        InputStream is = this.getClass().getResourceAsStream("../crawler.json");
        config.setConfigStream(is);
        Properties props = System.getProperties();
        String dir = props.getProperty("user.dir");
        config.setGlobalPrefix(dir);
    }

    @Test
    public void testCreate() {
        String st = "Hallo '\\We\tlt\\'";
        String esc = SqlConnection.copyEscape(st);
        System.out.printf(">>>%s<<<\n>>>%s<<<\n", st, esc);
        Assertions.assertEquals("Hallo '\\\\We\\tlt\\\\'",
                esc);
    }
}
