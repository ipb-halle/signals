/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals.users;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * SNB role
 */

@Entity
@Table(name="roles")
public class RoleEntity {


    @Id
    private String id;

    @Column
    private String description;

    @Column
    private String name;

    @Column(name="ldap_role")
    private boolean ldapRole;

    @Column
    private boolean deleted;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RoleEntity) {
            RoleEntity other = (RoleEntity) obj;
            return Objects.equals(id,other.getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isLdapRole() {
        return ldapRole;
    }

    public RoleEntity setId(String i) {
        id = i;
        return this;
    }

    public RoleEntity setDeleted(boolean b) {
        deleted = b;
        return this;
    }

    public RoleEntity setDescription(String d) {
        description = d;
        return this;
    }

    public RoleEntity setLdapRole(boolean b) {
        ldapRole = b;
        return this;
    }

    public RoleEntity setName(String n) {
        name = n;
        return this;
    }
}
