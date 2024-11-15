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

import de.ipb_halle.signals.util.EmbeddedKeyValue;
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
    private EmbeddedKeyValue id;


    public ContainerTypeField() {
        id = new EmbeddedKeyValue();
    }

    public ContainerTypeField(String ct, String f) {
        id = new EmbeddedKeyValue(ct, f);
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
        return id.getId();
    }

    public String getFieldId() {
        return id.getValue();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public ContainerTypeField setContainerTypeId(String ct) {
        id.setId(ct);
        return this;
    }

    public ContainerTypeField setFieldId(String f) {
        id.setValue(f);
        return this;
    }
}
