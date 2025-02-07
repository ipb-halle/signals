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

import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;


/**
 * Quality (measure) - field assignments
 */

@Entity
@Table(name="field_options")
public class FieldOption implements Serializable {

    private final static long serialVersionUID = 1L;

    @EmbeddedId
    private EmbeddedKeyValue id;

    /**
     * default constructor
     */
    public FieldOption() {
        new EmbeddedKeyValue();
    }

    public FieldOption(String f, String o) {
        id = new EmbeddedKeyValue(f, o);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        FieldOption other = (FieldOption) o;
        return id.equals(other.id);
    }

    public String getFieldId() {
        return id.getId();
    }

    public String getOption() {
        return id.getValue();
    }

    @Override
    public int hashCode() {
        return getFieldId().hashCode() + getOption().hashCode();
    }

    public void setFieldId(String i) {
        id.setId(i);
    }

    public void setOption(String o) {
        id.setValue(o);
    }
}
