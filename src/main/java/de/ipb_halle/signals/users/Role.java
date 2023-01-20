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

import java.util.Iterator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * SNB role
 */

@Entity
@Table(name="roles")
public class Role implements IRole {

    // entity field names (NOT column names from SQL table)
    public final static String ROLE_NAME = "name";

    // JSON attributes
    public final static String ATTR_COUNTS = "counts";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_FLAGS = "flags";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_PRIVILEGES = "privileges";

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Set<RolePriv> privileges;

    @Column(name="json_string")
    private String jsonString;

    public Role() {
        privileges = new HashSet<> ();
        deleted = false;
        ldapRole = false;
    }

    public Role addPrivilege(RolePrivilege p) {
        privileges.add(new RolePriv(id,p));
        return this;
    }

    public void applyChangesFromSnb(Role snbRole) {
        description = snbRole.getDescription();
        name = snbRole.getName();
        privileges = snbRole.getPrivileges();
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role(%d) --> %s\n", id, name));
        sb.append("Privileges: ");
        int i = 0;
        String sep = "";
        Iterator<RolePriv> iter = privileges.iterator();
        while(iter.hasNext()) {
            sb.append(sep);
            sb.append(iter.next().getRolePrivilege().toString());
            i++;
            if (i % 4 == 0) {
                sep = ",\n            ";
            } else {
                sep = ", ";
            }
        }
        sb.append("\n==============================================================");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof IRole) {
            IRole other = (IRole) obj;
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

    public Set<RolePriv> getPrivileges() {
        return privileges;
    }

    public String getJsonString() {
        return jsonString;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    public boolean hasPrivilege(RolePrivilege p) {
        return privileges.contains(new RolePriv(id, p));
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isModified(Role role) {
        return ! (Objects.equals(name, role.getName())
            && Objects.equals(description, role.getDescription())
            && (deleted == role.isDeleted())
            && privileges.equals(role.getPrivileges()));
    }

    public boolean isLdapRole() {
        return ldapRole;
    }

    public void removePrivilege(RolePrivilege p) {
        privileges.remove(p);
    }

    public IRole setId(String i) {
        id = i;
        return this;
    }

    public void setDeleted(boolean b) {
        deleted = b;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }

    public void setLdapRole(boolean b) {
        ldapRole = b;
    }

    public void setName(String n) {
        name = n;
    }

    public void setPrivileges(Set<RolePriv> sp) {
        privileges = sp;
    }
}
