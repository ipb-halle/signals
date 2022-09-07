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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/** 
 * Field definition entity
 */

@Entity
@Table(name="field_definitions")
public class FieldDefinition {


    public final static String ATTR_ATTRIBUTE_LIST_EID = "attributeListEid";
    public final static String ATTR_ATTRIBUTE = "attribute";
    public final static String ATTR_CALCULATED = "calculated";
    public final static String ATTR_DATA_TYPE = "dataType";
    public final static String ATTR_DEFAULT_UNIT = "defaultUnit";
    public final static String ATTR_DEFINED_BY = "definedBy";
    public final static String ATTR_DEFINITION = "definition";
    public final static String ATTR_FIELD_TYPE = "type";
    public final static String ATTR_HIDDEN = "hidden";
    public final static String ATTR_KEY = "key";
    public final static String ATTR_MEASURES = "measures";
    public final static String ATTR_MULTISELECT = "multiSelect";
    public final static String ATTR_OPTIONS = "options";
    public final static String ATTR_READ_ONLY = "readOnly";
    public final static String ATTR_REQUIRED = "isRequired";
    public final static String ATTR_MANDATORY = "mandatory";
    public final static String ATTR_TITLE = "title";
    public final static String ATTR_USER_DEFINED = "isUserDefined";

    @Id
    private String id;

    /**
     * referred by ATTR_ATTRIBUTE_LIST_EID and ATTR_ATTRIBUTE
     */
    @Column(name="attribute_list_eid")
    private String attributeListEid;

    @Column
    private Boolean calculated;

    @Column(name="default_unit")
    private String defaultUnit;

    @Column(name="defined_by")
    private String definedBy;

    /**
     * referred by ATTR_FIELD_TYPE and ATTR_DATA_TYPE
     */
    @Column(name="field_type")
    private FieldType fieldType;

    @Column
    private Boolean hidden;

    @Column
    private String key;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @JoinColumn(name = "field_id")
    private Set<FieldMeasure> measures;

    @Column(name="multiselect")
    private Boolean multiSelect;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @JoinColumn(name = "field_id")
    private Set<FieldOption> options;
    
    @Column(name="read_only")
    private Boolean readOnly;
    /**
     * referred by ATTR_REQUIRED and ATTR_MANDATORY
     */
    @Column
    private Boolean required;

    @Column
    private String title; 

    @Column(name="user_defined")
    private Boolean userDefined;


    /**
     * default constructor
     */
    public FieldDefinition() {
        measures = new HashSet<> ();
        options = new HashSet<> ();
    }

    public FieldDefinition addMeasure(Quality q) {
        measures.add(new FieldMeasure(id, q));
        return this;
    }

    public FieldDefinition addOption(String o) {
        options.add(new FieldOption(id, o));
        return this;
    }

    public void dump() {
        System.out.printf("FieldDefinition(%s): %s\n", id, title);
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

    public FieldType getFieldType() {
        return fieldType;
    }

    public String getKey() {
        return key;
    }

    public Set<FieldMeasure> getMeasures() {
        return measures;
    }

    public Set<FieldOption> getOptions() {
        return options;
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

    public void removeMeasure(FieldMeasure m) {
        measures.remove(m);
    }

    public void removeOption(FieldOption o) {
        options.remove(o);
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

    public FieldDefinition setFieldType(FieldType t) {
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

    public FieldDefinition setMeasures(Set<FieldMeasure> m) {
        measures = m;
        return this;
    }

    public FieldDefinition setMultiSelect(Boolean b) {
        multiSelect = b;
        return this;
    }

    public FieldDefinition setOptions(Set<FieldOption> o) {
        options = o;
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
