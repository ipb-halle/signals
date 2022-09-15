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
package de.ipb_halle.signals.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/** 
 * Quality (measure) - field assignments
 */

@Entity
@Table(name="field_values")
public class FieldValue implements Serializable {

    private final static long serialVersionUID = 1L;

    public final static String ATTR_USER_VALUE = "content.user";
    public final static String ATTR_IS_RAW_VALUE = "content.isRawValue";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="field_definition_id")
    private String fieldDefinitionId;

    @Column(name="raw_value")
    private Boolean rawValue;

    @Column
    private String value;

    public String getFieldDefinitionId() {
        return fieldDefinitionId;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public Boolean isRawValue() {
        return rawValue;
    }

    public FieldValue setFieldDefinitionId(String i) {
        fieldDefinitionId = i;
        return this;
    }

    public FieldValue setId(Long i) {
        id = i;
        return this;
    }

    public FieldValue setRawValue(Boolean r) {
        rawValue = r;
        return this;
    }

    public FieldValue setValue(String v) {
        value = v;
        return this;
    }
}
