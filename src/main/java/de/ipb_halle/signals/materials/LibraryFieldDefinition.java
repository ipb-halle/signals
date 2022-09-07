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
package de.ipb_halle.signals.materials;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


/** 
 * Container type field definition 
 */

@Entity
@IdClass(LibraryFieldDefinitionId.class)
@Table(name="library_fields")
public class LibraryFieldDefinition {

    @Id
    private String library_id; 

    @Id
    private String field_definition_id;

    @Id
    private String type;

    public LibraryFieldDefinition() {
    }

    public LibraryFieldDefinition(String lib, String fd, String t) {
        setLibraryId(lib);
        setFieldDefinitionId(fd);
        setType(t);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        LibraryFieldDefinition other = (LibraryFieldDefinition) o;
        return (getLibraryId() == other.getLibraryId())
            && (getFieldDefinitionId() == other.getFieldDefinitionId())
            && (getType() == other.getType());
    }

    public String getLibraryId() {
        return library_id;
    }

    public String getFieldDefinitionId() {
        return field_definition_id;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return getLibraryId().hashCode() + getFieldDefinitionId().hashCode() + getType().hashCode();
    }

    public LibraryFieldDefinition setLibraryId(String lib) {
        library_id = lib;
        return this;
    }

    public LibraryFieldDefinition setFieldDefinitionId(String fd) {
        field_definition_id = fd;
        return this;
    }

    public LibraryFieldDefinition setType(String t) {
        type = t;
        return this;
    }
}
