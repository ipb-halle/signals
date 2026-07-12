/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.Config;
import de.ipb_halle.jcrawler.db.Acl;
import de.ipb_halle.jcrawler.linux.LinuxCalls;
import de.ipb_halle.jcrawler.linux.UnixError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;

/**
 *
 * @author fblocal
 */
public class LinuxNFS4AclHandler implements AclHandler {

    private final static int ACL_BUFFER_SIZE = 4196;
    private final static LinuxNFS4AclHandler instance = new LinuxNFS4AclHandler();
    private final static String NFS4_ACL_ATTR = "system.nfs4_acl";

    private LinuxNFS4AclHandler() {
    }

    public static AclHandler getInstance() {
        return instance;
    }

    @Override
    public Acl getAcl(Path path) throws IOException {
        Acl acl = new Acl();
        byte[] rawBuffer = readRawAttribute(path.toString());
        acl.setRawAttribute(rawBuffer);
        acl.setAcl(AclConverter.getInstance().parseAcl(rawBuffer));
        return acl;
    }

    private byte[] readRawAttribute(String filename) throws IOException {
        byte[] buffer = new byte[ACL_BUFFER_SIZE];
        int size = LinuxCalls.readAttribute(filename, NFS4_ACL_ATTR, buffer);
        if (size > 0) {
            // prepend FlavorId and size to buffer
            ByteBuffer tmp = ByteBuffer.allocate(ACL_BUFFER_SIZE + 8)
                    .putInt(Flavor.LinuxNFS4.getFlavorId())
                    .putInt(size)
                    .put(buffer, 0, size);

            return Arrays.copyOf(tmp.array(), size + 8);
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
}
