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

package de.ipb_halle.signals.experiments;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Experiment {
    public static final Logger logger = LogManager.getLogger(Experiment.class);
    public static final String ATTR_TYPE_EXPERIMENT = "experiment";

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
    private String templateId;

    private Set<SignalsEntity> ancestors;
    private Set<SignalsEntity> children;
    private Set<ExperimentProperty> properties;
    private Set<ExperimentPropertyValue> propertyValues;

    public Experiment() {
        ancestors = new HashSet<>();
        children = new HashSet<>();
        properties = new HashSet<>();
        propertyValues = new HashSet<>();
    }

    public Experiment(ExperimentEntity experimentEntity, DynEnumManager dynEnumManager) {
        logger.info("creating an experiment from experiment entity {}\n", experimentEntity.getId());
        this.id = experimentEntity.getId();
        this.name = experimentEntity.getName();
        this.description = experimentEntity.getDescription();
        this.type = (EntityType) dynEnumManager.valueOf(experimentEntity.getType());
        this.createdAt = experimentEntity.getCreatedAt();
        this.editedAt = experimentEntity.getEditedAt();
        this.createdBy = new UserReference(experimentEntity.getCreatedBy());
        this.editedBy = new UserReference(experimentEntity.getEditedBy());
        this.owner = new UserReference(experimentEntity.getOwner());
        this.digest = experimentEntity.getDigest();
        this.ancestorId = experimentEntity.getAncestorId();
        this.templateId = experimentEntity.getTemplateId();

        this.ancestors = new HashSet<>();
        this.children = new HashSet<>();

        this.properties = new HashSet<>();
        this.propertyValues = new HashSet<>();
    }


    public ExperimentEntity createEntity() {
        return new ExperimentEntity()
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
                .setTemplateId(templateId);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public IUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(IUser createdBy) {
        this.createdBy = createdBy;
    }

    public IUser getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(IUser editedBy) {
        this.editedBy = editedBy;
    }

    public IUser getOwner() {
        return owner;
    }

    public void setOwner(IUser owner) {
        this.owner = owner;
    }

    public Long getDigest() {
        return digest;
    }

    public void setDigest(Long digest) {
        this.digest = digest;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public void setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Set<SignalsEntity> getAncestors() {
        return ancestors;
    }

    public void setAncestors(Set<SignalsEntity> ancestors) {
        this.ancestors = ancestors;
    }

    public Set<SignalsEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<SignalsEntity> children) {
        this.children.addAll(children);
    }

    public Set<ExperimentProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<ExperimentProperty> properties) {
        this.properties.addAll(properties);
    }

    public Set<ExperimentPropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(Set<ExperimentPropertyValue> propertyValues) {
        this.propertyValues.addAll(propertyValues);
    }

    public void addAncestor(SignalsEntity se) {
        this.ancestors.add(se);
    }

    public void addChild(SignalsEntity se) {
        this.children.add(se);
    }

    public void addProperty(ExperimentProperty experimentProperty) {
        properties.add(experimentProperty);
    }

    public void addPropertyValue(ExperimentPropertyValue experimentPropertyValue) {
        propertyValues.add(experimentPropertyValue);
    }
}
