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

import org.apache.logging.log4j.Logger;

public abstract class AbstractPropertyValue<P extends AbstractProperty> {

    private String entityId;
    private String propertyId;
    private String propertyValue;
    private P property;

    public String getEntityId() { return entityId; }
    public AbstractPropertyValue<P> setEntityId(String entityId) {
        this.entityId = entityId; return this;
    }

    public String getPropertyId() { return propertyId; }
    public AbstractPropertyValue<P> setPropertyId(String propertyId) {
        this.propertyId = propertyId; return this;
    }

    public String getPropertyValue() { return propertyValue; }
    public AbstractPropertyValue<P> setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue; return this;
    }

    public P getProperty() { return property; }
    public AbstractPropertyValue<P> setProperty(P property) {
        this.property = property; return this;
    }

    public AbstractPropertyValueEntity<?, ?> createEntity() {
        if (getEntityId() == null || getPropertyId() == null) {
            getLogger().warn("Skipping property value creation: entityId={}, propertyId={}", getEntityId(), getPropertyId());
            return null;
        }

        AbstractPropertyValueId id = createId(getEntityId(), getPropertyId());
        return createEntityInstance(id, getPropertyValue());
    }

    protected abstract Logger getLogger();

    protected abstract AbstractPropertyValueId createId(String entityId, String propertyId);

    protected abstract AbstractPropertyValueEntity<?, ?> createEntityInstance(AbstractPropertyValueId id, String value);
}

