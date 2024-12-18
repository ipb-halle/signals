/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.attachment;

import java.sql.Struct;
import java.util.Date;

import jakarta.persistence.*;

/**
 * Attachment entity
 */

@Entity
@Table(name = "attachments")
public class AttachmentEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "field_id")
    private String fieldId;

    @Column(name = "ancestor_id")
    private String ancestorId;

    public String dump() {
        return String.format("Attachment(%d): %s\n", id, entityId);
    }


    public Integer getId() {
        return id;
    }

    public AttachmentEntity setId(Integer i) {
        id = i;
        return this;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public AttachmentEntity setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public AttachmentEntity setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getFieldId() {
        return fieldId;
    }

    public AttachmentEntity setFieldId(String fieldId) {
        this.fieldId = fieldId;
        return this;
    }

    @Override
    public String toString() {
        return "AttachmentEntity{" +
                "id='" + id + '\'' +
                ", ancestorId='" + ancestorId + '\'' +
                '}';
    }
}
