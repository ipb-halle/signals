/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.field.Field;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "location_types")
public class LocationTypeEntity {
    private final static String LOCATION_TYPE_ENTITY_PREFIX = "location:";
    private final static String LOCATION_TYPE_ENTITY_SUFFIX = ":ivt";

    @Id
    @Column
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name = "in_use")
    private boolean inUse;

    @Column(name = "movable")
    private boolean movable;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updateAt;

    private transient Set<Field> fields;

    //default constructor
    public LocationTypeEntity() {
        createdAt = new Date();
        updateAt = new Date();
        fields = new HashSet<>();
    }

    public LocationTypeEntity addSuffixPrefix() {
        return this.setId(LOCATION_TYPE_ENTITY_PREFIX + this.getId() + LOCATION_TYPE_ENTITY_SUFFIX);
    }

    public String getId() {
        return id;
    }

    public LocationTypeEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public LocationTypeEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LocationTypeEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isInUse() {
        return inUse;
    }

    public LocationTypeEntity setInUse(boolean inUse) {
        this.inUse = inUse;
        return this;
    }

    public boolean isMovable() {
        return movable;
    }

    public LocationTypeEntity setMovable(boolean movable) {
        this.movable = movable;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public LocationTypeEntity setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public LocationTypeEntity setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
        return this;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public LocationTypeEntity setFields(Set<Field> fields) {
        this.fields = fields;
        return this;
    }

    @Override
    public String toString() {
        return "LocationTypeEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inUse=" + inUse +
                ", movable=" + movable +
                ", createdAt=" + createdAt +
                ", updateAt=" + updateAt +
                ", fields=" + fields +
                '}';
    }
}
