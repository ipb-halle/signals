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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * Single signals entity (entities API endpoint) 
 */

@Entity
@Table(name="container_types")
public class ContainerType {

    public final static String ATTR_ID = "id";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_NAME = "name";

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name="json_string")
    private String jsonString;


    public void dump() {
        System.out.printf("ContainerType(%s): %s\n", id, name);
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
        return jsonString;
    }

    public void setId(String i) {
        id = i;
    }

    public void setName(String n) {
        name = n;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }
}
