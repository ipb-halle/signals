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
package de.ipb_halle.signals.location;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * Single signals entity (entities API endpoint) 
 */

@Entity
@Table(name="locations")
public class Location {

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

    @Column(name="type_id")
    private String typeId;

    @Column(name="type_name")
    private String typeName;

    @Column(name="ancestor_id")
    private String ancestorId;

    @Column(name="ancestor_name")
    private String ancestorName;

    @Column
    private String json_string;


    private transient LocationType type;
    private transient Location ancestor;
    private transient JsonElement json;

    /**
     * default constructor
     */
    public Location() {
        grid = false;
    }

    public static Location createLocation(JsonElement j) {
        Location loc = new Location();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        loc.id = j.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        loc.json = j;
        loc.json_string = j.toString();

        loc.barcode = attributes.getAsJsonPrimitive("barcode").getAsString();
        loc.name = attributes.getAsJsonPrimitive("name").getAsString();
        loc.description = attributes.getAsJsonPrimitive("description").getAsString();
        loc.grid = attributes.getAsJsonPrimitive("isGrid").getAsBoolean();
        loc.typeId = attributes.getAsJsonPrimitive("typeId").getAsString();
        loc.typeId = attributes.getAsJsonPrimitive("typeName").getAsString();

        loc.setAncestor(attributes.getAsJsonArray("ancestors"));
        return loc;
    }

    public void dump() {
        System.out.println("Location " + id);
        System.out.println(json.toString());
        System.out.println("==============================================================");
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public String getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getJsonString() {
        return json_string;
    }

    public void setAncestor(JsonArray ancestors) {
        if (ancestors.size() > 0) {
            JsonObject obj = ancestors.get(0).getAsJsonObject();
            ancestorId = obj.getAsJsonPrimitive("id").getAsString();
            ancestorName = obj.getAsJsonPrimitive("name").getAsString();
            return;
        }
        ancestor = null;
        ancestorId = null;
        ancestorName = null;
    }

    public void setId(String id) {
        id = id;
    }

    public void setAncestor(Location l) {
        ancestor = l;
        ancestorId = l.getAncestorId();
        ancestorName = l.getAncestorName();
    }

    public void setBarcode(String b) {
        barcode = b;
    }

    public void setName(String n) {
        name = n;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setGrid(Boolean g) {
        grid = g;
    }

    public void setColumns(Integer col) {
        columns = col;
        grid = true;
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
    
}
