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
public class Statistics {

    private long newEntities = 0;
    private long changedEntities = 0;
    private long vanishedEntities = 0;

    public long getNewEntities() {
        return newEntities;
    }

    public long getChangedEntities() {
        return changedEntities;
    }

    public long getVanishedEntities() {
        return vanishedEntities;
    }

    public void incrementNew() {
        newEntities++;
    }

    public void incrementChanged() {
        changedEntities++;
    }

    public void incrementVanished() {
        vanishedEntities++;
    }

    public void accumulateStatistics(Statistics s) {
        newEntities += s.getNewEntities();
        changedEntities += s.getChangedEntities();
        vanishedEntities += s.getVanishedEntities();
    }
}
