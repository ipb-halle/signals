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


import de.ipb_halle.signals.dynEnum.DynEnumManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Signals Attribute definition
 */

public class Attribute {

    public final static String ATTR_FORMAT = "format";

    private String id;
    private AttributeType type;
    private String description;
    private String name;
    private String format;
    private Set<AttributeValue> options;

    /*
     * default constructor
     */
    public Attribute() {
        this.options = new HashSet<>();
    }

    public final static String ATTR_OPTIONS = "options";

    /*
     * entity constructor
     */
    public Attribute(AttributeDefinition def, DynEnumManager dynEnumMgr, List<AttributeValue> options) {
        this.id = def.getId();
        this.name = def.getName();
        this.format = def.getFormat();
        this.description = def.getDescription();
        this.type = (AttributeType) dynEnumMgr.valueOf(def.getType());
        if (options != null) {
            setOptions((Collection) options);
        }
    }

    public AttributeDefinition createEntity() {
        AttributeDefinition def = new AttributeDefinition();
        def.setId(id);
        def.setName(name);
        def.setDescription(description);
        def.setFormat(format);
        def.setType(type.getId());
        return def;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Attribute(%s) --> %d\n", id, type));
        return sb.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public AttributeType getType() {
        return type;
    }

    public void setId(String i) {
        id = i;
    }

    public void setType(AttributeType t) {
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

    public void addOption(String o) {
        options.add(new AttributeValue(id, o));
    }

    public Set<AttributeValue> getOptions() {
        return options;
    }

    public void setOptions(Collection<AttributeValue> options) {
        this.options = new HashSet<>();
        this.options.addAll(options);
    }
}
