/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 *
 */
package de.ipb_halle.jcrawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
/**
 *
 * @author fblocal
 */
public class MainTest {

    @Test
    public void testOptions() {
        Main main = new Main();
        Assertions.assertFalse(Main.processCommandLine(main,
                new String[] {"-h", "-f"}));
    }
}
