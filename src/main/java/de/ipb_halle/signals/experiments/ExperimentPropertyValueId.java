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

package de.ipb_halle.signals.experiments;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ExperimentPropertyValueId {

    @Column(name="experiment_id")
    private String experimentId;

    @Column(name= "property_id")
    private String propertyId;

    public ExperimentPropertyValueId() {
    }

    public ExperimentPropertyValueId(String experimentId, String propertyId) {
        this.experimentId = experimentId;
        this.propertyId = propertyId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public ExperimentPropertyValueId setExperimentId(String experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public ExperimentPropertyValueId setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }
}
