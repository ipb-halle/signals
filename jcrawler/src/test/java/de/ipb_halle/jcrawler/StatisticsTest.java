/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 *
 * @author fblocal
 */
public class StatisticsTest {

    @Test
    public void testStatistics() {
        Statistics s1 = new Statistics();
        s1.incrementNew();
        s1.incrementVanished();

        Statistics s2 = new Statistics();
        s2.incrementNew();
        s2.incrementNew();
        s2.incrementChanged();
        s2.incrementVanished();

        s1.accumulateStatistics(s2);
        Assertions.assertEquals(3,
                s1.getNewEntities());
        Assertions.assertEquals(1,
                s1.getChangedEntities());
        Assertions.assertEquals(2,
                s1.getVanishedEntities());

        Assertions.assertTrue(s1.toString().contains("New"));
        Assertions.assertTrue(s1.toString().contains("Changed"));
        Assertions.assertTrue(s1.toString().contains("Vanished"));
        Assertions.assertTrue(s1.toString().contains("entities"));
        Assertions.assertTrue(s1.toString().contains("Byte"));
        Assertions.assertTrue(s1.toString().contains("Total"));
    }

    @Test
    public void testFormat() {
        Statistics s = new Statistics();
        Assertions.assertEquals("1", s.formatLong(1L, null));
        Assertions.assertEquals("1.20 k", s.formatLong(1_200L, null));
        Assertions.assertEquals("634.80 GByte", s.formatLong(634_801_234_567L, "Byte"));
        Assertions.assertEquals("-44.01 MWh", s.formatLong(-44_008_000, "Wh"));
    }
}
