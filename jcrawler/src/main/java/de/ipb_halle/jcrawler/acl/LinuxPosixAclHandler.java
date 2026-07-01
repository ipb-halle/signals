/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.db.Acl;
import de.ipb_halle.jcrawler.linux.LinuxCalls;
import de.ipb_halle.jcrawler.linux.UnixError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 *
 * @author fbroda
 */
public class LinuxPosixAclHandler implements AclHandler {

    private final static LinuxPosixAclHandler instance = new LinuxPosixAclHandler();
    private final static int ACL_BUFFER_SIZE = 4096;
    private final static String POSIX_ACL_ATTR = "system.posix_acl_access";
    private final static String POSIX_DEFAULT_ACL_ATTR = "system.posix_acl_default";

    private LinuxPosixAclHandler() {

    }

    public static AclHandler getInstance() {
        return instance;
    }

    @Override
    public Acl getAcl(Path path) throws IOException {
        // process attributes 'system.posix_acl_default' and 'system.posix_acl_access'
        byte[] buffer = readRawAttribute(path);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private byte[] readRawAttribute(Path path) throws IOException {
        String filename = path.toString();
        boolean isDir = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
        byte[] accessBuffer = new byte[ACL_BUFFER_SIZE];
        byte[] defaultBuffer = new byte[ACL_BUFFER_SIZE];
        int defaultSize = 0;
        int accessSize = LinuxCalls.readAttribute(filename, POSIX_ACL_ATTR, accessBuffer);
        if (accessSize < 0) {
            throw new IOException (UnixError.byErrNo(accessSize).getDescription() + " (access ACL)");
        }
        if (isDir) {
            defaultSize = LinuxCalls.readAttribute(filename, POSIX_ACL_ATTR, defaultBuffer);
            if (defaultSize < 0) {
                throw new IOException (UnixError.byErrNo(defaultSize).getDescription() + " (default ACL)");
            }
        }
        if ((accessSize > 0) || (defaultSize > 0)) {
            // prepend FlavorId and size to buffer
            ByteBuffer tmp = ByteBuffer.allocate(ACL_BUFFER_SIZE)
                    .putInt(Flavor.LinuxPosix.getFlavorId())
                    .putInt(accessSize)
                    .putInt(defaultSize);
            if (accessSize > 0) {
                tmp.put(accessBuffer, 0, accessSize);
            }
            if (defaultSize > 0) {
                tmp.put(defaultBuffer, 0, defaultSize);
            }
            return Arrays.copyOf(tmp.array(), accessSize + defaultSize + 12);
        }
        return null;
    }
}
