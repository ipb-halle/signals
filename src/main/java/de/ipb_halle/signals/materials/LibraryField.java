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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;


/**
 * Container type field definition
 */

@Entity
@Table(name = "library_fields")
public class LibraryField {

    @EmbeddedId
    private LibraryFieldId id;

    public LibraryField() {
        id = new LibraryFieldId();
    }

    public LibraryField(String lib, String fd) {
    id = new LibraryFieldId(lib, fd);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        LibraryField other = (LibraryField) o;
        return Objects.equals(id, other.id);
    }

    public String getLibraryId() {
        return id.getLibrary_id();
    }

    public String getFieldId() {
        return id.getField_id();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public LibraryField setLibraryId(String lib) {
        id.setLibrary_id(lib);
        return this;
    }

    public LibraryField setFieldId(String f) {
        id.setField_id( f);
        return this;
    }
}
