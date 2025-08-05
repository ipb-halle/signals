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

package de.ipb_halle.signals.ado;

import de.ipb_halle.signals.ado.properties.AdoProperty;
import de.ipb_halle.signals.ado.properties.AdoPropertyValue;
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

/**
 * Admin defined Object
 */
public class Ado {
    public static final Logger logger = LogManager.getLogger(Ado.class);
    public final static String ENTITY_TYPE_ADO = "ado";

    private String id;
    private String eid;
    private String name;
    private String description;
    private EntityType type;
    private String ipbCode;
    private String molId;
    private Integer procId;
    private String sampleId;
    private Date createdAt;
    private IUser createdBy;
    private String state;
    private String ancestorId;
    private String templateId;


    private Set<SignalsEntity> ancestors;
    private Set<SignalsEntity> children;
    private Set<AdoProperty> properties;
    private Set<AdoPropertyValue> propertyValues;

    public Ado() {
        ancestors = new HashSet<>();
        children = new HashSet<>();
        properties = new HashSet<>();
        propertyValues = new HashSet<>();
    }

    public Ado(AdoEntity entity, DynEnumManager manager) {
        logger.info("Ado.class -> creating an ado from ado entity {}\n", entity.getId());
        this.id = entity.getId();
        this.eid = entity.getEid();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.type = (EntityType) manager.valueOf(entity.getType());
        this.ipbCode = entity.getIpbCode();
        this.molId = entity.getMolId();
        this.procId = entity.getProcId();
        this.sampleId = entity.getSampleId();
        this.createdAt = entity.getCreatedAt();
        this.createdBy = new UserReference(entity.getCreatedBy());
        this.state = entity.getState();
        this.ancestorId = entity.getAncestorId();
        this.templateId = entity.getTemplateId();

        this.ancestors = new HashSet<>();
        this.children = new HashSet<>();

        this.properties = new HashSet<>();
        this.propertyValues = new HashSet<>();
    }


    public AdoEntity createEntity() {
        return new AdoEntity()
                .setId(id)
                .setEid(eid)
                .setName(name)
                .setDescription(description)
                .setType(type.getId())
                .setIpbCode(ipbCode)
                .setMolId(molId)
                .setProcId(procId)
                .setSampleId(sampleId)
                .setCreatedAt(createdAt)
                .setCreatedBy(createdBy.toString())
                .setState(state)
                .setAncestorId(ancestorId)
                .setTemplateId(templateId);
    }

    // Getter and Setter
    public String getId() {
        return id;
    }

    public Ado setId(String id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public Ado setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public String getName() {
        return name;
    }

    public Ado setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Ado setDescription(String description) {
        this.description = description;
        return this;
    }

    public EntityType getType() {
        return type;
    }

    public Ado setType(EntityType type) {
        this.type = type;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public Ado setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    public String getMolId() {
        return molId;
    }

    public Ado setMolId(String molId) {
        this.molId = molId;
        return this;
    }

    public Integer getProcId() {
        return procId;
    }

    public Ado setProcId(Integer procId) {
        this.procId = procId;
        return this;
    }

    public String getSampleId() {
        return sampleId;
    }

    public Ado setSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Ado setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public IUser getCreatedBy() {
        return createdBy;
    }

    public Ado setCreatedBy(IUser createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getState() {
        return state;
    }

    public Ado setState(String state) {
        this.state = state;
        return this;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public Ado setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Ado setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public Set<SignalsEntity> getAncestors() {
        return ancestors;
    }

    public Ado setAncestors(Set<SignalsEntity> ancestors) {
        this.ancestors = ancestors;
        return this;
    }

    public Set<SignalsEntity> getChildren() {
        return children;
    }

    public Ado setChildren(Set<SignalsEntity> children) {
        this.children = children;
        return this;
    }

    public Set<AdoProperty> getProperties() {
        return properties;
    }

    public Ado setProperties(Set<AdoProperty> properties) {
        this.properties = properties;
        return this;
    }

    public Set<AdoPropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public Ado setPropertyValues(Set<AdoPropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
        return this;
    }

    public void addAncestor(SignalsEntity se) {
        this.ancestors.add(se);
    }

    public void addChild(SignalsEntity se) {
        this.children.add(se);
    }

    public void addProperty(AdoProperty adoProperty) {
        properties.add(adoProperty);
    }
    public void addPropertyValue(AdoPropertyValue adoPropertyValue) {
        propertyValues.add(adoPropertyValue);
    }

    @Override
    public String toString() {
        return "Ado{" +
                "id='" + id + '\'' +
                ", eid='" + eid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", ipbCode='" + ipbCode + '\'' +
                ", molId='" + molId + '\'' +
                ", procId=" + procId +
                ", sampleId='" + sampleId + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", state='" + state + '\'' +
                ", ancestorId='" + ancestorId + '\'' +
                ", templateId='" + templateId + '\'' +
                '}';
    }
}
