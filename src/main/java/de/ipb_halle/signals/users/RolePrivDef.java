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
 * Known SNB role privileges
 */

@Entity
@Table(name="role_priv_defs")
public class RolePrivDef{


    @Id
    private String id;


    public RolePrivDef() {
    }

    public RolePrivDef(String p) {
        id = p;
    }

    public String getId() {
        return id;
    }

    public boolean equals(Object o) {
        if ((o != null) && (o instanceof RolePrivDef)) {
            RolePrivDef other = (RolePrivDef) o;
            return Objects.equals(this.id, other.getId());
        }
        return false;
    }

    public int hashCode() {
        return Objects.hashCode(id);
    }

    public RolePrivDef setId(String i) {
        id = i;
        return this;
    }
}
