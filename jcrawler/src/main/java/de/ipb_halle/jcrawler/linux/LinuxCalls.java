/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.linux;

/**
 *
 * @author fblocal
 */
public class LinuxCalls {

    public static native int readAttribute(String filename, String attrname, byte[] buffer);

}
