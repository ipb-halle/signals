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
import java.util.ArrayList;

/**
 *
 * @author fbroda
 */
public class NullAclHandler implements AclHandler {

    @Override
    public Acl getAcl(Path path) throws IOException {
        Acl acl = new Acl();
        acl.setAcl(new ArrayList<> ());
        acl.setRawAttribute(new byte[4]);
        return acl;
    }

}
