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

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.*;


/** 
 * Quality (measure) - field assignments
 */

@Entity
@Table(name="field_options")
public class FieldOption implements Serializable {

    private final static long serialVersionUID = 1L;

    @EmbeddedId
    private FieldOptionId id;

    /**
     * default constructor
     */
    public FieldOption() {
        new FieldOptionId();
    }

    public FieldOption(String f, String o) {
        id = new FieldOptionId(f, o);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        FieldOption other = (FieldOption) o;
        return id.equals(other.getEmbeddedId());
    }

    public FieldOptionId getEmbeddedId() {
        return id;
    }

    public String getFieldId() {
        return id.getField_id();
    }

    public String getOption() {
        return id.getOption();
    }

    @Override
    public int hashCode() {
        return getFieldId().hashCode() + getOption().hashCode();
    }

    public void setFieldId(String i) {
        id.setField_id(i);
    }

    public void setOption(String o) {
        id.setOption(o);
    }
}
