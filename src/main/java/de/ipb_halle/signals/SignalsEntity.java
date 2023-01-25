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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 
 * Single signals entity (entities API endpoint) 
 */

@Entity
@Table(name="signalsentities")
public class SignalsEntity {

    @Id
    private String id;

    @Column(name="snb_type")
    private String type;

    @Column(name="json_string")
    private String jsonString;


    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SignalsEntity(%s) --> %s\n", id,  type));
        sb.append(jsonString);
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getType() {
        return type;
    }

    public void setId(String i) {
        id = i;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }

    public void setType(String t) {
        type = t;
    }
}
