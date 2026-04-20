/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.security.Principal;
import java.util.Objects;


/**
 *
 * @author fblocal
 */
public class DbPrincipal  {

    public class SimplePrincipal implements Principal {
        private final String name;

        public SimplePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimplePrincipal other = (SimplePrincipal) obj;
            return Objects.equals(this.name, other.name);
        }

        @Override
        public String toString() {
            return String.format("SimplePrincipal:%s", name);
        }
    }

    private Long id;
    private Principal principal;
    private boolean group;
    private boolean everyone;
    private Long guid;

    private DbPrincipal() {
        group = false;
        everyone = false;
        id = null;
        guid = null;
    }

    public DbPrincipal(Principal p) {
        this();
        principal = p;
    }

    public DbPrincipal(String n) {
        this();
        principal = new SimplePrincipal(n);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return principal.getName();
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public boolean isEveryone() {
        return everyone;
    }

    public void setEveryone(boolean everyone) {
        this.everyone = everyone;
    }

    public Long getGuid() {
        return guid;
    }

    public void setGuid(Long guid) {
        this.guid = guid;
    }
}
