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
import javax.persistence.Embeddable;

/** 
 * SNB role privileges
 */
@Embeddable
public class RolePrivId implements Serializable {
    private final static long serialVersionUID = 1L;

    private Integer role_id;

    private RolePrivilege privilege;


    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        RolePrivId other = (RolePrivId) o;
        return (getRoleId() == other.getRoleId())
            && (getRolePrivilege() == other.getRolePrivilege());
    }

    public Integer getRoleId() { 
        return role_id; 
    }

    public RolePrivilege getRolePrivilege() {
        return privilege;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (role_id != null) {
            hc = role_id.hashCode();
        }
        if (privilege != null) {
            hc += privilege.hashCode();
        }
        return hc;
    }

    public void setRoleId(Integer id) {
        role_id = id;
    }

    public void setRolePrivilege(RolePrivilege p) {
        privilege = p;
    }
}
