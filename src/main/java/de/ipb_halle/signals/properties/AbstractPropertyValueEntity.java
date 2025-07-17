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

package de.ipb_halle.signals.properties;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class AbstractPropertyValueEntity<ID extends AbstractPropertyValueId, E extends AbstractPropertyEntity> {

    @EmbeddedId
    private ID id;

    @Column(name = "property_value")
    private String propertyValue;

    @ManyToOne
    @MapsId("propertyId")
    @JoinColumn(name="property_id")
    private E property;

    public ID getId() { return id; }
    public AbstractPropertyValueEntity<ID, E> setId(ID id) {
        this.id = id; return this;
    }

    public String getPropertyValue() { return propertyValue; }
    public AbstractPropertyValueEntity<ID, E> setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public E getProperty() { return property; }

    public AbstractPropertyValueEntity<ID, E> setProperty(E property) {
        this.property = property; return this;
    }
}
