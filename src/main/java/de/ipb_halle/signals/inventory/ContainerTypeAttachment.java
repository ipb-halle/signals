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
package de.ipb_halle.signals.inventory;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


/** 
 * Container type field definition 
 */

@Entity
@IdClass(ContainerTypeAttachmentId.class)
@Table(name="container_type_attachments")
public class ContainerTypeAttachment {

    @Id
    private String container_type_id;

    @Id
    private String attachment_id;


    public ContainerTypeAttachment() {
    }

    public ContainerTypeAttachment(String ct, String ai) {
        container_type_id = ct;
        attachment_id = ai;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        ContainerTypeAttachment other = (ContainerTypeAttachment) o;
        return Objects.equals(container_type_id, other.container_type_id)
            && Objects.equals(attachment_id, other.attachment_id);
    }

    public String getContainerTypeId() {
        return container_type_id;
    }

    public String getAttachmentId() {
        return attachment_id;
    }

    @Override
    public int hashCode() {
        return getContainerTypeId().hashCode() + getAttachmentId().hashCode();
    }

    public ContainerTypeAttachment setContainerTypeId(String ct) {
        container_type_id = ct;
        return this;
    }

    public ContainerTypeAttachment setAttachmentId(String a) {
        attachment_id = a;
        return this;
    }
}
