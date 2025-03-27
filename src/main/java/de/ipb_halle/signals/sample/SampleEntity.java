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

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.users.UserReference;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "samples")
public class SampleEntity {

    public static final String ENTITY_TYPE_SAMPLE = "sample";
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

    @Column(name = "stoicRef_id")
    private String stoicRefId;

    @Column(name = "stoicRef_row_id")
    private String stoicRefRowId;

    @Column(name = "parent_container_id")
    private String parentContainerId;

    @ManyToOne
    @JoinColumn(name = "template_id", referencedColumnName = "template_id")
    private SampleTemplateEntity template;
    public String getId() {
        return id;
    }

    public SampleEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SampleEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SampleEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public SampleEntity setType(Integer type) {
        this.type = type;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public SampleEntity setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public SampleEntity setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public SampleEntity setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public SampleEntity setEditedBy(String editedBy) {
        this.editedBy = editedBy;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public SampleEntity setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Long getDigest() {
        return digest;
    }

    public SampleEntity setDigest(Long digest) {
        this.digest = digest;
        return this;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public SampleEntity setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public String getStoicRefId() {
        return stoicRefId;
    }

    public SampleEntity setStoicRefId(String stoicRefId) {
        this.stoicRefId = stoicRefId;
        return this;
    }

    public String getStoicRefRowId() {
        return stoicRefRowId;
    }

    public SampleEntity setStoicRefRowId(String stoicRefRowId) {
        this.stoicRefRowId = stoicRefRowId;
        return this;
    }

    public String getParentContainerId() {
        return parentContainerId;
    }

    public SampleEntity setParentContainerId(String parentContainerId) {
        this.parentContainerId = parentContainerId;
        return this;
    }

    public SampleTemplateEntity getTemplate() {
        return template;
    }

    public SampleEntity setTemplate(SampleTemplateEntity template) {
        this.template = template;
        return this;
    }

    public String getTemplateId() {
        return template != null ? template.getTemplateId() : null;
    }
}
