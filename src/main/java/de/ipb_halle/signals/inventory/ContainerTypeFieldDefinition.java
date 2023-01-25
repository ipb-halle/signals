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
import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;


/** 
 * Container type field definition 
 */

@Entity
@IdClass(ContainerTypeFieldDefinitionId.class)
@Table(name="container_type_fields")
public class ContainerTypeFieldDefinition {

    @Id
    private String container_type_id; 

    @Id
    private String field_definition_id;


    public ContainerTypeFieldDefinition() {
    }

    public ContainerTypeFieldDefinition(String ct, String fd) {
        setContainerTypeId(ct);
        setFieldDefinitionId(fd);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        ContainerTypeFieldDefinition other = (ContainerTypeFieldDefinition) o;
        return Objects.equals(container_type_id, other.getContainerTypeId())
            && Objects.equals(field_definition_id, other.getFieldDefinitionId());
    }

    public String getContainerTypeId() {
        return container_type_id;
    }

    public String getFieldDefinitionId() {
        return field_definition_id;
    }

    @Override
    public int hashCode() {
        return getContainerTypeId().hashCode() + getFieldDefinitionId().hashCode();
    }

    public ContainerTypeFieldDefinition setContainerTypeId(String ct) {
        container_type_id = ct;
        return this;
    }

    public ContainerTypeFieldDefinition setFieldDefinitionId(String fd) {
        field_definition_id = fd;
        return this;
    }
}
