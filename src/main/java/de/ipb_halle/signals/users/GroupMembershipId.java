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
 * SNB group membership id
 */
@Embeddable
public class GroupMembershipId implements Serializable {
    private final static long serialVersionUID = 1L;

    public final static String USER_ID = "user_id";

    private Integer group_id;
    private Integer user_id;


    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        GroupMembershipId other = (GroupMembershipId) o;
        return (getGroupId() == other.getGroupId())
            && (getUserId() == other.getUserId());
    }

    public Integer getGroupId() {
        return group_id;
    }

    public Integer getUserId() {
        return user_id;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (user_id != null) {
            hc = user_id.hashCode();
        }
        if (group_id != null) {
            hc += group_id.hashCode();
        }
        return hc;
    }

    public void setGroupId(Integer g) {
        group_id = g;
    }

    public void setUserId(Integer u) {
        user_id = u;
    }
}
