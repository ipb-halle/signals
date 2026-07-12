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
public enum UnixError {
    ENOENT(2, "No such file or directory"),
    E2BIG(7, "Argument list too long"),
    EBADF(9, "Bad file number"),
    EACCES(13, "Permission denied"),
    EFAULT(14, "Bad address"),
    ENOTDIR(20, "Not a directory"),
    ERANGE(34, "Math result not representable"),
    ENAMETOOLONG(36, "File name too long"),
    ELOOP(40, "Too many symbolic links encountered"),
    ENODATA(61, "No data available"),
    ERROR_PATH(10001, "Invalid filename argument"),
    ERROR_ATTR(10002, "Invalid attribute name argument"),
    ERROR_BUFFER(10003, "Invalid buffer argument"),
    UNKNOWN(0, "Unknown error");

    private final int errno;
    private final String description;

    private UnixError(int errno, String description) {
        this.errno = errno;
        this.description = description;
    }

    public static UnixError byErrNo(int errno) {
        for (UnixError e : UnixError.values()) {
            if (e.errno == -errno) {
                return e;
            }
        }
        return UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

    public int getErrNo() {
        return -errno;
    }
}
