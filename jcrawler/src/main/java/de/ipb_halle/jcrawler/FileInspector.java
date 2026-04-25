/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.CrawlFile;
import de.ipb_halle.jcrawler.db.DbPrincipal;
import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import de.ipb_halle.jcrawler.db.Directory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import java.sql.Timestamp;

/**
 *
 * @author fbroda
 */
public class FileInspector {

    private final static FileInspector instance = new FileInspector();

    private static FileInspector getInstance() {
        return instance;
    }

    public static CrawlFile inspect(Path p) {
        return getInstance().getCrawlFile(p);
    }

    private CrawlFile getCrawlFile(Path p) {
        CrawlFile c = new CrawlFile();
        try {
            c.setName(p.getFileName().toString());
            PosixFileAttributes attrs = Files.readAttributes(p,
                    PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            c.setSize(attrs.size());
            c.setType(getFileType(c, attrs));
            c.setAtime(new Timestamp(attrs.lastAccessTime().toMillis()));
            c.setCtime(new Timestamp(attrs.creationTime().toMillis()));
            c.setMtime(new Timestamp(attrs.lastModifiedTime().toMillis()));
            c.setMode(getMode(attrs));
            c.setUid(getOwner(attrs));
            c.setGid(getGroup(attrs));
//            c.setDigest();
            if (c.getType() == CrawlFile.FileType.SYMBOLIC_LINK) {
                c.setLinkTarget(Files.readSymbolicLink(p).toString());
            }
            c.setMissing(false);
        } catch (IOException e) {
            //
        }
        return c;
    }

    private CrawlFile.FileType getFileType(CrawlFile file, PosixFileAttributes attrs) {
        if (attrs.isRegularFile()) {
            return CrawlFile.FileType.REGULAR_FILE;
        }
        if (attrs.isDirectory()) {
            return CrawlFile.FileType.DIRECTORY;
        }
        if (attrs.isSymbolicLink()) {
            return CrawlFile.FileType.SYMBOLIC_LINK;
        }
        return CrawlFile.FileType.OTHER;
    }

    private Long getOwner(PosixFileAttributes attrs) {
        DbPrincipalCache cache = DbPrincipalCache.getInstance();
        DbPrincipal p = new DbPrincipal(attrs.owner());
        p = cache.lookup(p);
        return p.getId();
    }

    private Long getGroup(PosixFileAttributes attrs) {
        DbPrincipalCache cache = DbPrincipalCache.getInstance();
        DbPrincipal p = new DbPrincipal(attrs.group());
        p.setGroup(true);
        p = cache.lookup(p);
        return p.getId();
    }

    private Integer getMode(PosixFileAttributes attrs) {
        int mode = 0;
        for (PosixFilePermission p : attrs.permissions()) {
            switch(p) {
                case OWNER_READ -> mode |= 0x100;
                case OWNER_WRITE -> mode |= 0x80;
                case OWNER_EXECUTE -> mode |= 0x40;
                case GROUP_READ -> mode |= 0x20;
                case GROUP_WRITE -> mode |= 0x10;
                case GROUP_EXECUTE -> mode |= 8;
                case OTHERS_READ -> mode |= 4;
                case OTHERS_WRITE -> mode |= 2;
                case OTHERS_EXECUTE -> mode |= 1;
            }
        }
        return mode;
    }

    public static CrawlFile applyDirectory(CrawlFile file, Directory dir) {
        file.setPathId(dir.getId());
        return file;
    }
}
