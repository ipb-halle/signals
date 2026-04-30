/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

/**
 *
 * @author fbroda
 */
public class MissingFilePurge extends SqlCommand {

    private final static String QUERY = """
        DELETE FROM files AS f
        USING directories AS d, namespaces AS n
        WHERE n.id=d.namespace_id AND f.path_id=d.id AND f.missing=true AND n.name=?""";

    public MissingFilePurge() {
        super(QUERY);
    }
}
