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

import de.ipb_halle.signals.entity.Attachment;
import de.ipb_halle.signals.entity.FieldDefinition;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/** 
 * Container Type DTO
 */

public class ContainerType {

    public final static String ATTR_ATTACHMENTS = "attachments";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_FIELDS = "fields";
    public final static String ATTR_IN_USE = "inUse";
    public final static String ATTR_MOVABLE = "isMovable";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "entityType";
    public final static String ATTR_UPDATED_AT = "updatedAt";

    private String id;

    private Set<Attachment> attachments;

    private Date createdAt;

    private String description;

    private Set<FieldDefinition> fieldDefinitions;

    private boolean inUse;

    private boolean movable;
    
    private String name;

    private String jsonString;

    private Date updatedAt;

    /**
     * default constructor
     */
    public ContainerType() {
        createdAt = new Date();
        updatedAt = new Date();
        attachments = new HashSet<> ();
        fieldDefinitions = new HashSet<> ();
    }

    public  ContainerType(ContainerTypeEntity cte, List<Attachment> a, List<FieldDefinition> fd) {
        id = cte.getId();
        createdAt = cte.getCreatedAt();
        description = cte.getDescription();
        inUse = cte.isInUse();
        jsonString = cte.getJsonString();
        movable = cte.isMovable();
        name = cte.getName();
        updatedAt = cte.getUpdatedAt();

        attachments = new HashSet<> ();
        fieldDefinitions = new HashSet<> ();
        attachments.addAll(a);
        fieldDefinitions.addAll(fd);
    }

    public ContainerTypeEntity createEntity() {
        ContainerTypeEntity cte = new ContainerTypeEntity()
            .setId(id)
            .setCreatedAt(createdAt)
            .setDescription(description)
            .setInUse(inUse)
            .setJsonString(jsonString)
            .setMovable(movable)
            .setName(name)
            .setUpdatedAt(updatedAt);
        return cte;
    }

    public String dump() {
        return String.format("ContainerType(%s): %s\n", id, name);
    }

    public String getId() {
        return id;
    }

    public void addAttachment(Attachment a) {
        attachments.add(a);
    }

    public void addFieldDefinition(FieldDefinition f) {
        fieldDefinitions.add(f);
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public Set<FieldDefinition> getFieldDefinitions() {
        return fieldDefinitions;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getName() {
        return name;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public boolean isInUse() {
        return inUse;
    }

    public boolean isMovable() {
        return movable;
    }

    public void removeAttachment(Attachment a) {
        attachments.remove(a);
    }

    public void removeFieldDefinition(FieldDefinition f) {
        fieldDefinitions.remove(f);       
    }

    public void setId(String i) {
        id = i;
    }

    public void setAttachments(Set<Attachment> a) {
        attachments = a;
    }

    public void setCreatedAt(Date d) {
        createdAt = d;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setFieldDefinitions(Set<FieldDefinition> fd) {
        fieldDefinitions = fd;
    }

    public void setMovable(boolean m) {
        movable = m;
    }

    public void setInUse(boolean u) {
        inUse = u;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }

    public void setName(String n) {
        name = n;
    }

    public void setUpdatedAt(Date u) {
        updatedAt = u;
    }
}
