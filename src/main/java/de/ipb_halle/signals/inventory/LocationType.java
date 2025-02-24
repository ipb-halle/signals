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

import de.ipb_halle.signals.attachment.AttachmentEntity;
import de.ipb_halle.signals.field.Field;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Single signals entity (entities API endpoint)
 */

public class LocationType {

    public final static String ATTR_NAME = "name";
    private final static Logger logger = LogManager.getLogger(LocationType.class);
    private final static String LOCATION_TYPE_ENTITY_PREFIX = "location:";
    private final static String LOCATION_TYPE_ENTITY_SUFFIX = ":ivt";

    private String id;
    private String name;
    private String description;
    private boolean inUse;
    private boolean movable;
    private Date createdAt;
    private Date updatedAt;
    private Set<Field> fields;
    private Set<AttachmentEntity> attachments;

    //Default constructor
    public LocationType() {
        createdAt = new Date();
        updatedAt = new Date();
        attachments = new HashSet<>();
        fields = new HashSet<>();
    }

    public LocationType(LocationTypeEntity lte, List<Field> fd) {
        this.id = lte.getId();
        this.name = lte.getName();
        this.description = lte.getDescription();
        this.inUse = lte.isMovable();
        this.movable = lte.isMovable();
        this.createdAt = lte.getCreatedAt();
        this.updatedAt = lte.getUpdateAt();

        this.fields = new HashSet<>();
        fields.addAll(fd);
        this.attachments = new HashSet<>();
    }

    public LocationTypeEntity createEntity() {
        LocationTypeEntity lte = new LocationTypeEntity()
                .setId(this.id)
                .setName(this.name)
                .setDescription(this.description)
                .setCreatedAt(this.createdAt)
                .setUpdateAt(this.updatedAt)
                .setInUse(this.inUse)
                .setMovable(this.movable);
        return lte;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("LocationType(%s): %s\n", id, name));
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getSuffixPrefixId() {
        return LOCATION_TYPE_ENTITY_PREFIX + this.getId() + LOCATION_TYPE_ENTITY_SUFFIX;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public void addField(Field f) {
        fields.add(f);
    }

    public LocationType setId(String i) {
        id = i;
        return this;
    }

    public void setDescription(String d) {
        description = d;
    }

    public LocationType setName(String n) {
        name = n;
        return this;
    }

    public void addFields(Set<Field> fd) {
        fields = fd;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setFields(Set<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "LocationType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inUse=" + inUse +
                ", movable=" + movable +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", fields=" + fields +
                ", attachments=" + attachments +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LocationType that = (LocationType) object;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }
}
