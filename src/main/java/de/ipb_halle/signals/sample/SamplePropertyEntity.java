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
@Table(name = "sample_properties")
public class SamplePropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String type;

    @Column
    private String name;

    @Column(name = "property_value")
    private String value;

    @Column(name = "property_key")
    private String key;

    @Column(name = "sample_id")
    private String sampleId;


    public SamplePropertyEntity() {
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


    public SamplePropertyEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public SamplePropertyEntity setType(String type) {
        this.type = type;
        return this;
    }

    public SamplePropertyEntity setName(String name) {
        this.name = name;
        return this;
    }

    public SamplePropertyEntity setValue(String value) {
        this.value = value;
        return this;
    }

    public SamplePropertyEntity setKey(String key) {
        this.key = key;
        return this;
    }

    public SamplePropertyEntity setSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

}
