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

import de.ipb_halle.signals.field.FieldValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Container entity
 */

@Entity
@Table(name = "containers")
public class ContainerEntity {
    public final static String ATTR_AMOUNT = "amount";
    public final static String ATTR_BARCODE = "barcode";
    public final static String ATTR_COORDINATE_X = "coordinateX";
    public final static String ATTR_COORDINATE_Y = "coordinateY";
    public final static String ATTR_CONTAINER_TYPE_ID = "typeId";
    public final static String ATTR_CONTAINER_TYPE_NAME = "typeName";
    public final static String ATTR_CONTENTS = "contents";
    public final static String ATTR_CONTENT_ID = "entityId";
    public final static String ATTR_CONTENT_TYPE = "entityType";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_CREATED_BY = "relationships.createdBy.data.id";
    public final static String ATTR_LOCATION_ID = "location.id";
    public final static String ATTR_UNIT = "unit";
    public final static String ATTR_UPDATED_AT = "updatedAt";
    public final static String ATTR_UPDATED_BY = "relationships.updatedBy.data.id";
    public final static String CONTENT_TYPE_ASSET = "asset";
    public final static String CONTENT_TYPE_BATCH = "batch";
    public final static String CONTENT_TYPE_SAMPLE = "sample";
    public final static String ENTITY_TYPE_CONTAINER = "container";

    @Id
    private String id;

    @Column(name = "description")
    private String description;

    @Column
    private Double amount;

    @Column
    private String barcode;

    @Column(name = "container_type_id")
    private String containerTypeId;

    @Column(name = "coordinate_x")
    private Integer coordinateX;

    @Column(name = "coordinate_y")
    private Integer coordinateY;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column
    private String digest;

    @Column(name = "location_id")
    private String locationId;

    @Column(name = "material_id")
    private String materialId;

    @Column
    private String name;

    @Column
    private String unit;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "type_name")
    private String containerTypeName;

    private transient Set<FieldValue> fieldValues;

    public ContainerEntity() {

        fieldValues = new HashSet<>();
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Container(%s): name=%s\n", id, name));
        return sb.toString();
    }

    public void addAllFieldValues(List<FieldValue> fieldValues) {
        this.fieldValues.addAll(fieldValues);
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

    public String getContainerTypeId() {
        return containerTypeId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getDigest() {
        return digest;
    }

    public String getId() {
        return id;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getContainerTypeName() {
        return containerTypeName;
    }

    public Set<FieldValue> getFieldValues() {
        return fieldValues;
    }

    public ContainerEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public ContainerEntity setAmount(Double a) {
        amount = a;
        return this;
    }

    public ContainerEntity setBarcode(String b) {
        barcode = b;
        return this;
    }

    public ContainerEntity setCoordinateX(Integer x) {
        coordinateX = x;
        return this;
    }

    public ContainerEntity setCoordinateY(Integer y) {
        coordinateY = y;
        return this;
    }

    public ContainerEntity setContainerTypeId(String i) {
        containerTypeId = i;
        return this;
    }

    public ContainerEntity setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public ContainerEntity setCreatedBy(String u) {
        createdBy = u;
        return this;
    }

    public ContainerEntity setDigest(String d) {
        digest = d;
        return this;
    }

    public ContainerEntity setId(String i) {
        id = i;
        return this;
    }

    public ContainerEntity setLocationId(String i) {
        locationId = i;
        return this;
    }

    public ContainerEntity setMaterialId(String materialId) {
        this.materialId = materialId;
        return this;
    }

    public ContainerEntity setName(String n) {
        name = n;
        return this;
    }

    public ContainerEntity setUnit(String u) {
        unit = u;
        return this;
    }

    public ContainerEntity setUpdatedAt(Date d) {
        updatedAt = d;
        return this;
    }

    public ContainerEntity setUpdatedBy(String u) {
        updatedBy = u;
        return this;
    }

    public ContainerEntity setContainerTypeName(String name) {
        this.containerTypeName = name;
        return this;
    }

    public ContainerEntity setFieldValues(Set<FieldValue> fieldValues) {
        this.fieldValues.addAll(fieldValues);
        return this;
    }

    @Override
    public String toString() {
        return "ContainerEntity{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", barcode='" + barcode + '\'' +
                ", containerTypeId='" + containerTypeId + '\'' +
                ", coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", digest='" + digest + '\'' +
                ", locationId='" + locationId + '\'' +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", containerTypeName='" + containerTypeName + '\'' +
                '}';
    }


}
