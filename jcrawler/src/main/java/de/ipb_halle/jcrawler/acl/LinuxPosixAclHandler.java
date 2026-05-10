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
import java.nio.file.Path;

/**
 *
 * @author fbroda
 */
public class LinuxPosixAclHandler implements AclHandler {

    @Override
    public Acl getAcl(Path path) throws IOException {
        // process attributes 'system.posix_acl_default' and 'system.posix_acl_access'
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
