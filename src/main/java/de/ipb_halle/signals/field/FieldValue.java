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

import de.ipb_halle.signals.attachment.Attachment;

import java.util.Objects;

public class FieldValue {

    public final static String ATTR_CONTENT = "content";

    // criteria constants; FieldValueEntity uses EmbeddedKeyValue
    public static final String ENTITY_ID ="EntityId" ;
    public static final String FIELD_ID ="FieldId" ;
    public static final String ID = "id";

    public enum LinkType {
        ID,
        FIELD_ID,
        FIELD_TITLE,
        UNSPECIFIED
    }

    private String entityId;
    private String fieldId;
    private String fieldTitle;
    private String value;
    private LinkType linkType;
    private transient Attachment attachment;

    /**
     * Field definition.
     * This field stores either an ad hoc field definition, which will be created
     * during parsing of materials or a field definition from the database, if
     * one exists.
     */
    private transient Field field;

    public FieldValue() {
        linkType = LinkType.UNSPECIFIED;
    }

    public FieldValue(FieldValueEntity fieldValueEntity) {
        linkType = LinkType.ID;
        entityId = fieldValueEntity.getEntityId();
        fieldId = fieldValueEntity.getFieldDefinitionId();
        value = fieldValueEntity.getValue();
    }

    public FieldValueEntity createEntity() {
        FieldValueEntity entity = new FieldValueEntity();
        entity.setEntityId(entityId);
        entity.setFieldDefinitionId(fieldId);
        entity.setValue(value);
        return entity;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldTitle() {
        return fieldTitle;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public String getValue() {
        return value;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public FieldValue setEntityId(String id) {
        this.entityId = id;
        return this;
    }

    public FieldValue setFieldId(String fieldId) {
        this.fieldId = fieldId;
        return this;
    }

    public FieldValue setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
        return this;
    }

    public FieldValue setLinkType(LinkType linkType) {
        this.linkType = linkType;
        return this;
    }

    public FieldValue setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        FieldValue that = (FieldValue) object;
        return Objects.equals(entityId, that.entityId) && Objects.equals(fieldId, that.fieldId) && Objects.equals(fieldTitle, that.fieldTitle) && Objects.equals(value, that.value) && linkType == that.linkType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, fieldId, fieldTitle, value, linkType);
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "FieldValue{" +
                "entityId='" + entityId + '\'' +
                ", fieldId='" + fieldId + '\'' +
                ", fieldTitle='" + fieldTitle + '\'' +
                ", value='" + value + '\'' +
                ", linkType=" + linkType +
                ", attachment=" + attachment +
                ", field=" + field +
                '}';
    }
}
