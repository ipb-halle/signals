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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.entity.EntityRelationships;
import de.ipb_halle.signals.entity.ISignalsEntity;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Material DTO
 */

public class Material implements IMaterial, EntityRelationships {

    public final static String ATTR_ASSET_TYPE_ID = "assetTypeId";
    public final static String ATTR_SYNONYMS = "synonyms";
    private Logger logger = LoggerFactory.getLogger(MaterialRestService.class);

    public final static String ENTITY_TYPE_ASSET = "asset";

    private String id;
    private String name;
    private String description;
    private String libraryId;
    private Date createdAt;
    private IUser createdBy;
    private IUser owner;
    private Date editedAt;
    private IUser editedBy;
    private Long digest;
    private String libraryName;

    private Set<Synonym> synonyms;
    private Set<FieldValue> fieldValues;
    private Set<Attachment> attachments;

    public Material() {
        synonyms = new HashSet<>();
        fieldValues = new HashSet<>();
        attachments = new HashSet<>();
    }

    public Material(MaterialEntity materialEntity) {
        this.id = materialEntity.getId();
        this.name = materialEntity.getName();
        this.description = materialEntity.getDescription();
        this.libraryId = materialEntity.getLibraryId();
        this.createdAt = materialEntity.getCreatedAt();
        this.createdBy = new UserReference(materialEntity.getCreatedBy());
        this.owner = new UserReference(materialEntity.getOwner());
        this.editedAt = materialEntity.getEditedAt();
        this.editedBy = new UserReference(materialEntity.getEditedBy());
        this.digest = materialEntity.getDigest();
        /* complex types */
        this.synonyms = new HashSet<>();
        this.fieldValues = new HashSet<>();
        this.attachments = new HashSet<>();
    }

    public MaterialEntity createEntity() {
        MaterialEntity entity = new MaterialEntity();
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy.getId());
        entity.setDescription(description);
        entity.setDigest(digest);
        entity.setLibraryId(libraryId);
        entity.setEditedAt(editedAt);
        entity.setEditedBy(editedBy.getId());
        entity.setName(name);
        entity.setOwner(owner.getId());
        return entity;
    }

    public Material addAllSynonyms(Collection<Synonym> synonyms) {
        this.synonyms.addAll(synonyms);
        return this;
    }

    public Material addAllFieldValues(List<FieldValue> values) {
        this.fieldValues.addAll(values);
        return this;
    }

    public void addSynonym(Synonym synonym) {
        synonyms.add(synonym);
    }

    public String getId() {
        return id;
    }

    public IMaterial setId(String i) {
        id = i;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    @Override
    public void addAllAncestors(Collection<ISignalsEntity> ancestors) {
        ancestors.addAll(ancestors);
    }

    @Override
    public void addAllChildren(Collection<ISignalsEntity> children) {
        //children.addAll(children);
    }

    @Override
    public Set<ISignalsEntity> getAncestors() {
        return Set.of();
    }

    @Override
    public void setAncestors(Set<ISignalsEntity> ancestors) {
        // xxxxx ToDo ancestors
        // this.ancestors = ancestors;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public IUser getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(IUser createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public IUser getOwner() {
        return owner;
    }

    public void setOwner(IUser owner) {
        this.owner = owner;
    }

    @Override
    public Date getEditedAt() {
        return editedAt;
    }

    @Override
    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    @Override
    public IUser getEditedBy() {
        return editedBy;
    }

    @Override
    public void setEditedBy(IUser editedBy) {
        this.editedBy = editedBy;
    }

    public Long getDigest() {
        return digest;
    }

    public void setDigest(Long digest) {
        this.digest = digest;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public Set<Synonym> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<Synonym> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<FieldValue> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(Set<FieldValue> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Material material = (Material) object;
        return Objects.equals(id, material.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Material{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", libraryId='" + libraryId + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", owner='" + owner + '\'' +
                ", editedAt=" + editedAt +
                ", editedBy='" + editedBy + '\'' +
                ", digest=" + digest +
                '}';
    }
}
