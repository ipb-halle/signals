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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;


/**
 * Container type field definition
 */

@Entity
@Table(name = "container_type_fields")
public class ContainerTypeField {

    @EmbeddedId
    private ContainerTypeFieldId id;


    public ContainerTypeField() {
        id = new ContainerTypeFieldId();
    }

    public ContainerTypeField(String ct, String f) {
        id = new ContainerTypeFieldId(ct, f);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        ContainerTypeField other = (ContainerTypeField) o;
        return Objects.equals(id, other.id);
    }

    public String getContainerTypeId() {
        return id.getContainer_type_id();
    }

    public String getFieldId() {
        return id.getField_id();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public ContainerTypeField setContainerTypeId(String ct) {
        id.setContainer_type_id(ct);
        return this;
    }

    public ContainerTypeField setFieldId(String f) {
        id.setField_id(f);
        return this;
    }
}
