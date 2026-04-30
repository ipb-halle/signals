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
public class MissingSubdirPurge extends SqlCommand {

    private final static String QUERY = """
        DELETE FROM directories AS d
        USING namespaces AS n
        WHERE n.id=d.namespace_id AND d.missing=true AND n.name=?""";

    public MissingSubdirPurge() {
        super(QUERY);
    }
}
