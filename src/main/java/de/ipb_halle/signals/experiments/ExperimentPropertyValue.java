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

import de.ipb_halle.signals.sample.SamplePropertyValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExperimentPropertyValue {

    private String experimentId;
    private String propertyId;
    private String propertyValue;
    public static final Logger logger = LogManager.getLogger(SamplePropertyValue.class);

    public ExperimentPropertyValue() {
    }

    public ExperimentPropertyValue(ExperimentPropertyValueEntity entity) {
        this.experimentId = entity.getId().getExperimentId();
        this.propertyId=entity.getId().getPropertyId();
        this.propertyValue = entity.getPropertyValue();

        if (entity.getId().getPropertyId() == null) {
            logger.warn("SamplePropertyValueEntity has null propertyId: sampleId = {}", entity.getId().getExperimentId());
        }
    }

    public ExperimentPropertyValueEntity createEntity() {
        if (experimentId == null || propertyId == null) {
            logger.warn("Skipping SamplePropertyValueEntity creation: sampleId={}, propertyId={}", experimentId, propertyId);
            return null;
        }
        ExperimentPropertyValueId experimentPropertyValueId = new ExperimentPropertyValueId();
        experimentPropertyValueId
                .setExperimentId(experimentId)
                .setPropertyId(propertyId);

        return new ExperimentPropertyValueEntity()
                .setId(experimentPropertyValueId)
                .setPropertyValue(propertyValue);
    }


    public String getExperimentId() {
        return experimentId;
    }

    public ExperimentPropertyValue setExperimentId(String experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public ExperimentPropertyValue setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public ExperimentPropertyValue setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }
}