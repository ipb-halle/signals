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
import de.ipb_halle.jcrawler.db.Directory;
import de.ipb_halle.jcrawler.db.DirectoryByName;
import de.ipb_halle.jcrawler.db.DirectoryCreate;
import de.ipb_halle.jcrawler.db.DirectoryUpdate;
import de.ipb_halle.jcrawler.db.MissingSubdir;
import de.ipb_halle.jcrawler.db.MissingSubdirUpdate;
import de.ipb_halle.jcrawler.db.Namespace;
import de.ipb_halle.jcrawler.db.NamespaceByName;
import de.ipb_halle.jcrawler.db.NamespaceCreate;
import de.ipb_halle.jcrawler.db.QueryType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author frank
 */
public class CrawlPathImpl implements CrawlPath {

    private final Statistics statistics;
    private final PathParameters parameters;
    private Crawler crawler;
    private Map<String, CrawlFile> files;
    private List<CrawlFile> directories;
    private Directory directory;
    private final Logger logger;

    public CrawlPathImpl(PathParameters parameters) {
        this.parameters = parameters;
        statistics = new Statistics();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void walkDirectory() {
        try {
            lookupNamespace();
            lookupPath();
            if (checkCanSkip()) {
                logger.info("Skipping directory: {}", parameters.getPath());
                return;
            }
            readDirectory();
            CrawlFileByDir byDir = (CrawlFileByDir) crawler.getQuery(QueryType.CrawlFileByDir);
            databaseFetch(byDir);
            matchFiles(byDir);
            handleNewFiles();
            processSubdirs();
            updateDirectory();
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

    private void lookupNamespace() throws SQLException {
        Namespace namespace = parameters.getNamespace();
        if ((namespace == null) || (namespace.getId() == null)) {
            NamespaceByName byName = (NamespaceByName) crawler.getQuery(QueryType.NamespaceByName);
            List<Object> arguments = new ArrayList<> ();
            arguments.add(parameters.getNamespaceName());
            byName.execute(arguments);
            if (byName.hasNext()) {
                Namespace nspc = byName.next();
                logger.debug("found namespace: {} --> {}", nspc.getName(), nspc.getId());
                parameters.setNamespace(nspc);
                byName.close();
            } else {
                createNamespace();
            }
        }
    }

    private void createNamespace() throws SQLException {
        NamespaceCreate create = (NamespaceCreate) crawler.getQuery(QueryType.NamespaceCreate);
        Namespace namespace = new Namespace();
        namespace.setName(parameters.getNamespaceName());
        create.execute(namespace);
        logger.info("created namespace: {} --> {}", namespace.getName(), namespace.getId());
        parameters.setNamespace(namespace);
    }

    private void lookupPath() throws SQLException {
        DirectoryByName byName = (DirectoryByName) crawler.getQuery(QueryType.DirectoryByName);
        List<Object> arguments = new ArrayList<> ();
        arguments.add(parameters.getNamespace().getId());
        arguments.add(parameters.getLogicalPath());
        byName.execute(arguments);
        if (byName.hasNext()) {
            directory = byName.next();
            logger.trace("found directory: {} --> {}", directory.getPath(), directory.getId());
            byName.close();
        } else {
            byName.close();
            createPath();
        }
    }

    private void createPath() throws SQLException {
        DirectoryCreate create = (DirectoryCreate) crawler.getQuery(QueryType.DirectoryCreate);
        directory = new Directory();
        directory.setPath(parameters.getLogicalPath());
        directory.setNamespaceId(parameters.getNamespace().getId());
        create.execute(directory);
        logger.debug("created directory {} --> {}", directory.getPath(), directory.getId());
    }

    private boolean checkCanSkip() {
        if (parameters.isFullScan()) {
            return false;
        }
        return directory.getChangeTime()
                .before(parameters.getScanCutOff());
    }

    private void readDirectory() throws IOException {
        Path currentPath = Paths.get(parameters.getPath());
        Stream<Path> pathStream = Files.walk(currentPath, 1);
        files = pathStream.filter(p -> !currentPath.equals(p))
                .map(p -> FileInspector.inspect(p, parameters))
                .map(f -> FileInspector.applyDirectory(f, directory))
                .collect(Collectors.toMap(f -> f.getName(),
                        Function.identity()));
        directories = files.values().stream()
                .filter(f -> f.isDirectory())
                .collect(Collectors.toList());
    }


    private void databaseFetch(CrawlFileByDir byDir) throws SQLException {
        List<Object> arguments = new ArrayList<> ();
        arguments.add(directory.getId());
        byDir.execute(arguments);
    }

    private void matchFiles(CrawlFileByDir byDir) throws SQLException {
        while (byDir.hasNext()) {
            CrawlFile fromDb = byDir.next();
            CrawlFile fromDir = files.remove(fromDb.getName());
            if (fromDir == null) {
                handleMissingFile(fromDb);
            } else {
                if (! fromDb.deepEquals(fromDir)) {
                    handleChangedFile(fromDb, fromDir);
                }
            }
        }
        byDir.close();
    }

    private void handleChangedFile(CrawlFile fromDb, CrawlFile fromDir) throws SQLException {
        CrawlFileUpdate fileUpdate = (CrawlFileUpdate) crawler.getQuery(QueryType.CrawlFileUpdate);
        statistics.incrementChanged();
        statistics.addChangedBytes(
                fromDir.getSize() - fromDb.getSize());
        // entry may have turned from directory into something different
        // --> entire subdirectory tree will be missing
        if (fromDb.isDirectory() && (! fromDir.isDirectory())) {
            handleMissingDirectory(fromDb);
        }
        fromDir.setId(fromDb.getId());
        fileUpdate.execute(fromDir);
    }

    private void handleMissingFile(CrawlFile fromDb) throws SQLException {
        CrawlFileUpdate fileUpdate = (CrawlFileUpdate) crawler.getQuery(QueryType.CrawlFileUpdate);
        statistics.incrementVanished();
        if (fromDb.isRegularFile()) {
            statistics.addVanishedBytes(fromDb.getSize());
        }
        if (fromDb.isDirectory()) {
            handleMissingDirectory(fromDb);
        }
        fromDb.setMissing(true);
        fileUpdate.execute(fromDb);
    }

    private void handleMissingDirectory(CrawlFile fromDb) throws SQLException {
        MissingSubdir subdir = new MissingSubdir();
        subdir.setNamespaceId(directory.getNamespaceId());
        subdir.setPath(Paths.get(directory.getPath(), fromDb.getName()).toString());
        MissingSubdirUpdate update = (MissingSubdirUpdate) crawler.getQuery(QueryType.MissingSubdirUpdate);
        update.execute(subdir);
        statistics.addVanishedBytes(subdir.getBytes());
        statistics.addVanishedEntities(subdir.getEntities());
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
            statistics.addNewBytes(f.getSize());
        }
        create.close();
    }

    private void processSubdirs() throws SQLException {
        for (CrawlFile f : directories) {
            Path path = Paths.get(parameters.getPath(), f.getName());
            PathParameters params = parameters.createPathParameters(path.toString());
            CrawlPathImpl subdir = new CrawlPathImpl(params);
            subdir.setCrawler(crawler);
            subdir.walkDirectory();
            statistics.accumulateStatistics(subdir.getStatistics());
        }
    }

    private void updateDirectory() throws SQLException {
        directory.setStatistics(statistics);
        DirectoryUpdate update = (DirectoryUpdate) crawler.getQuery(QueryType.DirectoryUpdate);
        update.execute(directory);
    }
}
