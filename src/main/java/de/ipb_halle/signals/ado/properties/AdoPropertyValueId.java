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

import de.ipb_halle.signals.properties.AbstractPropertyValueId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AdoPropertyValueId implements Serializable {

    @Column(name = "ado_id")
    private String adoId;

    @Column(name = "property_id")
    private String propertyId;

    public AdoPropertyValueId() {}

    public AdoPropertyValueId(String adoId, String propertyId) {
        this.adoId = adoId;
        this.propertyId = propertyId;
    }

    public String getAdoId() {
        return adoId;
    }

    public AdoPropertyValueId setAdoId(String adoId) {
        this.adoId = adoId;
        return this;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public AdoPropertyValueId setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdoPropertyValueId that)) return false;
        return Objects.equals(adoId, that.adoId) &&
                Objects.equals(propertyId, that.propertyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adoId, propertyId);
    }
}
