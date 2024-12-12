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

package de.ipb_halle.signals.attachment;


import java.util.Date;
import java.util.Objects;

/**
 * Attachment DTO
 */
public class Attachment implements IAttachment {

    public final static String ATTR_CREATED_AT = "createdAt";

    private String entityId;
    private String name;
    private AttachmentType type;
    private Date createdAt;
    private Date editedAt;
    private String digest;
    private String ancestorId;

    public Attachment() {
    }

    public Attachment(String entityId, String name, Date createdAt, Date editedAt, AttachmentType type, String digest, String ancestorId) {
        this.entityId = entityId;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
        this.digest = digest;
        this.ancestorId = ancestorId;
    }

    public AttachmentEntity createEntity() {
        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setId(entityId);
        attachmentEntity.setName(name);
        attachmentEntity.setEntityType(type.getId());
        attachmentEntity.setCreatedAt(createdAt);
        attachmentEntity.setEditedAt(editedAt);
        attachmentEntity.setDigest(digest);
        attachmentEntity.setAncestorId(ancestorId);
        return attachmentEntity;
    }

    @Override
    public String getId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public AttachmentType getType() {
        return type;
    }

    public String getDigest() {
        return digest;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    @Override
    public IAttachment setId(String id) {
        this.entityId = id;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }


    public void setDigest(String digest) {
        this.digest = digest;
    }


    public void setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Attachment that = (Attachment) object;
        return Objects.equals(entityId, that.entityId) && Objects.equals(name, that.name) && Objects.equals(createdAt, that.createdAt) && Objects.equals(editedAt, that.editedAt) && Objects.equals(type, that.type) && Objects.equals(digest, that.digest) && Objects.equals(ancestorId, that.ancestorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, name, createdAt, editedAt, type, digest, ancestorId);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "entityId='" + entityId + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", editedAt=" + editedAt +
                ", type=" + type +
                ", digest='" + digest + '\'' +
                ", ancestorId='" + ancestorId + '\'' +
                '}';
    }
}
