/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import de.ipb_halle.jcrawler.Statistics;
import java.sql.Timestamp;

/**
 *
 * @author fblocal
 */
public class Directory {
    private Long id;
    private Integer namespaceId;
    private String path;
    private Long newEntries;
    private Long changedEntries;
    private Long vanishedEntries;
    private Long accumlatedSizes;
    private Timestamp changeTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(Integer namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getNewEntries() {
        return newEntries;
    }

    public void setNewEntries(Long newEntries) {
        this.newEntries = newEntries;
    }

    public Long getChangedEntries() {
        return changedEntries;
    }

    public void setChangedEntries(Long changedEntries) {
        this.changedEntries = changedEntries;
    }

    public Long getVanishedEntries() {
        return vanishedEntries;
    }

    public void setVanishedEntries(Long vanishedEntries) {
        this.vanishedEntries = vanishedEntries;
    }

    public Long getAccumlatedSizes() {
        return accumlatedSizes;
    }

    public void setAccumlatedSizes(Long accumlatedSizes) {
        this.accumlatedSizes = accumlatedSizes;
    }

    public Timestamp getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(Timestamp changeTime) {
        this.changeTime = changeTime;
    }

    public void setStatistics(Statistics statistics) {
        newEntries = statistics.getNewEntities();
        changedEntries = statistics.getChangedEntities();
        vanishedEntries = statistics.getVanishedEntities();
    }
}
