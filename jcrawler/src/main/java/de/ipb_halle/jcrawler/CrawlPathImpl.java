/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.CrawlFile;
import de.ipb_halle.jcrawler.db.Directory;
import de.ipb_halle.jcrawler.db.DirectoryByName;
import de.ipb_halle.jcrawler.db.DirectoryCreate;
import de.ipb_halle.jcrawler.db.Namespace;
import de.ipb_halle.jcrawler.db.QueryType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author frank
 */
public class CrawlPathImpl implements CrawlPath {

    private final Statistics statistics;
    private Crawler crawler;
    private String logicalPath;
    private final String path;
    private final String prefix;
    private Set<CrawlFile> files;
    private Directory directory;
    private Namespace namespace;

    public CrawlPathImpl(String path, String prefix) {
        this.path = path;
        this.prefix = prefix;
        initPaths();
        statistics = new Statistics();
    }

    @Override
    public void walkDirectory() {
        try {
            lookupPath();
            readDirectory();
        } catch (IOException | SQLException e) {
            // ignore
        }
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public void setCrawler(Crawler crawler) {
        this.crawler = crawler;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    private void initPaths() {
        int len = prefix.length();
        if ((path.length() >= len)
                && (path.startsWith(prefix))) {
            this.logicalPath = path.substring(len);
            if (this.logicalPath.charAt(0) == File.pathSeparatorChar) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid path/prefix combination");
    }

    private void lookupPath() throws SQLException {
        DirectoryByName byName = (DirectoryByName) crawler.getQuery(QueryType.DirectoryByName);
        List<Object> arguments = new ArrayList<> ();
        arguments.add(namespace.getId());
        arguments.add(logicalPath);
        byName.execute(arguments);
        if (byName.hasNext()) {
            directory = byName.next();
            byName.close();
        } else {
            createPath();
        }
    }

    private void createPath() throws SQLException {
        DirectoryCreate create = (DirectoryCreate) crawler.getQuery(QueryType.DirectoryCreate);
        directory = new Directory();
        directory.setPath(logicalPath);
        directory.setNamespaceId(namespace.getId());
        create.execute(directory);
    }

    private void readDirectory() throws IOException {
        Stream<Path> pathStream = Files.walk(Paths.get(path), 0);
        files = pathStream.map(p -> getCrawlFile(p))
                .collect(Collectors.toSet());
    }

    private CrawlFile getCrawlFile(Path p) {
        CrawlFile c = new CrawlFile();
        try {
            c.setName(p.getFileName().toString());
            c.setPathId(directory.getId());
            PosixFileAttributes attrs = Files.readAttributes(p,
                    PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            c.setSize(attrs.size());
            c.setType(getFileType(attrs));
            c.setAtime(new Timestamp(attrs.lastAccessTime().toMillis()));
            c.setCtime(new Timestamp(attrs.creationTime().toMillis()));
            c.setMtime(new Timestamp(attrs.lastModifiedTime().toMillis()));
//            c.setMode(0);
//            c.setUID(0);
//            c.setGID(0);
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
}
