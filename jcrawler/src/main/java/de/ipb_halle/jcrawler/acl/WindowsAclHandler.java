/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

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
    public byte[] getRawAttribute(Path path) throws IOException {
        AclFileAttributeView aclView = Files.getFileAttributeView(
                path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        return AclConverter.getInstance().buildRawAttribute(aclView.getAcl());
    }
}
