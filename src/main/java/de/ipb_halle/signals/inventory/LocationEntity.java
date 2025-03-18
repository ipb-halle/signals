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

import java.util.*;

/**
 * Location entity (/inventory/locations/ API endpoint)
 */

@Entity
@Table(name = "locations")
public class LocationEntity implements ILocation {

    public final static String ATTR_ANCESTORS = "ancestors";
    public final static String ATTR_ANCESTOR_ID = "id";
    public final static String ATTR_ANCESTOR_NAME = "name";
    public final static String ATTR_BARCODE = "barcode";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_CREATED_BY = "relationships.createdBy.data.id";
    public final static String ATTR_GRID = "isGrid";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE_ID = "typeId";
    public final static String ATTR_TYPE_NAME = "typeName";
    public final static String ATTR_UPDATED_AT = "createdAt";
    public final static String ATTR_UPDATED_BY = "relationships.updatedBy.data.id";

    public final static String ENTITY_TYPE_LOCATION = "location";
    public static final String ATTR_ROWS = "rows";
    public static final String ATTR_COLUMNS = "columns";

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String barcode;

    @Column
    private boolean grid;

    @Column(name = "grid_rows")
    private Integer rows;

    @Column(name = "grid_columns")
    private Integer columns;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "type_id")
    private String typeId;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "ancestor_id")
    private String ancestorId;

    @Column(name = "ancestor_name")
    private String ancestorName;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    private transient LocationType type;
    private transient ILocation ancestor;
    private transient Set<FieldValue> fieldValues;

    /**
     * default constructor
     */
    public LocationEntity() {
        grid = false;
        fieldValues = new HashSet<>();
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Location(%s): name=%s\n", id, name));
        return sb.toString();
    }

    public void addAllFieldValues(List<FieldValue> fieldValues) {
        this.fieldValues.addAll(fieldValues);
    }

    public String getId() {
        return id;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public String getAncestorName() {
        return ancestorName;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getColumns() {
        return columns;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Integer getRows() {
        return rows;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Set<FieldValue> getFieldValues() {
        return fieldValues;
    }

    public boolean isGrid() {
        return grid;
    }

    public LocationEntity setId(String i) {
        id = i;
        return this;
    }

    public LocationEntity setAncestorId(String id) {
        ancestorId = id;
        return this;
    }

    public LocationEntity setAncestorName(String n) {
        ancestorName = n;
        return this;
    }

    public LocationEntity setBarcode(String b) {
        barcode = b;
        return this;
    }

    public LocationEntity setColumns(Integer col) {
        columns = col;
        grid = true;
        return this;
    }

    public LocationEntity setCreatedAt(Date d) {
        createdAt = d;
        return this;

    }

    public LocationEntity setCreatedBy(String u) {
        createdBy = u;
        return this;

    }

    public LocationEntity setDescription(String d) {
        description = d;
        return this;

    }

    public LocationEntity setGrid(Boolean g) {
        grid = g;
        return this;

    }

    public LocationEntity setName(String n) {
        name = n;
        return this;

    }

    public LocationEntity setRows(Integer r) {
        rows = r;
        grid = true;
        return this;

    }

    public LocationEntity setType(LocationType t) {
        type = t;
        typeId = t.getId();
        typeName = t.getName();
        return this;

    }

    public LocationEntity setTypeId(String i) {
        typeId = i;
        return this;

    }

    public LocationEntity setTypeName(String n) {
        typeName = n;
        return this;

    }

    public LocationEntity setUpdatedAt(Date d) {
        updatedAt = d;
        return this;

    }

    public LocationEntity setUpdatedBy(String u) {
        updatedBy = u;
        return this;

    }

    public LocationEntity setFieldValues(Set<FieldValue> vs) {
        fieldValues = vs;
        return this;

    }


    @Override
    public String toString() {
        return "LocationEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", barcode='" + barcode + '\'' +
                ", grid=" + grid +
                ", rows=" + rows +
                ", columns=" + columns +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", typeId='" + typeId + '\'' +
                ", typeName='" + typeName + '\'' +
                ", ancestorId='" + ancestorId + '\'' +
                ", ancestorName='" + ancestorName + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", type=" + type +
                ", ancestor=" + ancestor +
                ", fieldValues=" + fieldValues +
                '}';
    }


}
