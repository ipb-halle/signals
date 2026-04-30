/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 *
 * @author fblocal
 */
public class ConfigTest {

    public final static String ARRAY_PATH = "someObject.testArray";

    @Test
    public void testJsonConfig() {
        Config cfg = Config.getInstance().reset();
        Assertions.assertFalse(cfg.isFullScan());
        InputStream is = this.getClass().getResourceAsStream("configtest.json");

        cfg.setFullScan(true);
        Assertions.assertTrue(cfg.isFullScan());
        Assertions.assertFalse(cfg.setConfigStream(is));

        Assertions.assertEquals("Hello World!",
                cfg.getConfigString("fields.string", null));
        Assertions.assertEquals((Long) 1234567890L,
                cfg.getConfigLong("fields.long", 123L));
        Assertions.assertEquals(Boolean.TRUE,
                cfg.getConfigBoolean("fields.boolean", false));

        Assertions.assertTrue(cfg.isArray(ARRAY_PATH));
        Assertions.assertEquals(2, cfg.getArraySize(ARRAY_PATH));
        ConfigElement e = cfg.getArrayElement(ARRAY_PATH, 0);
        Assertions.assertEquals("foo", e.getConfigString("key", null));
        e = cfg.getArrayElement(ARRAY_PATH, 1);
        Assertions.assertEquals("BAR", e.getConfigString("value", null));
    }
}
