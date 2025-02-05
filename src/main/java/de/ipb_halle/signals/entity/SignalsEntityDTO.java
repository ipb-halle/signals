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
package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;

import java.util.*;

public class SignalsEntityDTO implements IEntityRelationships, ISignalsEntity {

    public enum StripIdPart {
        SUFFIX,
        BOTH
    }
    public final static String ATTR_EID = "eid";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_EDITED_AT = "editedAt";
    public final static String ATTR_CREATED_BY = "relationships.createdBy.data.id";
    public final static String ATTR_EDITED_BY = "relationships.editedBy.data.id";
    public final static String ATTR_OWNER = "relationships.owner.data.id";
    public final static String ATTR_ANCESTORS = "relationships.ancestors.data";
    public final static String ATTR_CHILDREN = "relationships.children.data";
    public final static String ATTR_FLAGS = "flags";

    private String id;
    private EntityType type;
    private String eid;
    private String name;
    private String description;
    private Date createdAt;
    private IUser createdBy;
    private IUser owner;
    private Date editedAt;
    private IUser editedBy;
    private Long digest;
    private Date timeStamp;
    private Set<ISignalsEntity> children;

    /**
     * default constructor
     */
    public SignalsEntityDTO() {
        children = new HashSet<>();
    }

    public SignalsEntityDTO(SignalsEntity entity, DynEnumManager dynEnumManager) {
        id = entity.getId();
        type = (EntityType) dynEnumManager.valueOf(entity.getType());
        this.eid = entity.getEid();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.createdAt = entity.getCreatedAt();
        this.createdBy = new UserReference(entity.getCreatedBy());
        this.owner = new UserReference(entity.getOwner());
        ;
        this.editedAt = entity.getEditedAt();
        this.editedBy = new UserReference(entity.getEditedBy());
        this.digest = entity.getDigest();
        this.timeStamp = entity.getTimeStamp();
        /* complex types */
        this.children = new HashSet<>();
    }

    public SignalsEntity createEntity() {
        SignalsEntity entity = new SignalsEntity();
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy.getId());
        entity.setDescription(description);
        entity.setDigest(digest);
        entity.setEditedAt(editedAt);
        entity.setEditedBy(editedBy.getId());
        entity.setEid(eid);
        entity.setId(id);
        entity.setName(name);
        entity.setOwner(owner.getId());
        entity.setTimeStamp(timeStamp);
        entity.setType(type.getId());
        return entity;
    }

    @Override
    public void addAllChildren(Collection<ISignalsEntity> children) {
        //this.children.addAll(children);
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("  \tname=");
        sb.append(getName());
        sb.append("  \tvalue=");
        sb.append(type.getValue());
        sb.append("  \tid=");
        sb.append(id);
        sb.append("  \towner=");
        sb.append(owner.getId());
        sb.append("  \tedited=");
        sb.append(editedAt.toString());
        return sb.toString();
    }

    public void addChild(ISignalsEntity entity) {
        this.children.add(entity);
    }

    public void addChildren(Collection<ISignalsEntity> children) {
        this.children.addAll(children);
    }

    public String getId() {
        return id;
    }

    /**
     * @return splits the id into prefix, id (and suffix) and return the id part
     */
    public String getStrippedId(StripIdPart part) {
        String[] parts = id.split(":");
        switch(part) {
            case BOTH:
                return parts.length > 1 ? parts[1] : parts[0];
            case SUFFIX:
                return parts.length > 2
                        ? parts[0] + ":" + parts[1]
                        : id;
        }
        throw new IllegalArgumentException("getStrippedId()");
    }

    public EntityType getType() {
        return type;
    }

    public void setId(String i) {
        id = i;
    }

    public void setType(EntityType t) {
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

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public IUser getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(IUser createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public IUser getOwner() {
        return owner;
    }

    @Override
    public void setOwner(IUser owner) {
        this.owner = owner;
    }

    @Override
    public Date getEditedAt() {
        return editedAt;
    }

    @Override
    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    @Override
    public IUser getEditedBy() {
        return editedBy;
    }

    @Override
    public void setEditedBy(IUser editedBy) {
        this.editedBy = editedBy;
    }

    public Long getDigest() {
        return digest;
    }

    public void setDigest(Long digest) {
        this.digest = digest;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public Set<ISignalsEntity> getChildren() {
        return children;
    }

    public void setChildren(Collection<ISignalsEntity> children) {
        this.children = new HashSet<> (children);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SignalsEntityDTO entityDTO = (SignalsEntityDTO) object;
        return Objects.equals(id, entityDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}


