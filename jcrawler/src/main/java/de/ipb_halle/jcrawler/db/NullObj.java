/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.sql.SQLType;

/**
 *
 * @author fblocal
 */
public class NullObj {
    private final SQLType type;

    public NullObj(SQLType type) {
        this.type = type;
    }

    public SQLType getType() {
        return this.type;
    }
}
