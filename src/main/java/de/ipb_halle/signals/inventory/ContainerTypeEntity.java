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
 * Container type entity
 */

@Entity
@Table(name="container_types")
public class ContainerTypeEntity {


    @Id
    private String id;

    @Column(name="created_at")
    private Date createdAt;

    @Column
    private String description;

    @Column(name="in_use")
    private boolean inUse;

    @Column
    private boolean movable;
    
    @Column
    private String name;

    @Column(name="json_string")
    private String jsonString;

    @Column(name="updated_at")
    private Date updatedAt;

    /**
     * default constructor
     */
    public ContainerTypeEntity() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    public String getId() {
        return id;
    }


    public Date getCreatedAt() {
        return createdAt;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public boolean isInUse() {
        return inUse;
    }

    public boolean isMovable() {
        return movable;
    }

    public ContainerTypeEntity setId(String i) {
        id = i;
        return this;
    }

    public ContainerTypeEntity setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public ContainerTypeEntity setDescription(String d) {
        description = d;
        return this;
    }

    public ContainerTypeEntity setMovable(boolean m) {
        movable = m;
        return this;
    }

    public ContainerTypeEntity setInUse(boolean u) {
        inUse = u;
        return this;
    }

    public ContainerTypeEntity setJsonString(String j) {
        jsonString = j;
        return this;
    }

    public ContainerTypeEntity setName(String n) {
        name = n;
        return this;
    }

    public ContainerTypeEntity setUpdatedAt(Date u) {
        updatedAt = u;
        return this;
    }
}
