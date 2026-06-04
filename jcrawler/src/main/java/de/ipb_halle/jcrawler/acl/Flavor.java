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
    Null(0, "de.ipb_halle.jcrawler.acl.NullAclHandler",
        new String[] {"linux", "windows", "windows 11"}),
    LinuxNFS4(1, "de.ipb_halle.jcrawler.acl.LinuxNFS4AclHandler",
        new String[] {"linux"}),
    LinuxPosix(2, "de.ipb_halle.jcrawler.acl.LinuxPosixAclHandler",
        new String[] {"linux"}),
    Windows(3, "de.ipb_halle.jcrawler.acl.WindowsAclHandler",
        new String[] {"windows", "windows 11"});

    private final int flavorId;
    private final String handlerClassName;
    private final String[] supportedPlatforms;

    private Flavor(int id, String className, String[] platforms) {
        flavorId = id;
        handlerClassName = className;
        supportedPlatforms = platforms;
    }

    public String getHandlerClassName() {
        return handlerClassName;
    }

    public int getFlavorId() {
        return flavorId;
    }

    public boolean isSupported(String platform) {
        for (String p : supportedPlatforms) {
            if (p.equals(platform)) {
                return true;
            }
        }
        return false;
    }

    public static Flavor valueOf(int id) {
        for (Flavor f : values()) {
            if (f.getFlavorId() == id) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown ACL Flavor Id");
    }
}
