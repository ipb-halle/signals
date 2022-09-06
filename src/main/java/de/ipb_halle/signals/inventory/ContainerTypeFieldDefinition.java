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

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


/** 
 * Container type field definition 
 */

@Entity
@Table(name="container_type_fields")
public class ContainerTypeFieldDefinition {

    @EmbeddedId
    private ContainerTypeFieldDefinitionId id;


    public ContainerTypeFieldDefinition() {
        id = new ContainerTypeFieldDefinitionId();
    }

    public ContainerTypeFieldDefinition(String ct, String fd) {
        id = new ContainerTypeFieldDefinitionId();
        id.setContainerTypeId(ct);
        id.setFieldDefinitionId(fd);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        ContainerTypeFieldDefinition other = (ContainerTypeFieldDefinition) o;
        return (id.getContainerTypeId() == other.getContainerTypeId())
            && (id.getFieldDefinitionId() == other.getFieldDefinitionId());
    }

    public String getContainerTypeId() {
        return id.getContainerTypeId();
    }

    public String getFieldDefinitionId() {
        return id.getFieldDefinitionId();
    }

    @Override
    public int hashCode() {
        return getContainerTypeId().hashCode() + getFieldDefinitionId().hashCode();
    }

    public ContainerTypeFieldDefinition setContainerTypeId(String ct) {
        id.setContainerTypeId(ct);
        return this;
    }

    public ContainerTypeFieldDefinition setFieldDefinitionId(String fd) {
        id.setFieldDefinitionId(fd);
        return this;
    }
}
