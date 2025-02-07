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

import java.util.Objects;
import jakarta.persistence.*;

/**
 * Signals Attribute values
 */

@Entity
@Table(name="attribute_values")
public class AttributeValue {

    @EmbeddedId
    private AttributeValueId id;

    /*
     * default constructor for persistence API
     */
    public AttributeValue() {
        new AttributeValueId();
    }

    public AttributeValue(String id, String value) {
        this.id = new AttributeValueId(id, value);
    }

    private AttributeValueId getEmbeddedId() {
        return id;
    }

    public String getId() {
        return id.getId();
    }

    public String getValue() {
        return id.getValue();
    }

    public void setId(String i) {
        id.setId(i);
    }

    public void setValue(String v) {
        id.setValue(v);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof AttributeValue)) {
            AttributeValue other = (AttributeValue) obj;
            return this.id.equals(other.getEmbeddedId());
        }
        return false;
    }
}
