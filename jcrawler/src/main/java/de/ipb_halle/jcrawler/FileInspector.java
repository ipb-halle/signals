/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.acl.LinuxNFS4AclView;
import de.ipb_halle.jcrawler.db.Acl;
import de.ipb_halle.jcrawler.db.AclCache;
import de.ipb_halle.jcrawler.db.CrawlFile;
import de.ipb_halle.jcrawler.db.DbPrincipal;
import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import de.ipb_halle.jcrawler.db.Directory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author fbroda
 */
public class FileInspector {

    private final static FileInspector instance = new FileInspector();
    private final static int BUFFER_SIZE = 65536;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static FileInspector getInstance() {
        return instance;
    }

    public static CrawlFile inspect(Path p, DigestAlgorithm algorithm) {
        return getInstance().getCrawlFile(p, algorithm);
    }

    private CrawlFile getCrawlFile(Path p, DigestAlgorithm algorithm) {
        CrawlFile c = new CrawlFile();
        try {
            c.setName(p.getFileName().toString());
            PosixFileAttributes attrs = Files.readAttributes(p,
                    PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            c.setType(getFileType(attrs));
            c.setAtime(new Timestamp(attrs.lastAccessTime().toMillis()));
            c.setCtime(new Timestamp(attrs.creationTime().toMillis()));
            c.setMtime(new Timestamp(attrs.lastModifiedTime().toMillis()));
            c.setMode(getMode(attrs));
            c.setUid(getOwner(attrs));
            c.setGid(getGroup(attrs));
            c.setMissing(false);
            if (c.getType() == CrawlFile.FileType.SYMBOLIC_LINK) {
                c.setLinkTarget(Files.readSymbolicLink(p).toString());
            }
            if (c.getType() == CrawlFile.FileType.REGULAR_FILE) {
                c.setSize(attrs.size());
                if (algorithm != null) {
                    c.setDigest(digest(p, algorithm));
                }
            } else {
                c.setSize(0L);
                c.setDigest(null);
            }
            c.setAclId(getAclId(p));
        } catch (NoSuchAlgorithmException | IOException e) {
            //
            logger.warn(e.getMessage());
            throw new RuntimeException("error in inspector");
        }
        return c;
    }

    private CrawlFile.FileType getFileType(PosixFileAttributes attrs) {
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

    private Long getAclId(Path path) throws IOException {
        byte[] rawAcl = new LinuxNFS4AclView(path).getRawAttribute();
        Acl acl = new Acl();
        if (rawAcl == null) {
            throw new NullPointerException("Was erlaube FileInspector!");
        }
        acl.setRawAttribute(rawAcl);
        acl = AclCache.getInstance().lookup(acl);
        if (acl == null) {
            return null;
        }
        return acl.getId();
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

    public byte[] digest(Path path, DigestAlgorithm algorithm)
            throws IOException, NoSuchAlgorithmException {

        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
            MessageDigest md = algorithm.newDigest();
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE); // Direct für native IO

            while (fc.read(buffer) != -1) {
                buffer.flip();
                md.update(buffer);
                buffer.clear();
            }
            return md.digest();
        }
    }
}
