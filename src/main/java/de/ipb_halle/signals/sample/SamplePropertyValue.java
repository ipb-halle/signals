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

public class SamplePropertyValue {

    private String sampleId;
    private String propertyId;
    private String propertyValue;

    public SamplePropertyValue() {
    }

    public SamplePropertyValue(SamplePropertyValueEntity spev) {
        this.sampleId = spev.getId().getSampleId();
        this.propertyId = spev.getId().getPropertyId();
        this.propertyValue = spev.getPropertyValue();
    }

    public SamplePropertyValueEntity createEntity() {
        SamplePropertyValueId samplePropertyValueId = new SamplePropertyValueId();
        samplePropertyValueId
                .setSampleId(sampleId)
                .setSampleId(propertyId);
        return new SamplePropertyValueEntity()
                .setId(samplePropertyValueId)
                .setPropertyValue(propertyValue);
    }

    //getter
    public String getSampleId() {
        return sampleId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    //setter
    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
