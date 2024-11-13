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
    private Boolean calculated;

    @Column(name = "default_unit")
    private String defaultUnit;

    @Column(name = "defined_by")
    private String definedBy;

    @Column(name = "field_type")
    private Integer fieldType;

    @Column
    private Boolean hidden;

    @Column
    private String key;

    @Column(name = "multiselect")
    private Boolean multiSelect;

    @Column(name = "read_only")
    private Boolean readOnly;

    @Column
    private Boolean required;

    @Column
    private String title;

    @Column(name = "user_defined")
    private Boolean userDefined;


    public String dump() {
        return String.format("FieldDefinition(%s): %s\n", id, title);
    }

    public String getId() {
        return id;
    }

    public String getAttributeListEid() {
        return attributeListEid;
    }

    public Boolean getCalculated() {
        return calculated;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public String getDefinedBy() {
        return definedBy;
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

    public Boolean isHidden() {
        return hidden;
    }

    public Boolean isMultiSelect() {
        return multiSelect;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public Boolean isRequired() {
        return required;
    }

    public Boolean isUserDefined() {
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

    public FieldDefinition setCalculated(Boolean c) {
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

    public FieldDefinition setFieldType(Integer t) {
        fieldType = t;
        return this;
    }

    public FieldDefinition setHidden(Boolean h) {
        hidden = h;
        return this;
    }

    public FieldDefinition setKey(String k) {
        key = k;
        return this;
    }

    public FieldDefinition setMultiSelect(Boolean b) {
        multiSelect = b;
        return this;
    }

    public FieldDefinition setReadOnly(Boolean r) {
        readOnly = r;
        return this;
    }

    public FieldDefinition setRequired(Boolean r) {
        required = r;
        return this;
    }

    public FieldDefinition setTitle(String t) {
        title = t;
        return this;
    }

    public FieldDefinition setUserDefined(Boolean u) {
        userDefined = u;
        return this;
    }
}
