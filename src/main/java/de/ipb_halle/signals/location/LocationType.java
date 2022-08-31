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
@Table(name="location_types")
public class LocationType {

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String json_string;

    private transient JsonElement json;

    public static LocationType createLocationType(JsonElement j) {
        LocationType lt = new LocationType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        lt.id = j.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        lt.json = j;
        lt.json_string = j.toString();

        lt.name = attributes.getAsJsonPrimitive("name").getAsString();
        lt.description = attributes.getAsJsonPrimitive("description").getAsString();
        return lt;
    }

    public void dump() {
        System.out.println("LocationType " + id);
        System.out.println(json.toString());
        System.out.println("==============================================================");
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

    public String getJsonString() {
        return json_string;
    }

    public void setId(String id) {
        id = id;
    }

    public void setName(String n) {
        name = n;
    }

    public void setDescription(String d) {
        description = d;
    }
}
