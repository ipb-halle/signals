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
import java.util.List;

/**
 *
 * @author fblocal
 */
public class LinuxNFS4AclView implements AclFileAttributeView {

    private final static String NFS4_ACL = "system.nfs4_acl";
    private final static int ACL_BUFFER_SIZE = 8192;

    private final Path path;
    private final boolean followLinks;

    LinuxNFS4AclView(Path path, boolean followLinks) {
        this.path = path;
        this.followLinks = followLinks;
    }

    @Override
    public String name() {
        return NFS4_ACL;
    }

    @Override
    public List<AclEntry> getAcl() throws IOException {
        byte[] buffer = new byte[ACL_BUFFER_SIZE];
        int result = readAttribute(path.toString(), buffer);

        throw new UnsupportedOperationException("Not supported yet.");
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

    private native int readAttribute(String filename, byte[] buffer);
}
