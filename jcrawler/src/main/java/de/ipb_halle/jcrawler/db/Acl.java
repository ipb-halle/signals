/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.nio.file.attribute.AclEntry;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author fblocal
 */
public class Acl {
    private Long id;
    private byte[] rawAttribute;
    private List<AclEntry> acl;

    @Override
    public int hashCode() {
        return Arrays.hashCode(rawAttribute);
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
        Acl other = (Acl) obj;
        return Arrays.equals(this.rawAttribute, other.rawAttribute);
    }

    public boolean isValid() {
        return ((rawAttribute != null) && (acl != null));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AclEntry> getAcl() {
        return acl;
    }

    public void setAcl(List<AclEntry> acl) {
        this.acl = acl;
    }

    public byte[] getRawAttribute() {
        return rawAttribute;
    }

    public void setRawAttribute(byte[] rawAttribute) {
        this.rawAttribute = rawAttribute;
    }


}
