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

import java.util.List;

@Entity
@Table(name = "sample_templates")
public class SampleTemplateEntity {

    @Id
    @Column(name = "template_id")
    private String templateId;

    @Column(name = "template_name")
    private String templateName;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<SampleEntity> samples;

    public SampleTemplateEntity() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public SampleTemplateEntity setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public SampleTemplateEntity setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }
}
