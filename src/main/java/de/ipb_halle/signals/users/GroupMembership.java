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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/** 
 * SNB group membership
 */

@Entity
@Table(name="group_memberships")
public class GroupMembership {

    public final static String GROUP_MEMBERSHIP_ID = "id";

    @EmbeddedId
    private GroupMembershipId id;

    public GroupMembership() {
        id = new GroupMembershipId();
    }

    public GroupMembership(IGroup g, IUser u) {
        id = new GroupMembershipId();
        id.setGroupId(g.getId());
        id.setUserId(u.getId());
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        GroupMembership other = (GroupMembership) o;
        return Objects.equals(getGroupId(), other.getGroupId())
            && Objects.equals(getUserId(), other.getUserId());
    }

    public String getGroupId() {
        return id.getGroupId();
    }

    public String getUserId() {
        return id.getUserId();
    }

    @Override
    public int hashCode() {
        return getGroupId().hashCode() + getUserId().hashCode();
    }

    public void setGroupId(String g) {
        id.setGroupId(g);
    }

    public void setUserId(String u) {
        id.setUserId(u);
    }
}
