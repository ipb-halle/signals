/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import de.ipb_halle.jcrawler.acl.NFS4AclParser;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
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
public class AclDetail extends SqlQuery<Acl> implements Runnable {

    private final static String QUERY = """
COPY acl_details (acl_id, seq, principal_id, type,
  file_inherit, dir_inherit, no_propagate, inherit_only,
  read_data, write_data, append_data, execute,
  delete, delete_child, read_attr, write_attr,
  read_named_attr, write_named_attr,
  read_acl, write_acl, write_owner, synchronize) FROM STDIN""";

    //                                           i   S   p   t   f   f   f   f   r   w   a   x   d   D   t   T   n   N   c   C   o   y
    private final static String COPY_TEMPLATE = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection;
    private PipedOutputStream outputStream;
    private PipedInputStream copyStream;
    private PrintStream printStream;
    private Thread copyThread;
    private boolean busy = true;

    @Override
    public void prepare(Connection c) throws SQLException {
        connection = c;
        busy = false;
    }

    @Override
    public void execute(Acl acl) throws SQLException {
        if (busy) {
            DbPrincipalCache cache = DbPrincipalCache.getInstance();
            int i = 0;
            for (AclEntry ace : NFS4AclParser.getInstance()
                    .parseAcl(acl.getRawAttribute())) {
                DbPrincipal principal = new DbPrincipal(ace.principal().getName());
                principal = cache.lookup(principal);

                printStream.append(COPY_TEMPLATE.formatted(
                    SqlConnection.copyEscape(acl.getId()),
                    SqlConnection.copyEscape(i),
                    SqlConnection.copyEscape(principal.getId()),
                    SqlConnection.copyEscape(ace.type().ordinal()),
                    SqlConnection.copyEscape(ace.flags().contains(AclEntryFlag.FILE_INHERIT)),
                    SqlConnection.copyEscape(ace.flags().contains(AclEntryFlag.DIRECTORY_INHERIT)),
                    SqlConnection.copyEscape(ace.flags().contains(AclEntryFlag.INHERIT_ONLY)),
                    SqlConnection.copyEscape(ace.flags().contains(AclEntryFlag.NO_PROPAGATE_INHERIT)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.READ_DATA)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.WRITE_DATA)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.APPEND_DATA)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.EXECUTE)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.DELETE)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.DELETE_CHILD)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.READ_ATTRIBUTES)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.WRITE_ATTRIBUTES)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.READ_NAMED_ATTRS)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.WRITE_NAMED_ATTRS)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.READ_ACL)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.WRITE_ACL)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.WRITE_OWNER)),
                    SqlConnection.copyEscape(ace.permissions().contains(AclEntryPermission.SYNCHRONIZE))));
                i++;
            }
            logger.trace("saved ACL detail for aclId {}", acl.getId());
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
            cp.copyIn(QUERY, copyStream);
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
    protected Acl getRecord() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public QueryType getType() {
        return QueryType.AclDetail;
    }



}
