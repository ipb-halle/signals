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

    private final static String[] suffixes = new String[] { "", "k", "M", "G", "T", "P", "E" };

    private long newEntities = 0;
    private long changedEntities = 0;
    private long vanishedEntities = 0;
    private long newBytes = 0;
    private long changedBytes = 0;
    private long vanishedBytes = 0;

    public void addNewBytes(long b) {
        newBytes += b;
    }

    public void addChangedBytes(long b) {
        changedBytes += b;
    }

    public void addVanishedBytes(long b) {
        vanishedBytes += b;
    }

    public long getNewBytes() {
        return newBytes;
    }

    public long getNewEntities() {
        return newEntities;
    }

    public long getChangedBytes() {
        return changedBytes;
    }

    public long getChangedEntities() {
        return changedEntities;
    }

    public long getVanishedBytes() {
        return vanishedBytes;
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
        newBytes += s.getNewBytes();
        changedBytes += s.getChangedBytes();
        vanishedBytes += s.getVanishedBytes();
        newEntities += s.getNewEntities();
        changedEntities += s.getChangedEntities();
        vanishedEntities += s.getVanishedEntities();
    }

    public long getTotalEntities() {
        return newEntities - vanishedEntities;
    }

    public long getTotalBytes() {
        return newBytes + changedBytes - vanishedBytes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("New entities:      %12s (%s)\n".formatted(
                formatLong(newEntities, null),
                formatLong(newBytes, "Bytes")));
        sb.append("Changed entities:  %12s (%s)\n".formatted(
                formatLong(changedEntities, null),
                formatLong(changedBytes, "Bytes")));
        sb.append("Vanished entities: %12s (%s)\n".formatted(
                formatLong(vanishedEntities, null),
                formatLong(vanishedBytes, "Bytes")));
        sb.append("Total entities:    %12s (%s)\n".formatted(
                formatLong(getTotalEntities(), null),
                formatLong(getTotalBytes(), "Bytes")));
        return sb.toString();
    }

    public String formatLong(long number, String unit) {
        int suffixIndex = 0;
        boolean negative = (number < 0);
        double d = Double.valueOf(negative ? -number : number);
        while (d > 1000.0) {
            d /= 1000.0;
            suffixIndex++;
        }
        if (suffixIndex > 0) {
            return "%s%.2f %s%s".formatted(
                    negative ? "-" : "",
                    d,
                    suffixes[suffixIndex],
                    unit == null ? "" : unit);
        }
        return "%d%s%s".formatted(
                number,
                unit == null ? "" : " ",
                unit == null ? "" : unit);
    }
}
