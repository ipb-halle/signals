/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import de.ipb_halle.jcrawler.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HexFormat;
import java.util.Properties;

/**
 *
 * @author fblocal
 */
public class SqlConnection {

    private final static String DB_CONNECTION_INFO = "dbConnectionInfo";
    private final static String DB_PASSWORD = "dbPassword";
    private final static String DB_USER = "dbUser";
    private final static String NULL = "\\N";

    public static Connection getConnection(Config config) throws SQLException {
        String user = config.getConfigString(DB_USER, null);
        String password = config.getConfigString(DB_PASSWORD, null);
        String connInfo = config.getConfigString(DB_CONNECTION_INFO, null);
        if ((connInfo == null) || (user == null) || (password == null)) {
            throw new IllegalStateException("ConnectionInfo, user or password missing");
        }
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        return DriverManager.getConnection(connInfo, props);
    }

    public static String copyEscape(Boolean b) {
        if (b == null) {
            return NULL;
        }
        return b ? "true" : "false";
    }

    public static String copyEscape(byte[] b) {
        if (b == null) {
        return NULL;
        }
        return "\\\\x%s".formatted(HexFormat.of().formatHex(b));
    }

    public static String copyEscape(Integer i) {
        if (i == null) {
            return NULL;
        }
        return i.toString();
    }

    public static String copyEscape(Long l) {
        if (l == null) {
            return NULL;
        }
        return l.toString();
    }

    public static String copyEscape(String st) {
        if (st == null) {
            return NULL;
        }
        return st.replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\0", "\\0");
    }

    public static String copyEscape(Timestamp t) {
        if (t == null) {
            return NULL;
        }
        return t.toString();
    }
}
