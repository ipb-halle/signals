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

package de.ipb_halle.signals.experiments.properties;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExperimentPropertyValueId implements Serializable {

    @Column(name = "experiment_id")
    private String experimentId;

    @Column(name = "property_id")
    private String propertyId;

    public ExperimentPropertyValueId() {
    }

    public ExperimentPropertyValueId(String experimentId, String propertyId) {
        this.experimentId = experimentId;
        this.propertyId = propertyId;
    }

    // ——— getters ——— //

    public String getExperimentId() {
        return experimentId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    // ——— Setters ——— //

    public ExperimentPropertyValueId setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    public ExperimentPropertyValueId setExperimentId(String experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ExperimentPropertyValueId that = (ExperimentPropertyValueId) object;
        return Objects.equals(experimentId, that.experimentId) && Objects.equals(propertyId, that.propertyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experimentId, propertyId);
    }
}
