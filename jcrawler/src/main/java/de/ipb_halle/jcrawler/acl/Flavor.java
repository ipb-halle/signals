/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

/**
 *
 * @author fbroda
 */
 public enum Flavor {
    LinuxNFS4("de.ipb_halle.jcrawler.acl.LinuxNFS4AclHandler",
        new String[] {"linux"}),
    LinuxPosix("de.ipb_halle.jcrawler.acl.LinuxPosixAclHandler",
        new String[] {"linux"}),
    Null("de.ipb_halle.jcrawler.acl.NullAclHandler",
        new String[] {"linux", "windows", "windows 11"}),
    Windows("de.ipb_halle.jcrawler.acl.WindowsAclHandler",
        new String[] {"windows", "windows 11"});

    String handlerClassName;
    String[] supportedPlatforms;

    private Flavor(String className, String[] platforms) {
        handlerClassName = className;
        supportedPlatforms = platforms;
    }

    public String getHandlerClassName() {
        return handlerClassName;
    }

    public boolean isSupported(String platform) {
        for (String p : supportedPlatforms) {
            if (p.equals(platform)) {
                return true;
            }
        }
        return false;
    }
}
