/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.Main;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;

/**
 *
 * @author fblocal
 */
public class LinuxNFS4AclHandler implements AclHandler {

    public enum UnixError {
        ENOENT(2, "No such file or directory"),
        E2BIG(7, "Argument list too long"),
        EBADF(9, "Bad file number"),
        EACCES(13, "Permission denied"),
        EFAULT(14, "Bad address"),
        ENOTDIR(20, "Not a directory"),
        ERANGE(34, "Math result not representable"),
        ENAMETOOLONG(36, "File name too long"),
        ELOOP(40, "Too many symbolic links encountered"),
        ENODATA(61, "No data available"),
        ERROR_PATH(10001, "Invalid path argument"),
        ERROR_BUFFER(10002, "Invalid buffer argument"),
        UNKNOWN(0, "Unknown error");

        private final int errno;
        private final String description;

        private UnixError(int errno, String description) {
            this.errno = errno;
            this.description = description;
        }

        public static UnixError byErrNo(int errno) {
            for (UnixError e : values()) {
                if (e.errno == -errno) {
                    return e;
                }
            }
            return UNKNOWN;
        }

        public String getDescription() {
            return description;
        }
    }

    private final static int ACL_BUFFER_SIZE = 4196;
    private final static LinuxNFS4AclHandler instance;

    static {
        System.loadLibrary("LinuxNFS4Acl.%s".formatted(Main.getProjectVersion()));
        instance = new LinuxNFS4AclHandler();
    }

    private LinuxNFS4AclHandler() {
    }

    public AclHandler getInstance() {
        return instance;
    }

    @Override
    public byte[] getRawAttribute(Path path) throws IOException {
        return readRawAttribute(path.toString());
    }

    private byte[] readRawAttribute(String filename) throws IOException {
        byte[] buffer = new byte[ACL_BUFFER_SIZE];
        int size = readAttribute(filename, buffer);
        if (size > 0) {
            return Arrays.copyOf(buffer, size);
        }
        if (size == 0) {
            return null;
        }
        throw new IOException (UnixError.byErrNo(size).getDescription());
    }

    public String toString(byte[] rawAttributeBuffer) {
        StringBuilder sb = new StringBuilder();
        HexFormat format = HexFormat.ofDelimiter(" ");
        if (rawAttributeBuffer != null) {
            int len = rawAttributeBuffer.length;
            for (int i = 0; i < len; i += 16) {
                sb.append("%04x  ".formatted(i));
                sb.append(format.formatHex(rawAttributeBuffer,
                        i,
                        (len > (i + 16)) ? i + 16 : len));
                sb.append("\n");
            }
        } else {
            sb.append("--- EMPTY ---");
        }
        return sb.toString();
    }

    private static native int readAttribute(String filename, byte[] buffer);
}
