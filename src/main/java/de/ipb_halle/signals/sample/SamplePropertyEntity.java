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

package de.ipb_halle.signals.sample;

import jakarta.persistence.*;

@Entity
@Table(name = "sample_properties")
public class SamplePropertyEntity {

    @Id
    @Column(name = "property_id")
    private String propertyId;

    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "property_type")
    private String propertyType;

    public SamplePropertyEntity() {
    }

    // ——— getters ——— //

    public String getPropertyId() {
        return propertyId;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public String getPropertyName() {
        return propertyName;
    }


    // ——— Setters ——— //
    public SamplePropertyEntity setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    public SamplePropertyEntity setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public SamplePropertyEntity setPropertyType(String propertyType) {
        this.propertyType = propertyType;
        return this;
    }

}
