/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class Sample {

    public static final String ATTR_STOIC_REF_EID = "stoicRef.eid";
    public static final String ATTR_STOIC_REF_ROWID = "stoicRef.rowId";
    public static final String ATTR_STOIC_REF = "stoicRef";
    public static final String ATTR_SAMPLE = "sample";

    private final Logger logger = LogManager.getLogger(Sample.class);

    private String id;
    private String name;
    private String description;
    private EntityType type;
    private Date createdAt;
    private Date editedAt;
    private IUser createdBy;
    private IUser editedBy;
    private IUser owner;
    private Long digest;
    private String ancestorId;
    private StoicRef stoicRef;
    private String parentContainerId;
    private String templateId;


    private Set<SignalsEntity> ancestors;
    private Set<SignalsEntity> children;
    private Set<SampleProperty> properties;
    private Set<SamplePropertyValue> propertyValues;


    public Sample() {
        ancestors = new HashSet<>();
        children = new HashSet<>();
        properties = new HashSet<>();
        propertyValues = new HashSet<>();
    }

    public Sample(SampleEntity sampleEntity, DynEnumManager dynEnumManager) {
        this.id = sampleEntity.getId();
        this.name = sampleEntity.getName();
        this.description = sampleEntity.getDescription();
        this.type = (EntityType) dynEnumManager.valueOf(sampleEntity.getType());
        this.createdAt = sampleEntity.getCreatedAt();
        this.editedAt = sampleEntity.getEditedAt();
        this.createdBy = new UserReference(sampleEntity.getCreatedBy());
        this.editedBy = new UserReference(sampleEntity.getEditedBy());
        this.owner = new UserReference(sampleEntity.getOwner());
        this.digest = sampleEntity.getDigest();
        this.ancestorId = sampleEntity.getAncestorId();
        this.parentContainerId = sampleEntity.getParentContainerId();
        this.stoicRef = new StoicRef().setEid(sampleEntity.getStoicRefId()).setRowId(sampleEntity.getStoicRefRowId());
        this.templateId = sampleEntity.getTemplateId();

        this.ancestors = new HashSet<>();
        this.children = new HashSet<>();

        this.properties = new HashSet<>();
        this.propertyValues = new HashSet<>();

    }


    public SampleEntity createEntity() {
        SampleEntity se = new SampleEntity()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setType(type.getId())
                .setCreatedAt(createdAt)
                .setEditedAt(editedAt)
                .setCreatedBy(createdBy.toString())
                .setEditedBy(editedBy.toString())
                .setOwner(owner.getId())
                .setDigest(digest)
                .setAncestorId(ancestorId)
                .setParentContainerId(parentContainerId)
                .setTemplateId(templateId);

        if (stoicRef != null) {
            se.setStoicRefId(this.stoicRef.getEid());
            se.setStoicRefRowId(this.stoicRef.getRowId());
            logger.info("Sample:->stoicRef_id={}, stoicRef_row_id={}\n",
                    this.stoicRef.getEid(), this.stoicRef.getRowId());
        }
        return se;
    }

    public void addAncestor(SignalsEntity ancestor) {
        this.ancestors.add(ancestor);
    }

    public void addChild(SignalsEntity child) {
        this.children.add(child);
    }

    //getter

    public Logger getLogger() {
        return logger;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityType getType() {
        return type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public IUser getCreatedBy() {
        return createdBy;
    }

    public IUser getEditedBy() {
        return editedBy;
    }

    public IUser getOwner() {
        return owner;
    }

    public Long getDigest() {
        return digest;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public StoicRef getStoicRef() {
        return stoicRef;
    }

    public String getParentContainerId() {
        return parentContainerId;
    }

    public Set<SamplePropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public Set<SignalsEntity> getAncestors() {
        return ancestors;
    }

    public Set<SignalsEntity> getChildren() {
        return children;
    }

    public Set<SampleProperty> getProperties() {
        return properties;
    }

    public String getTemplateId() {
        return templateId;
    }
    //setter

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public void setCreatedBy(IUser createdBy) {
        this.createdBy = createdBy;
    }

    public void setEditedBy(IUser editedBy) {
        this.editedBy = editedBy;
    }

    public void setOwner(IUser owner) {
        this.owner = owner;
    }

    public void setDigest(Long digest) {
        this.digest = digest;
    }

    public Sample setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public void setStoicRef(StoicRef stoicRef) {
        this.stoicRef = stoicRef;
    }

    public void setParentContainerId(String parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    public void addPropertyValues(Collection<FieldValue> fieldValues) {
        this.propertyValues.addAll(propertyValues);
    }

    public void setProperties(Set<SampleProperty> properties) {
        this.properties.addAll(properties);
    }

    public void setAncestors(Set<SignalsEntity> ancestors) {
        this.ancestors = ancestors;
    }

    public void setChildren(Set<SignalsEntity> children) {
        this.children = children;
    }

    public void addProperty(SampleProperty property) {
        properties.add(property);
    }

    public void setTemplateId(String templateId) {
        this.templateId=templateId;
    }

    public void addPropertyValue(SamplePropertyValue samplePropertyValue) {
        this.propertyValues.add(samplePropertyValue);
    }
}
