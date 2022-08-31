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
package de.ipb_halle.signals;

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
@Table(name="signalsentities")
public class SignalsEntity {

    @Id
    private String id;

    @Column
    private String type;

    @Column
    private String json_string;

    private transient JsonElement json;

    public static SignalsEntity createSignalsEntity(JsonElement json) {
        SignalsEntity entity = new SignalsEntity();
        entity.json = json;
        entity.id = json.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        entity.type = json.getAsJsonObject().getAsJsonObject("attributes").getAsJsonPrimitive("type").getAsString();
        entity.json_string = json.toString();
        return entity;
    }

    public void dump() {
        System.out.println(id + " --> " + type);
        System.out.println(json.toString());
        System.out.println("==============================================================");
    }

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return json_string;
    }

    public String getType() {
        return type;
    }
}
