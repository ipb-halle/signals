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
@Table(name = "sample_property_values")
public class SamplePropertyValueEntity {

    @EmbeddedId
    private SamplePropertyValueId id;

//    @MapsId("sampleId")
//    @Column(name="sample_id")
//    private String sampleId;
//
//    @MapsId("propertyId")
//    @Column(name="property_id")
//    private String propertyId;

    @Column(name = "property_value")
    private String propertyValue;

    public SamplePropertyValueEntity() {
    }

    public SamplePropertyValueId getId() {
        return id;
    }

    public SamplePropertyValueEntity setId(SamplePropertyValueId id) {
        this.id = id;
        return this;
    }
    //    //getter
//    public String getSampleId() {
//        return sampleId;
//    }
//
//    public String getPropertyId() {
//        return propertyId;
//    }

    public String getPropertyValue() {
        return propertyValue;
    }

//    //setter
//    public SamplePropertyValueEntity setSampleId(String sampleId) {
//        this.sampleId = sampleId;
//        return this;
//    }
//
//    public SamplePropertyValueEntity setPropertyId(String propertyId) {
//        this.propertyId = propertyId;
//        return this;
//    }

    public SamplePropertyValueEntity setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }
}
