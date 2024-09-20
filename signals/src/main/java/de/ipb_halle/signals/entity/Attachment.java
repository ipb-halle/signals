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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 
 * Attachment 
 */

@Entity
@Table(name="attachments")
public class Attachment {


    public final static String ATTR_ENTITY_ID = "entityId";
    public final static String ATTR_ATTACHMENT_ID = "attachmentId";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_ENTITY_TYPE = "entityType";
    public final static String ATTR_FILE_NAME = "fileName";
    public final static String ATTR_TEMPLATE = "isTemplate";
    public final static String ATTR_UPDATED_AT = "updatedAt";
    public final static String ATTR_VERSION_ID = "versionId";

    @Id
    private String id;

    @Column(name="created_at")
    private Date createdAt;

    @Column(name="entity_id")
    private String entityId;

    @Column(name="entity_type")
    private String entityType;

    @Column(name="file_name")
    private String fileName;

    @Column
    private Boolean template;

    @Column(name="updated_at")
    private Date updatedAt;

    @Column(name="version_id")
    private String versionId;

    public String dump() {
        return String.format("Attachment(%s): %s\n", id, fileName);
    }

    public String getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getVersionId() {
        return versionId;
    }

    public Boolean isTemplate() {
        return template;
    }

    public Attachment setId(String i) {
        id = i;
        return this;
    }

    public Attachment setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public Attachment setEntityId(String i) {
        entityId = i;
        return this;
    }

    public Attachment setEntityType(String t) {
        entityType = t;
        return this;
    }

    public Attachment setFileName(String f) {
        fileName = f;
        return this;
    }

    public Attachment setTemplate(Boolean t) {
        template = t;
        return this;
    }

    public Attachment setUpdatedAt(Date d) {
        updatedAt = d;
        return this;
    }

    public Attachment setVersionId(String i) {
        versionId = i;
        return this;
    }
}
