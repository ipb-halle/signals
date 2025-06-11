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

public class ExperimentProperty {
    public static final String TEMPLATE_ID = "templateId";
    private String propertyId;
    private String propertyName;
    private String propertyType;
    private String templateId;

    public ExperimentProperty() {
    }

    public ExperimentProperty(ExperimentPropertyEntity experimentPropertyEntity) {
        this.propertyId = experimentPropertyEntity.getPropertyId();
        this.propertyName = experimentPropertyEntity.getPropertyName();
        this.propertyType = experimentPropertyEntity.getPropertyType();
        this.templateId = experimentPropertyEntity.getTemplateId();
    }

    public ExperimentPropertyEntity createEntity() {
        return new ExperimentPropertyEntity()
                .setPropertyId(propertyId)
                .setPropertyName(propertyName)
                .setPropertyType(propertyType)
                .setTemplateId(templateId);
    }

    public String getPropertyId() {
        return propertyId;
    }

    public ExperimentProperty setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public ExperimentProperty setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public ExperimentProperty setPropertyType(String propertyType) {
        this.propertyType = propertyType;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public ExperimentProperty setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }
}
