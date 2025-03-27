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

public class SampleProperty {
    private String propertyId;
    private String propertyName;
    private String propertyType;

    public SampleProperty() {
    }

    public SampleProperty(SamplePropertyEntity spe) {
        this.propertyId = spe.getPropertyId();
        this.propertyName = spe.getPropertyName();
        this.propertyType = spe.getPropertyType();
    }

    public SamplePropertyEntity createEntity() {
        SamplePropertyEntity entity = new SamplePropertyEntity()
                .setPropertyId(propertyId)
                .setPropertyName(propertyName)
                .setPropertyType(propertyType);
        return entity;
    }

    //getter
    public String getPropertyType() {
        return propertyType;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public String getPropertyId() {
        return propertyId;
    }


    //setter
    public SampleProperty setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }
    public SampleProperty setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }
    public SampleProperty setPropertyType(String propertyType) {
        this.propertyType = propertyType;
        return this;
    }


}
