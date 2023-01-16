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
 * SNB user roles
 */
@Embeddable
public class UserRoleId implements Serializable {
    private final static long serialVersionUID = 1L;

    public final static String USER_ID = "user_id";

    private String role_id;

    private String user_id;


    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        UserRoleId other = (UserRoleId) o;
        return (getRoleId().equals(other.getRoleId()))
            && (getUserId().equals(other.getUserId()));
    }

    public String getRoleId() {
        return role_id;
    }

    public String getUserId() {
        return user_id;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (role_id != null) {
            hc = role_id.hashCode();
        }
        if (user_id != null) {
            hc += user_id.hashCode();
        }
        return hc;
    }

    public void setRoleId(String id) {
        role_id = id;
    }

    public void setUserId(String u) {
        user_id = u;
    }
}
