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

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Field definitions for libraries. This class solely exists
 * for JPA purposes (compound Id).
 */
@Embeddable
public class LibraryFieldId implements Serializable {
    private final static long serialVersionUID = 1L;

    private String library_id;

    private String field_id;

    public LibraryFieldId(String lib, String fd) {
        library_id = lib;
        field_id = fd;
    }

    public LibraryFieldId() {

    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        LibraryFieldId other = (LibraryFieldId) o;
        return Objects.equals(library_id, other.library_id)
                && Objects.equals(field_id, other.field_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(library_id) + Objects.hash(field_id);
    }

    public String getLibrary_id() {
        return library_id;
    }

    public void setLibrary_id(String library_id) {
        this.library_id = library_id;
    }

    public String getField_id() {
        return field_id;
    }

    public void setField_id(String field_id) {
        this.field_id = field_id;
    }
}
