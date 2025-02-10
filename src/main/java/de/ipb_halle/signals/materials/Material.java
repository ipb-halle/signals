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
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.IObjectMetaData;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Material DTO
 */
public class Material implements IMaterial, IObjectMetaData {

    public final static String ATTR_ASSET_TYPE_ID = "assetTypeId";
    public final static String ATTR_ASSET_ID = "assetId";
    public final static String ATTR_BATCH = "batch";
    public final static String ATTR_SYNONYMS = "synonyms";
    public static final String MATERIAL_ASSET_PREFIX = "asset:" ;
    private Logger logger = LoggerFactory.getLogger(MaterialRestService.class);

    public final static String ENTITY_TYPE_ASSET = "asset";
    public final static String ENTITY_TYPE_BATCH = "batch";

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

    private Set<Synonym> synonyms;
    private Set<FieldValue> fieldValues;
    private Set<Attachment> attachments;
    private EntityType entityType;

    /**
     * Parent material (i.e. asset) of a material batch. Is null
     * for assets.
     */
    private IMaterial material;

    public Material() {
        synonyms = new HashSet<>();
        fieldValues = new HashSet<>();
        attachments = new HashSet<>();
    }

    public Material(MaterialEntity materialEntity, EntityType entityType) {
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
        if (!(EntityType.valueOf(Material.ENTITY_TYPE_ASSET).equals(entityType)
                || EntityType.valueOf(Material.ENTITY_TYPE_BATCH).equals(entityType))) {
            throw new IllegalArgumentException("Attempt to create Material of inappropriate EntityType");
        }
        this.entityType = entityType;
        if (materialEntity.getMaterialId() != null) {
            this.material = new MaterialReference().setId(materialEntity.getMaterialId());
        }
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
        entity.setEntityType(entityType.getId());
        if (material != null) {
            entity.setMaterialId(material.getId());
        }
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

    public Material addFieldValue(FieldValue value) {
        fieldValues.add(value);
        return this;
    }

    public void addSynonym(Synonym synonym) {
        synonyms.add(synonym);
    }

    public String getId() {
        return id;
    }

    /**
     * @return splits the id into prefix, id (and suffix) and return the id part
     */
    public String getStrippedId() {
        String[] parts = id.split(":");
        return parts.length > 1 ? parts[1] : parts[0];
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

    @Override
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

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public IMaterial getMaterial() {
        return material;
    }

    public void setMaterial(IMaterial material) {
        this.material = material;
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
