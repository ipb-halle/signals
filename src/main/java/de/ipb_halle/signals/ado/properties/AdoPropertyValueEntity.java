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

package de.ipb_halle.signals.ado.properties;

import jakarta.persistence.*;

@Entity
@Table(name = "ado_property_values")
public class AdoPropertyValueEntity {

    @EmbeddedId
    private AdoPropertyValueId id;

    @Column(name = "property_value")
    private String propertyValue;

    @ManyToOne
    @JoinColumn(name = "property_id", referencedColumnName = "property_id", insertable = false, updatable = false)
    private AdoPropertyEntity property;

    public AdoPropertyValueEntity() {
    }

    // ——— getters ——— //

    public AdoPropertyValueId getId() {
        return id;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public AdoPropertyEntity getProperty() {
        return property;
    }

    // ——— Setters ——— //

    public AdoPropertyValueEntity setId(AdoPropertyValueId id) {
        this.id = id;
        return this;
    }

    public AdoPropertyValueEntity setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public AdoPropertyValueEntity setProperty(AdoPropertyEntity property) {
        this.property = property;
        return this;
    }
}
