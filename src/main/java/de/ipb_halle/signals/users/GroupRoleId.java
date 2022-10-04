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
 * SNB group roles 
 */
@Embeddable
public class GroupRoleId implements Serializable {
    private final static long serialVersionUID = 1L;

    private Integer group_id;
    private Integer role_id;

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        GroupRoleId other = (GroupRoleId) o;
        return (getGroupId() == other.getGroupId())
            && (getRoleId() == other.getRoleId());
    }

    public Integer getGroupId() {
        return group_id;
    }

    public Integer getRoleId() { 
        return role_id; 
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (role_id != null) {
            hc = role_id;
        }
        if (group_id != null) {
            hc += group_id;
        }
        return hc;
    }

    public void setGroupId(Integer g) {
        group_id = g;
    }

    public void setRoleId(Integer id) {
        role_id = id;
    }
}
