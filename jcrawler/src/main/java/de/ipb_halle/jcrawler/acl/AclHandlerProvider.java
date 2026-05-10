/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * OS independent handler of ACLs
 * @author frank
 */
public class AclHandlerProvider {


    private final static String OS_NAME_PROPERTY = "os.name";
    private final String platform;
    private static final AclHandlerProvider instance = new AclHandlerProvider();

    private AclHandlerProvider() {
        platform = System.getProperty(OS_NAME_PROPERTY).toLowerCase();
    }

    public static AclHandlerProvider getInstance() {
        return instance;
    }

    public AclHandler getHandler(String flavorName) {
        Flavor flavor;
        if (flavorName == null) {
            flavor = getFlavor();
        } else {
            flavor = Flavor.valueOf(flavorName);
        }
        if ((flavor == null) || (! flavor.isSupported(platform))) {
            throw new IllegalArgumentException("Invalid Flavor or Flavor not supported by platform");
        }
        try {
            Class<?> handlerClass = Class.forName(flavor.getHandlerClassName());
            Method method = handlerClass.getDeclaredMethod("getInstance");
            AclHandler instance = (AclHandler) method.invoke(null, new Object[0]);
            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    protected Flavor getFlavor() {
        switch (platform) {
            case "windows" -> { return Flavor.Windows; }
            case "windows 11" -> { return Flavor.Windows; }
            case "linux" -> { return Flavor.Null; }
            default -> throw new RuntimeException("Unknown platform %s"
                .formatted(System.getProperty(OS_NAME_PROPERTY)));
        }
    }

    protected String getPlatform() {
        return platform;
    }
}
