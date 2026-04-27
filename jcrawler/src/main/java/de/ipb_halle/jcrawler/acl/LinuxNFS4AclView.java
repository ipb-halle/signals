/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

/**
 *
 * @author fblocal
 */
public class LinuxNFS4AclView implements AclFileAttributeView {

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

    private final static String NFS4_ACL = "system.nfs4_acl";
    private final static int ACL_BUFFER_SIZE = 4196;

    static {
        System.loadLibrary("LinuxNFS4Acl");
    }

    private final byte[] rawAttributeBuffer;

    public LinuxNFS4AclView(Path path) throws IOException {
        this.rawAttributeBuffer = readRawAttribute(path.toString());
    }

    @Override
    public String name() {
        return NFS4_ACL;
    }

    @Override
    public List<AclEntry> getAcl() throws IOException {
        return NFS4AclParser.getInstance().parseAcl(rawAttributeBuffer);
    }

    @Override
    public void setAcl(List<AclEntry> list) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOwner(UserPrincipal up) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] getRawAttribute() {
        return rawAttributeBuffer;
    }


    public static byte[] readRawAttribute(String filename) throws IOException {
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

    @Override
    public String toString() {
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
