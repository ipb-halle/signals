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
package de.ipb_halle.signals.materials;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/** 
 * Database entity for material library (materials API endpoint) 
 */

@Entity
public class Library {

    public final static String ATTR_ID = "id";

    @Id
    private String id;

    @Column(name="json_string")
    private String jsonString;
    
    private transient JsonElement json;


    public static Library createEntity(JsonElement j) {
        Library lib = new Library();
        lib.setId(j.getAsJsonObject().getAsJsonPrimitive(Library.ATTR_ID).getAsString());
        lib.setJsonString(j.toString());
        return lib;
    }

    public void dump() {
        System.out.println(id);
        System.out.println(jsonString);
        System.out.println("============================================================");
    }

    public void setId(String i) {
        id = i;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }
}
