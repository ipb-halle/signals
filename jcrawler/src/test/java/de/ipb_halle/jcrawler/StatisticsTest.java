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
    }
}
