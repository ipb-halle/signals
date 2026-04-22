/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import de.ipb_halle.jcrawler.db.CrawlFile;
import de.ipb_halle.jcrawler.db.CrawlFileByDir;
import de.ipb_halle.jcrawler.db.CrawlFileCreate;
import de.ipb_halle.jcrawler.db.CrawlFileUpdate;
import de.ipb_halle.jcrawler.db.DbPrincipal;
import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import de.ipb_halle.jcrawler.db.Directory;
import de.ipb_halle.jcrawler.db.DirectoryByName;
import de.ipb_halle.jcrawler.db.DirectoryCreate;
import de.ipb_halle.jcrawler.db.Namespace;
import de.ipb_halle.jcrawler.db.NamespaceByName;
import de.ipb_halle.jcrawler.db.NamespaceCreate;
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
import java.util.Map;
import java.util.function.Function;
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
    private final String namespaceName;
    private final String path;
    private final String prefix;
    private Map<String, CrawlFile> files;
    private Directory directory;
    private final List<CrawlFile> directories;
    private Namespace namespace;

    public CrawlPathImpl(String path, String prefix, String namespace) {
        this.path = path;
        this.prefix = prefix;
        this.namespaceName = namespace;
        initPaths();
        statistics = new Statistics();
        directories = new ArrayList<> ();
    }

    @Override
    public void walkDirectory() {
        try {
            lookupNamespace();
            lookupPath();
            readDirectory();
            CrawlFileByDir byDir = (CrawlFileByDir) crawler.getQuery(QueryType.CrawlFileByDir);
            databaseFetch(byDir);
            matchFiles(byDir);
            handleNewFiles();
            processSubdirs();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
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
            if (this.logicalPath.charAt(0) == File.separatorChar) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid path/prefix combination");
    }

    private void lookupNamespace() throws SQLException {
        if ((namespace == null) || (namespace.getId() == null)) {
            NamespaceByName byName = (NamespaceByName) crawler.getQuery(QueryType.NamespaceByName);
            List<Object> arguments = new ArrayList<> ();
            arguments.add(namespaceName);
            byName.execute(arguments);
            if (byName.hasNext()) {
                namespace = byName.next();
                byName.close();
            } else {
                createNamespace();
            }
        }
    }

    private void createNamespace() throws SQLException {
        NamespaceCreate create = (NamespaceCreate) crawler.getQuery(QueryType.NamespaceCreate);
        namespace = new Namespace();
        namespace.setName(namespaceName);
        create.execute(namespace);
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
            byName.close();
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
        Path currentPath = Paths.get(path);
        Stream<Path> pathStream = Files.walk(currentPath, 1);
        files = pathStream.filter(p -> !currentPath.equals(p))
                .map(p -> getCrawlFile(p))
                .collect(Collectors.toMap(f -> f.getName(),
                        Function.identity()));
    }

    private CrawlFile getCrawlFile(Path p) {
        CrawlFile c = new CrawlFile();
        try {
            c.setName(p.getFileName().toString());
            c.setPathId(directory.getId());
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
            directories.add(file);
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
        return 0;
    }

    private void databaseFetch(CrawlFileByDir byDir) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(directory.getId());
        byDir.execute(arguments);
    }

    private void matchFiles(CrawlFileByDir byDir) throws SQLException {
        CrawlFileUpdate fileUpdate = (CrawlFileUpdate) crawler.getQuery(QueryType.CrawlFileUpdate);
        while (byDir.hasNext()) {
            CrawlFile fromDb = byDir.next();
            CrawlFile fromDir = files.remove(fromDb.getName());
            if (fromDir == null) {
                statistics.incrementVanished();
                fromDb.setMissing(true);
                fileUpdate.execute(fromDb);
            } else {
                if (! fromDb.deepEquals(fromDir)) {
                    statistics.incrementChanged();
                    fromDir.setId(fromDb.getId());
                    fileUpdate.execute(fromDir);
                }
            }
        }
        byDir.close();
    }

    private void handleNewFiles() throws SQLException {
        if (files.isEmpty()) {
            // must not start copy process with empty map!
            return;
        }
        CrawlFileCreate create = (CrawlFileCreate) crawler.getQuery(QueryType.CrawlFileCreate);
        create.begin();
        for (CrawlFile f : files.values()) {
            create.execute(f);
            statistics.incrementNew();
        }
        create.close();
    }

    private void processSubdirs() throws SQLException {
        for (CrawlFile f : directories) {
            Path p = Paths.get(path, f.getName());
            CrawlPathImpl subdir = new CrawlPathImpl(p.toString(), prefix, namespaceName);
            subdir.setNamespace(namespace);
            subdir.setCrawler(crawler);
            subdir.walkDirectory();
            statistics.accumulateStatistics(subdir.getStatistics());
        }
    }
}
