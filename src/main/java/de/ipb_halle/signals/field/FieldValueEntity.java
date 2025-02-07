/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.persistence.*;


/**
 * Field values (field definition, entity id, value)
 */

@Entity
@Table(name="field_values")
public class FieldValueEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    public final static String ATTR_USER_VALUE = "content.user";
    public final static String ATTR_IS_RAW_VALUE = "content.isRawValue";

    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "entity_id")),
            @AttributeOverride(name = "value", column = @Column(name = "field_id"))
    })
    @EmbeddedId
    private EmbeddedKeyValue id;

    @Column
    private String value;

    /**
     * default constructor
     */
    public FieldValueEntity() {
        id = new EmbeddedKeyValue();
    }

    public String getEntityId() {
        return id.getId();
    }

    public String getFieldDefinitionId() {
        return id.getValue();
    }

    public String getValue() {
        return value;
    }

    public FieldValueEntity setEntityId(String e) {
        id.setId(e);
        return this;
    }

    public FieldValueEntity setFieldDefinitionId(String i) {
        id.setValue(i);
        return this;
    }

    public FieldValueEntity setValue(String v) {
        value = v;
        return this;
    }
}
