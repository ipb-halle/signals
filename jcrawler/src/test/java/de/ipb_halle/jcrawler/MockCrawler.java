/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

/**
 *
 * @author fblocal
 */
public class MockCrawler extends Crawler {

    private int cycles;

    public MockCrawler(int cycles) {
        super();
        this.cycles = cycles;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        for (int i=0; i<cycles; i++) {
            getStatistics().incrementNew();
            try {
                Thread.sleep(20L);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
}
