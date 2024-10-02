/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

/** 
 * Single signals entity (entities API endpoint) 
 */

@Entity
@Table(name="signalsentities")
public class SignalsEntity {

    @Id
    private String id;

    @Column(name = "snb_type")
    private String type;

    @Column
    private String eid;

    @Column
    private String name;

    @Column
    @Lob
    private String description;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column
    private String createdBy;

    @Column
    private String owner;

    @Column(name = "edited_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedAt;

    @Column
    private String editedBy;

    @Column(name = "digest")
    private Long digest_hash;

    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @ElementCollection
    @CollectionTable(name = "signalsentities_children", joinColumns = @JoinColumn(name = "signals_entity_id"))
    @Column(name = "child_id")
    private List<String> children;

    @ElementCollection
    @CollectionTable(name = "signalsentities_flags", joinColumns = @JoinColumn(name = "signals_entity_id"))
    @Column(name = "flag_value")
    private List<String> flags;

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SignalsEntity(%s) --> %s\n", id,  type));
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setId(String i) {
        id = i;
    }

    public void setType(String t) {
        type = t;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(String editedBy) {
        this.editedBy = editedBy;
    }

    public Long getDigest() {
        return digest_hash;
    }

    public void setDigest(Long digest_hash) {
        this.digest_hash = digest_hash;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
}
