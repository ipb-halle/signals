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

import de.ipb_halle.signals.field.Field;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.Set;

/**
 * Single signals entity (entities API endpoint)
 */

@Entity
@Table(name="location_types")
public class LocationType {

    public final static String ATTR_NAME = "name";

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    private transient Set<Field> fields;

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("LocationType(%s): %s\n", id, name));
        return sb.toString();
    }

    public String getId() {
        return id;
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

    public void setId(String i) {
        id = i;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setName(String n) {
        name = n;
    }

    public void addFields(Set<Field> fd) {
        fields = fd;
    }

    @Override
    public String toString() {
        return "LocationType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
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
