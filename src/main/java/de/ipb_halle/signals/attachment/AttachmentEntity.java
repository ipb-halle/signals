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
package de.ipb_halle.signals.attachment;

import java.sql.Struct;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Attachment
 */

@Entity
@Table(name = "attachments")
public class AttachmentEntity {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "entity_type")
    private Integer entityType;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "edited_at")
    private Date editedAt;

    @Column
    private String digest;

    @Column(name = "ancestor_id")
    private String ancestorId;

    public String dump() {
        return String.format("Attachment(%s): %s\n", id, name);
    }

    public String getId() {
        return id;
    }

    public AttachmentEntity setId(String i) {
        id = i;
        return this;
    }

    public String getName() {
        return name;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public String getDigest() {
        return digest;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public AttachmentEntity setName(String name) {
        this.name = name;
        return this;
    }

    public AttachmentEntity setEntityType(Integer entityType) {
        this.entityType = entityType;
        return this;
    }

    public AttachmentEntity setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public AttachmentEntity setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
        return this;
    }

    public AttachmentEntity setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public AttachmentEntity setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    @Override
    public String toString() {
        return "AttachmentEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", entityType=" + entityType +
                ", createdAt=" + createdAt +
                ", editedAt=" + editedAt +
                ", digest='" + digest + '\'' +
                ", ancestorId='" + ancestorId + '\'' +
                '}';
    }
}
