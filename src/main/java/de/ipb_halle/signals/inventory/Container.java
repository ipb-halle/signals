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
package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.materials.IMaterial;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Single signals entity (entities API endpoint)
 */

public class Container {

    public final static String ATTR_ATTACHMENT_FILENAME = "attachment.filename";
    public final static String ATTR_ATTACHMENT_FILESIZE = "attachment.size";
    public final static String ATTR_ATTACHMENT_MIMETYPE = "attachment.mimeType";
    public final static String CONTAINER_TYPE_ENTITY_PREFIX = "container:";
    public final static String CONTAINER_TYPE_ENTITY_SUFFIX = ":ivt";

    private String id;
    private String description;
    private Double amount;
    private String barcode;
    private ContainerType containerType;
    private String containerTypeId;
    private String containerTypeName;
    private Integer coordinateX;
    private Integer coordinateY;
    private Date createdAt;
    private IUser createdBy;
    private String digest;
    private ILocation location;
    private IMaterial material;
    private String name;
    private Date updatedAt;
    private IUser updatedBy;
    private Unit unit;

    private Set<Field> fields;
    private Set<FieldValue> fieldValues;
    private Set<IMaterial> materials;
    private Set<Sample> samples;

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Container(%s): name=%s barcode=%s\n", id, name, barcode));
        sb.append(String.format("  createdBy='%s', updatedBy='%s'\n", createdBy.dump(), updatedBy.dump()));
        sb.append(String.format("  amount=%f, location='%s'\n", amount, location.dump()));
        return sb.toString();
    }

    /**
     * default constructor
     */
    public Container() {
        fields = new HashSet<>();
        fieldValues = new HashSet<>();
        materials = new HashSet<>();
        samples = new HashSet<>();
    }

    // entity constructor
    public Container(ContainerEntity ce) {
        id = ce.getId();
        description = ce.getDescription();
        amount = ce.getAmount();
        barcode = ce.getBarcode();
        containerTypeId = ce.getContainerTypeId();
        containerTypeName = ce.getContainerTypeName();
        coordinateX = ce.getCoordinateX();
        coordinateY = ce.getCoordinateY();
        createdAt = ce.getCreatedAt();
        createdBy = new UserReference(ce.getCreatedBy());
        location = new LocationReference().setId(ce.getLocationId());
        material = new MaterialReference().setId(ce.getMaterialId());
        name = ce.getName();
        unit = Unit.getUnit(ce.getUnit());
        updatedAt = ce.getUpdatedAt();
        updatedBy = new UserReference(ce.getUpdatedBy());

        fields = new HashSet<>();
        fieldValues = new HashSet<>();
        fieldValues.addAll(ce.getFieldValues());
        materials = new HashSet<>();
        samples = new HashSet<>();
    }

    public ContainerEntity createEntity() {
        return new ContainerEntity()
                .setId(id)
                .setDescription(description)
                .setAmount(amount)
                .setBarcode(barcode)
                .setCoordinateX(coordinateX)
                .setCoordinateY(coordinateY)
                .setCreatedAt(createdAt)
                .setCreatedBy(createdBy.getId())
                .setDigest(digest)
                .setLocationId(location.getId())
                .setMaterialId(material.getId())
                .setContainerTypeId(containerTypeId)
                .setName(name)
                .setUnit(unit.getUnit())
                .setUpdatedAt(updatedAt)
                .setUpdatedBy(updatedBy.getId())
                .setContainerTypeName(containerTypeName);
    }

    public void addFields(Collection<Field> fields) {
        this.fields.addAll(fields);
    }

    public void addFieldValues(Collection<FieldValue> values) {
        this.fieldValues.addAll(values);
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public void addMaterial(IMaterial m) {
        materials.add(m);
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getCoordinateX() {
        return coordinateX;
    }

    public Integer getCoordinateY() {
        return coordinateY;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getContainerTypeId() {
        return containerTypeId;
    }

    public String getContainerTypeName() {
        return containerTypeName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public IUser getCreatedBy() {
        return createdBy;
    }

    public String getDigest() {
        return digest;
    }

    public String getId() {
        return id;
    }

    public ILocation getLocation() {
        return location;
    }

    public IMaterial getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public Unit getUnit() {
        return unit;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public IUser getUpdatedBy() {
        return updatedBy;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public Set<FieldValue> getFieldValues() {
        return fieldValues;
    }

    public Set<IMaterial> getMaterials() {
        return materials;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(Double a) {
        amount = a;
    }

    public void setBarcode(String b) {
        barcode = b;
    }

    public void setCoordinateX(Integer x) {
        coordinateX = x;
    }

    public void setCoordinateY(Integer y) {
        coordinateY = y;
    }

    public void setContainerType(ContainerType t) {
        containerType = t;
    }

    public void setContainerTypeId(String i) {
        containerTypeId = i;
    }

    public void setContainerTypeName(String n) {
        containerTypeName = n;
    }

    public void setCreatedAt(Date d) {
        createdAt = d;
    }

    public void setCreatedBy(IUser u) {
        createdBy = u;
    }

    public void setDigest(String d) {
        digest = d;
    }

    public void setFieldValues(Set<FieldValue> vs) {
        fieldValues = vs;
    }

    public void setId(String i) {
        id = i;
    }

    public void setLocation(ILocation l) {
        location = l;
    }

    public void setMaterial(IMaterial material) {
        this.material = material;
    }

    public void setMaterials(Set<IMaterial> ms) {
        materials = ms;
    }

    public void setName(String n) {
        name = n;
    }

    public void setUnit(Unit u) {
        unit = u;
    }

    public void setUpdatedAt(Date d) {
        updatedAt = d;
    }

    public void setUpdatedBy(IUser u) {
        updatedBy = u;
    }

    public void setFields(Set<Field> fields) {
        this.fields = fields;
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void addSamples(Sample sample) {
        samples.add( sample);
    }
}
