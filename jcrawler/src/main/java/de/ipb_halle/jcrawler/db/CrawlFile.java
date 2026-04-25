/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author frank
 */
public class CrawlFile {

    public enum FileType {
        REGULAR_FILE(0),
        DIRECTORY(1),
        SYMBOLIC_LINK(2),
        OTHER(3);

        private final int typeId;

        private FileType(int id) {
            typeId = id;
        }

        public int getTypeId() {
            return typeId;
        }
        public static FileType getById(int id) {
            for (FileType t : FileType.values()) {
                if (t.typeId == id) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Invalid FileType-Id");
        }
    }

    private Long id;
    private Long pathId;
    private Long size;
    private Long uid;
    private Long gid;
    private FileType type;
    private Integer mode;
    private String name;
    private String digest;
    private String linkTarget;
    private Timestamp atime;
    private Timestamp ctime;
    private Timestamp mtime;
    private boolean missing;

    @Override
    public int hashCode() {
        int hash = (name != null) ? name.hashCode() : 0;
        hash += (pathId != null) ? pathId.hashCode() : 0;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CrawlFile other = (CrawlFile) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.pathId, other.pathId);
    }

    public boolean deepEquals(CrawlFile other) {
        return (missing == other.missing)
                && Objects.equals(size, other.size)
                && Objects.equals(type, other.type)
                && Objects.equals(mode, other.mode)
                && Objects.equals(uid, other.uid)
                && Objects.equals(gid, other.gid)
                && Objects.equals(ctime, other.ctime)
                && Objects.equals(mtime, other.mtime)
                && Objects.equals(name, other.name)
                && Objects.equals(digest, other.digest)
                && Objects.equals(linkTarget, other.linkTarget);
    }

    public boolean isDirectory() {
        return type == CrawlFile.FileType.DIRECTORY;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPathId() {
        return pathId;
    }

    public void setPathId(Long path_id) {
        this.pathId = path_id;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    public Timestamp getAtime() {
        return atime;
    }

    public void setAtime(Timestamp atime) {
        this.atime = atime;
    }

    public Timestamp getCtime() {
        return ctime;
    }

    public void setCtime(Timestamp ctime) {
        this.ctime = ctime;
    }

    public Timestamp getMtime() {
        return mtime;
    }

    public void setMtime(Timestamp mtime) {
        this.mtime = mtime;
    }

    public boolean isMissing() {
        return missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }
}
