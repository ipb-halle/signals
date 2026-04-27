/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author fblocal
 */
public enum DigestAlgorithm {
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256");

    private final String jdkName;

    DigestAlgorithm(String name) {
        jdkName = name;
    }

    public MessageDigest newDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(jdkName);
    }

    public static DigestAlgorithm byName(String name) {
        for (DigestAlgorithm algo : values()) {
            if (algo.toString().equals(name) || (algo.jdkName.equals(name))) {
                return algo;
            }
        }
        if ((name == null)
                || (name.length() == 0)
                || name.toLowerCase().equals("null")) {
            return null;
        }
        throw new RuntimeException("Unknown algorithm in configuration");
    }
}
