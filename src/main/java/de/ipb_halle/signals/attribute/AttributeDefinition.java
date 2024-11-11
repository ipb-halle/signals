/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.attribute;

import jakarta.persistence.*;

/** 
 * Signals Attribute definition
 */

@Entity
@Table(name="attribute_definitions")
public class AttributeDefinition {

    @Id
    private String id;

    @Column(name = "attr_type")
    private Integer type;

    @Column
    private String name;

    @Column
    private String format;

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("AttributeDefinition(%s) --> %d\n", id,  type));
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public Integer getType() {
        return type;
    }

    public void setId(String i) {
        id = i;
    }

    public void setType(Integer t) {
        type = t;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
