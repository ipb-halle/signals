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
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiment_properties")
public class ExperimentPropertyEntity {

    @Id
    @Column(name = "property_id")
    private String propertyId;

    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "property_type")
    private String propertyType;

  @Column(name = "template_id")
    private String templateId;

    public String getPropertyId() {
        return propertyId;
    }

    public ExperimentPropertyEntity setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public ExperimentPropertyEntity setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public ExperimentPropertyEntity setPropertyType(String propertyType) {
        this.propertyType = propertyType;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public ExperimentPropertyEntity setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    @Override
    public String toString() {
        return "ExperimentPropertyEntity{" +
                "propertyId='" + propertyId + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", templateId='" + templateId + '\'' +
                '}';
    }
}
