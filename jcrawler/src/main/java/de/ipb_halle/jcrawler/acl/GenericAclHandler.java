/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * OS independent handler of ACLs
 * @author frank
 */
public class GenericAclHandler implements AclHandler {

    private enum Platform {
        Linux("de.ipb_halle.jcrawler.acl.LinuxNFS4AclHandler"),
        Windows("java.nio.file.attribute.WindowsAclHandler");

        String handlerClassName;

        private Platform(String className) {
            handlerClassName = className;
        }
        public String getHandlerClassName() {
            return handlerClassName;
        }
    }

    private final static String OS_NAME_PROPERTY = "os.name";
    private final AclHandler handler;
    private static final AclHandler genericInstance = new GenericAclHandler();

    private GenericAclHandler() {
        handler = getHandler();
    }

    public static AclHandler getInstance() {
        return genericInstance;
    }

    private AclHandler getHandler() {
        Platform platform = getPlatform();
        try {
            Class<?> handlerClass = Class.forName(platform.getHandlerClassName());
            Method method = handlerClass.getDeclaredMethod("getInstance");
            AclHandler instance = (AclHandler) method.invoke(null, new Object[0]);
            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private Platform getPlatform() {
        switch (System.getProperty(OS_NAME_PROPERTY).toLowerCase()) {
            case "windows" -> { return Platform.Windows; }
            case "linux" -> { return Platform.Linux; }
            default -> throw new RuntimeException("Unknown platform %s"
                .formatted(System.getProperty(OS_NAME_PROPERTY)));
        }
    }

    @Override
    public byte[] getRawAttribute(Path path) throws IOException {
        return handler.getRawAttribute(path);
    }
}
