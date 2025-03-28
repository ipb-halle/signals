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

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="experiments")
public class ExperimentEntity {
    public static final String ENTITY_TYPE_EXPERIMENT = "experiment";
    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Integer type;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "edited_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "edited_by")
    private String editedBy;

    @Column
    private String owner;

    @Column(name = "digest", nullable = false, columnDefinition = "BIGINT")
    private Long digest;

    @Column(name = "ancestor_id")
    private String ancestorId;

    @Column(name="template_id")
    private String templateId;

    public String getId() {
        return id;
    }

    public ExperimentEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ExperimentEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExperimentEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public ExperimentEntity setType(Integer type) {
        this.type = type;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public ExperimentEntity setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public ExperimentEntity setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ExperimentEntity setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public ExperimentEntity setEditedBy(String editedBy) {
        this.editedBy = editedBy;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public ExperimentEntity setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Long getDigest() {
        return digest;
    }

    public ExperimentEntity setDigest(Long digest) {
        this.digest = digest;
        return this;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public ExperimentEntity setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public ExperimentEntity setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }
}
