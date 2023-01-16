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

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * Single signals entity (entities API endpoint) 
 */

@Entity
@Table(name="locations")
public class LocationEntity implements ILocation {

    public final static String ATTR_ANCESTORS = "ancestors";
    public final static String ATTR_ANCESTOR_ID = "id";
    public final static String ATTR_ANCESTOR_NAME = "name";
    public final static String ATTR_BARCODE = "barcode";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_CREATED_BY = "relationships.createdBy.data.id";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_GRID = "isGrid";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE_ID = "typeId";
    public final static String ATTR_TYPE_NAME = "typeName";
    public final static String ATTR_UPDATED_AT = "createdAt";
    public final static String ATTR_UPDATED_BY = "relationships.updatedBy.data.id";

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

    @Column(name="grid_rows")
    private Integer rows;

    @Column(name="grid_columns")
    private Integer columns;

    @Column(name="created_at")
    private Date createdAt;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="type_id")
    private String typeId;

    @Column(name="type_name")
    private String typeName;

    @Column(name="ancestor_id")
    private String ancestorId;

    @Column(name="ancestor_name")
    private String ancestorName;

    @Column(name="json_string")
    private String jsonString;

    @Column(name="updated_at")
    private Date updatedAt;

    @Column(name="updated_by")
    private String updatedBy;

    private transient LocationType type;
    private transient ILocation ancestor;

    /**
     * default constructor
     */
    public LocationEntity() {
        grid = false;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Location(%s): name=%s\n",id, name));
        return sb.toString();
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

    public String getJsonString() {
        return jsonString;
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

    public boolean isGrid() {
        return grid;
    }

    public ILocation setId(String i) {
        id = i;
        return this;
    }

    public void setAncestorId(String id) {
        ancestorId = id;
    }

    public void setAncestorName(String n) {
        ancestorName = n;
    }

    public void setBarcode(String b) {
        barcode = b;
    }

    public void setColumns(Integer col) {
        columns = col;
        grid = true;
    }

    public void setCreatedAt(Date d) {
        createdAt = d;
    }

    public void setCreatedBy(String u) {
        createdBy = u;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setGrid(Boolean g) {
        grid = g;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }

    public void setName(String n) {
        name = n;
    }

    public void setRows(Integer r) {
        rows = r;
        grid = true;
    }

    public void setType(LocationType t) {
        type = t;
        typeId = t.getId();
        typeName = t.getName();
    }

    public void setTypeId(String i) {
        typeId = i;
    }

    public void setTypeName(String n) {
        typeName = n;
    }

    public void setUpdatedAt(Date d) {
        updatedAt = d;
    }

    public void setUpdatedBy(String u) {
        updatedBy = u;
    }
}
