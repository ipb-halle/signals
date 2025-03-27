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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class SamplePropertyValueId {

    @Column(name="sample_id")
    private String sampleId;

    @Column(name= "property_id")
    private String propertyId;

    public SamplePropertyValueId() {
    }

    public SamplePropertyValueId(String sampleId, String propertyId) {
        this.sampleId = sampleId;
        this.propertyId = propertyId;
    }

    //getter and setter

    public String getSampleId() {
        return sampleId;
    }

    public SamplePropertyValueId setSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public SamplePropertyValueId setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SamplePropertyValueId that = (SamplePropertyValueId) object;
        return Objects.equals(sampleId, that.sampleId) && Objects.equals(propertyId, that.propertyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleId, propertyId);
    }
}
