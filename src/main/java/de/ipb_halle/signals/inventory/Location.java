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

package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Location {
    public static final String ATTR_ATTACHMENT_MIMETYPE = "attachment.mimeType";
    public static final String ATTR_ATTACHMENT_FILENAME = "attachment.fileName";
    public static final String ATTR_ATTACHMENT_FILE_SIZE = "attachment.fileSize";


    private String id;
    private String name;
    private String description;
    private String barcode;
    private boolean grid;
    private Integer rows;
    private Integer columns;
    private Date createdAt;
    private IUser createdBy;
    private String locationTypeId;
    private String typeName;
    private String ancestorId;
    private String ancestorName;
    private Date updatedAt;
    private IUser updatedBy;

    private Set<Field> fields;
    private Set<FieldValue> fieldValues;

    public Location() {
        fields = new HashSet<>();
        fieldValues = new HashSet<>();
    }

    public Location(LocationEntity le) {
        this.id = le.getId();
        this.name = le.getName();
        this.description = le.getDescription();
        this.barcode = le.getBarcode();
        this.grid = le.isGrid();
        this.rows = le.getRows();
        this.columns = le.getColumns();
        this.createdAt = le.getCreatedAt();
        this.createdBy = new UserReference(le.getCreatedBy());
        this.locationTypeId = le.getTypeId();
        this.typeName = le.getTypeName();
        this.ancestorId = le.getAncestorId();
        this.ancestorName = le.getAncestorName();
        this.updatedAt = le.getUpdatedAt();
        this.updatedBy = new UserReference(le.getUpdatedBy());

        this.fields = new HashSet<>();
        this.fieldValues = new HashSet<>();
    }

    public LocationEntity createEntity() {
        return new LocationEntity()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setBarcode(barcode)
                .setGrid(grid)
                .setRows(rows)
                .setColumns(columns)
                .setCreatedAt(createdAt)
                .setCreatedBy(createdBy.getId())
                .setTypeId(locationTypeId)
                .setTypeName(typeName)
                .setAncestorId(ancestorId)
                .setAncestorName(ancestorName)
                .setUpdatedAt(updatedAt)
                .setUpdatedBy(updatedBy.getId());
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

    public String getBarcode() {
        return barcode;
    }

    public boolean isGrid() {
        return grid;
    }

    public Integer getRows() {
        return rows;
    }

    public Integer getColumns() {
        return columns;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public IUser getCreatedBy() {
        return createdBy;
    }

    public String getLocationTypeId() {
        return locationTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public String getAncestorName() {
        return ancestorName;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setGrid(boolean grid) {
        this.grid = grid;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(IUser createdBy) {
        this.createdBy = createdBy;
    }

    public void setLocationTypeId(String locationTypeId) {
        this.locationTypeId = locationTypeId;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
    }

    public void setAncestorName(String ancestorName) {
        this.ancestorName = ancestorName;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(IUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void addFields(Collection<Field> fields) {
        this.fields.addAll(fields);
    }

    public void addFieldValues(Collection<FieldValue> fieldValues) {
        this.fieldValues.addAll(fieldValues);
    }

    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", barcode='" + barcode + '\'' +
                ", grid=" + grid +
                ", rows=" + rows +
                ", columns=" + columns +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", locationTypeId='" + locationTypeId + '\'' +
                ", typeName='" + typeName + '\'' +
                ", ancestorId='" + ancestorId + '\'' +
                ", ancestorName='" + ancestorName + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", fields=" + fields +
                ", fieldValues=" + fieldValues +
                '}';
    }
}
