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
package de.ipb_halle.signals.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/** 
 * SNB group 
 */

@Entity
@Table(name="groups")
public class Group {

    public final static String ATTR_ID = "id";
    public final static String ATTR_CREATED_AT= "createdAt";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_DIGEST = "digest";
    public final static String ATTR_EDITED_AT= "editedAt";
    public final static String ATTR_EID = "eid";
    public final static String ATTR_FLAGS = "flags";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_SYSTEM = "isSystem";
    public final static String ATTR_TYPE = "type";

    @Id
    private Integer id;

    @Column(name="created_at")
    private Date createdAt;

    @Column
    private String description;

    @Column
    private String digest;

    @Column(name="edited_at")
    private Date editedAt;

    @Column
    private String name;

    @Column(name="is_system")
    private boolean system;

    @Column
    private String type;

    @Column
    private String json_string;

    private transient SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private transient JsonElement json;


    public static Group createGroup(JsonElement j) {
        Group group = new Group();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        group.id = j.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
        group.json = j;
        group.json_string = j.toString();

        group.createdAt = group.parseDate(attributes.getAsJsonPrimitive(ATTR_CREATED_AT).getAsString());
        group.description = attributes.getAsJsonPrimitive(ATTR_DESCRIPTION).getAsString();
        // digest
        // eid
        group.editedAt = group.parseDate(attributes.getAsJsonPrimitive(ATTR_EDITED_AT).getAsString());
        // flags
        group.name = attributes.getAsJsonPrimitive(ATTR_NAME).getAsString();
        group.system = attributes.getAsJsonPrimitive(ATTR_SYSTEM).getAsBoolean();
        group.type = attributes.getAsJsonPrimitive(ATTR_TYPE).getAsString();

        return group;
    }

    public void dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Group(%d) --> %s\n", id, name));
        sb.append("\n==============================================================");
        System.out.println(sb.toString());
    }

    public Integer getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public String getName() {
        return name;
    }

    public String getJsonString() {
        return json_string;
    }

    public boolean isSystem() {
        return system;
    }

    public Date parseDate(String ds) {
        try {
            return dateFormat.parse(ds);
        } catch(Exception e) {
        }
        return new Date();
    }

    public void setId(Integer id) {
        id = id;
    }

    public void setCreatedAt(Date d) {
        createdAt = d;
    }

    public void setDescription(String d) { 
        description = d;
    }

    public void setEditedAt(Date d) {
        editedAt = d;
    }

    public void setName(String n) {
        name = n;
    }

    public void setSystem(boolean b) {
        system = b;
    }
}
