/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

/**
 * Helper class for parsing and transforming POSIX Acls into lists
 * of java.nio.file.attribute.AclEntry
 *
 * @author fblocal
 */
public class PosixAcl {
    private enum PosixAceType {
        MASK, GROUP, USER;
    }
    private class PosixAce implements Comparable {

       long principal;
       PosixAceType type;
       boolean read;
       boolean write;
       boolean execute;

       public PosixAce(long p, PosixAceType t, boolean r, boolean w, boolean x) {
           principal = p;
           type = t;
           read = r;
           write = w;
           execute = x;
       }

        @Override
        public int compareTo(Object t) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
   }
}
