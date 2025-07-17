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

package de.ipb_halle.signals.ado;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "ados")
public class AdoEntity {

    public static final String ENTITY_TYPE_ADO = "ado";

    @Id
    private String id;
    @Column
    private String eid;
    @Column
    private String name;
    @Column
    private String description;
    @Column
    private Integer type;
    @Column(name = "ipb_code")
    private String ipbCode;
    @Column(name = "mol_id")
    private String molId;
    @Column(name ="proc_id")
    private Integer procId;
    @Column(name="sample_id")
    private String sampleId;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "created_by")
    private String  createdBy;
    @Column
    private String state;
    @Column(name = "ancestor_id")
    private String ancestorId;
    @Column(name="template_id")
    private String templateId;


    //Setter and Getter
    public String getId() {
        return id;
    }

    public AdoEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public AdoEntity setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public String getName() {
        return name;
    }

    public AdoEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdoEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public AdoEntity setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public AdoEntity setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    public String getMolId() {
        return molId;
    }

    public AdoEntity setMolId(String molId) {
        this.molId = molId;
        return this;
    }

    public Integer getProcId() {
        return procId;
    }

    public AdoEntity setProcId(Integer procId) {
        this.procId = procId;
        return this;
    }

    public String getSampleId() {
        return sampleId;
    }

    public AdoEntity setSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public AdoEntity setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public AdoEntity setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getState() {
        return state;
    }

    public AdoEntity setState(String state) {
        this.state = state;
        return this;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public AdoEntity setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public AdoEntity setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }
}
