/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.Namespace;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;

/**
 *
 * @author fbroda
 */
public class PathParameters {

    private boolean fullScan;
    private String logicalPath;
    private Namespace namespace;
    private final String namespaceName;
    private final String path;
    private final String prefix;
    private Timestamp scanCutOff;

    public PathParameters(String path, String prefix, String namespaceName) {
        this.fullScan = false;  // same as Config default
        this.path = path;
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        initPaths();
        setScanThreshold(0);
    }

    public PathParameters createPathParameters(String p) {
        PathParameters params = new PathParameters(p, prefix, namespaceName);
        params.setNamespace(namespace);
        params.setScanCutOff(scanCutOff);
        params.setFullScan(fullScan);
        return params;
    }

    private void initPaths() {
        int len = prefix.length();
        if ((path.length() >= len)
                && (path.startsWith(prefix))) {
            this.logicalPath = path.substring(len);
            if (this.logicalPath.charAt(0) == File.separatorChar) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid path/prefix combination");
    }

    public boolean isFullScan() {
        return fullScan;
    }

    public Timestamp getScanCutOff() {
        return scanCutOff;
    }

    public String getLogicalPath() {
        return logicalPath;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public String getPath() {
        return path;
    }

    public void setFullScan(boolean fullScan) {
        this.fullScan = fullScan;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    private void setScanCutOff(Timestamp scanCutOff) {
        this.scanCutOff = scanCutOff;
    }

    public final void setScanThreshold(long seconds) {
        this.scanCutOff = Timestamp.from(
                Instant.now().minusSeconds(seconds));
    }
}
