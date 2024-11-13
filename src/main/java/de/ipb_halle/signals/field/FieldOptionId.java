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
package de.ipb_halle.signals.field;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;


/** 
 * SNB field options (This class exists solely for JPA purposes)
 */
@Embeddable
public class FieldOptionId implements Serializable {
    private final static long serialVersionUID = 1L;

    private String field_id;

    private String option;

    /**
     * default constructor
     */
    public FieldOptionId() {
    }

    public FieldOptionId(String f, String o) {
        field_id = f;
        option = o;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        } 
        FieldOptionId other = (FieldOptionId) o;
        return Objects.equals(field_id, other.field_id)
            && Objects.equals(option, other.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_id) + Objects.hash(option);
    }

    public String getField_id() {
        return field_id;
    }

    public void setField_id(String field_id) {
        this.field_id = field_id;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
