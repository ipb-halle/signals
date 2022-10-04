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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** 
 * SNB role privileges
 */

@Entity
@Table(name="user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    public UserRole() {
        id = new UserRoleId();
    }

    public UserRole(IRole r, IUser u) {
        id = new UserRoleId();
        id.setRoleId(r.getId());
        id.setUserId(u.getId());
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        UserRole other = (UserRole) o;
        return (id.getRoleId() == other.getRoleId())
            && (id.getUserId() == other.getUserId());
    }

    public Integer getRoleId() {
        return id.getRoleId();
    }

    public Integer getUserId() {
        return id.getUserId();
    }

    @Override
    public int hashCode() {
        return getRoleId() + getUserId();
    }

    public void setRoleId(Integer i) {
        id.setRoleId(i);
    }

    public void setUserId(Integer u) {
        id.setUserId(u);
    }
}
