/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author fblocal
 */
public class CrawlFileCreate extends SqlQuery<CrawlFile> implements Runnable {

    private final static String QUERY = """
COPY files (path_id, size, type, mode, uid,
  gid, atime, ctime, mtime, digest, name,
  link_target, acl_id) FROM STDIN""";

    //                                           p   s   t   m   u   g   a   c   m   d   n   l   a
    private final static String COPY_TEMPLATE = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection;
    private PipedOutputStream outputStream;
    private PipedInputStream copyStream;
    private PrintStream printStream;
    private Thread copyThread;
    private boolean busy = true;


    public CrawlFileCreate() {
        super(QUERY);
    }

    @Override
    public void prepare(Connection c) throws SQLException {
        connection = c;
        busy = false;
    }

    @Override
    public void execute(CrawlFile file) throws SQLException {
        if (busy) {
            printStream.append(COPY_TEMPLATE.formatted(
                    SqlConnection.copyEscape(file.getPathId()),
                    SqlConnection.copyEscape(file.getSize()),
                    SqlConnection.copyEscape(file.getType().getTypeId()),
                    SqlConnection.copyEscape(file.getMode()),
                    SqlConnection.copyEscape(file.getUid()),
                    SqlConnection.copyEscape(file.getGid()),
                    SqlConnection.copyEscape(file.getAtime()),
                    SqlConnection.copyEscape(file.getCtime()),
                    SqlConnection.copyEscape(file.getMtime()),
                    SqlConnection.copyEscape(file.getDigest()),
                    SqlConnection.copyEscape(file.getName()),
                    SqlConnection.copyEscape(file.getLinkTarget()),
                    SqlConnection.copyEscape(file.getAclId())));
            logger.trace("copied {}", file.getName());
        } else {
            throw new IllegalStateException("Not initialized.");
        }
    }

    @Override
    public void close() {
        if (busy) {
            try {
                logger.trace("closing COPY transaction");
                printStream.append("\\.\n");
                printStream.flush();
                printStream.close();
                copyThread.join();
                logger.trace("copy-thread joined");
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            busy = false;
        } else {
            throw new IllegalStateException("Instance is not busy");
        }
    }

    public synchronized void begin() {
        if (! busy) {
            try {
                logger.trace("starting COPY transaction");
                outputStream = new PipedOutputStream();
                printStream = new PrintStream(outputStream);
                copyStream = new PipedInputStream(outputStream);
                copyThread = new Thread(this);
                copyThread.start();
                Thread.yield();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
            busy = true;
        } else {
            throw new IllegalStateException("Instance is busy");
        }
    }

    @Override
    public void run() {
        try {
            logger.trace("copy-thread started");
            CopyManager cp = new CopyManager((BaseConnection) connection);
            cp.copyIn(getQuery(), copyStream);
            logger.trace("copy-thread completed");
        } catch (IOException | SQLException e) {
            throw new RuntimeException("SQL error: " + e.getMessage());
        }
    }

    @Override
    public void execute(List<Object> arguments) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    protected CrawlFile getRecord() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public QueryType getType() {
        return QueryType.CrawlFileCreate;
    }



}
