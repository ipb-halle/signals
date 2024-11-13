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
package de.ipb_halle.signals.inventory;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Field definitions for container types. This class solely exists
 * for JPA purposes (compound Id).
 */
@Embeddable
public class ContainerTypeFieldId implements Serializable {
    private final static long serialVersionUID = 1L;

    private String container_type_id;

    private String field_id;

    public ContainerTypeFieldId(String ct, String f) {
        container_type_id = ct;
        field_id = f;
    }

    public ContainerTypeFieldId() {

    }

    public String getContainer_type_id() {
        return container_type_id;
    }

    public void setContainer_type_id(String container_type_id) {
        this.container_type_id = container_type_id;
    }

    public String getField_id() {
        return field_id;
    }

    public void setField_id(String field_id) {
        this.field_id = field_id;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        ContainerTypeFieldId other = (ContainerTypeFieldId) o;
        return Objects.equals(container_type_id, other.container_type_id)
                && Objects.equals(field_id, other.field_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container_type_id) + Objects.hash(field_id);
    }
}
