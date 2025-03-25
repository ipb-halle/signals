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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SampleProperty {
    private Long id;
    private String type;
    private String name;
    private String value;
    private String key;
    private String sampleId;

    public SampleProperty() {
    }

    public SampleProperty(SamplePropertyEntity spe) {
        this.id = spe.getId();
        this.type = spe.getType();
        this.name = spe.getName();
        this.value = spe.getValue();
        this.key = spe.getKey();
        this.sampleId = spe.getSampleId();

    }

    public SamplePropertyEntity createEntity() {
        return new SamplePropertyEntity()
                .setId(id)
                .setName(name)
                .setType(type)
                .setValue(value)
                .setKey(key)
                .setSampleId(sampleId);
    }

    //getter
    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getSampleId() {
        return sampleId;
    }

    //setter

    public SampleProperty setId(Long id) {
        this.id = id;
        return this;
    }

    public SampleProperty setType(String type) {
        this.type = type;
        return this;
    }

    public SampleProperty setName(String name) {
        this.name = name;
        return this;
    }

    public SampleProperty setValue(String value) {
        this.value = value;
        return this;
    }

    public SampleProperty setKey(String key) {
        this.key = key;
        return this;
    }

    public SampleProperty setSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

}
