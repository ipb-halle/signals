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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Field definition entity
 */

@Entity
@Table(name = "field_definitions")
public class FieldDefinition {

    @Id
    private String id;

    @Column(name = "attribute_list_eid")
    private String attributeListEid;

    @Column(name = "designation")
    private Integer fieldDesignation;

    @Column
    private boolean calculated;

    @Column(name = "default_unit")
    private String defaultUnit;

    /**
     *  SYSTEM_DEFAULT or USER_ADDED
     */
    @Column(name = "defined_by")
    private String definedBy;

    /**
     * The id of the entity, which defined this field.
     */
    @Column(name = "defining_entity_id")
    private String definingEntityId;

    @Column(name = "field_type")
    private Integer fieldType;

    @Column
    private boolean hidden;

    @Column
    private String key;

    @Column(name = "multiselect")
    private boolean multiSelect;

    @Column(name = "read_only")
    private boolean readOnly;

    @Column
    private boolean required;

    @Column
    private String title;

    @Column(name = "user_defined")
    private boolean userDefined;


    public String dump() {
        return String.format("FieldDefinition(%s): %s\n", id, title);
    }

    public String getId() {
        return id;
    }

    public String getAttributeListEid() {
        return attributeListEid;
    }

    public boolean getCalculated() {
        return calculated;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public String getDefinedBy() {
        return definedBy;
    }

    public String getDefiningEntityId() {
        return definingEntityId;
    }

    public Integer getFieldType() {
        return fieldType;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    public Integer getFieldDesignation() {
        return fieldDesignation;
    }

    public void setFieldDesignation(Integer fieldDesignation) {
        this.fieldDesignation = fieldDesignation;
    }

    public FieldDefinition setId(String i) {
        id = i;
        return this;
    }

    public FieldDefinition setAttributeListEid(String a) {
        attributeListEid = a;
        return this;
    }

    public FieldDefinition setCalculated(boolean c) {
        calculated = c;
        return this;
    }

    public FieldDefinition setDefaultUnit(String u) {
        defaultUnit = u;
        return this;
    }

    public FieldDefinition setDefinedBy(String d) {
        definedBy = d;
        return this;
    }

    public void setDefiningEntityId(String definingEntityId) {
        this.definingEntityId = definingEntityId;
    }

    public FieldDefinition setFieldType(Integer t) {
        fieldType = t;
        return this;
    }

    public FieldDefinition setHidden(boolean h) {
        hidden = h;
        return this;
    }

    public FieldDefinition setKey(String k) {
        key = k;
        return this;
    }

    public FieldDefinition setMultiSelect(boolean b) {
        multiSelect = b;
        return this;
    }

    public FieldDefinition setReadOnly(boolean r) {
        readOnly = r;
        return this;
    }

    public FieldDefinition setRequired(boolean r) {
        required = r;
        return this;
    }

    public FieldDefinition setTitle(String t) {
        title = t;
        return this;
    }

    public FieldDefinition setUserDefined(boolean u) {
        userDefined = u;
        return this;
    }
}
