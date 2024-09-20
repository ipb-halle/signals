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

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** 
 * SNB role privileges
 */

@Entity
@Table(name="role_privileges")
public class RolePriv {

    public final static String ROLE_PRIV_ID = "id";

    @EmbeddedId
    private RolePrivId id;

    public RolePriv() {
        id = new RolePrivId();
    }

    public RolePriv(String r, String p) {
        id = new RolePrivId();
        id.setRoleId(r);
        id.setRolePrivilege(p);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        RolePriv other = (RolePriv) o;
        return Objects.equals(getRoleId(), other.getRoleId())
            && Objects.equals(getRolePrivilege(), other.getRolePrivilege());
    }

    public String getRoleId() {
        return id.getRoleId();
    }

    public String getRolePrivilege() {
        return id.getRolePrivilege();
    }

    @Override
    public int hashCode() {
        return getRoleId().hashCode() + getRolePrivilege().hashCode();
    }

    public void setRoleId(String i) {
        id.setRoleId(i);
    }

    public void setRolePrivilege(String p) {
        id.setRolePrivilege(p);
    }
}
