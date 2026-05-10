/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.db.Acl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;

/**
 *
 * @author frank
 */
public class WindowsAclHandler implements AclHandler {

    private final static WindowsAclHandler instance = new WindowsAclHandler();

    private WindowsAclHandler() {

    }

    public static AclHandler getInstance() {
        return instance;
    }

    @Override
    public Acl getAcl(Path path) throws IOException {
        AclFileAttributeView aclView = Files.getFileAttributeView(
                path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        Acl acl = new Acl();
        acl.setAcl(aclView.getAcl());
        acl.setRawAttribute(AclConverter.getInstance()
                .buildRawAttribute(acl.getAcl()));
        return acl;
    }
}
