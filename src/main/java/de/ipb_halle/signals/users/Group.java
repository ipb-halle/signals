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
package de.ipb_halle.signals.users;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * SNB group
 */

@Entity
@Table(name="groups")
public class Group implements IGroup {

    public final static String GROUP_NAME = "name";

    public final static String ATTR_ASSOCIATE_TYPE = "associateType";
    public final static String ATTR_ASSOCIATE_TYPE_ADD = "ADD";
    public final static String ATTR_CREATED_AT= "createdAt";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_DIGEST = "digest";
    public final static String ATTR_EDITED_AT= "editedAt";
    public final static String ATTR_EID = "eid";
    public final static String ATTR_FLAGS = "flags";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_SYSTEM = "isSystem";
    public final static String ATTR_TYPE = "type";

    public final static String SNB_GROUP_TYPE = "group";

    @Id
    private String id;

    @Column(name="created_at")
    private Date createdAt;

    @Column
    private boolean deleted;

    @Column
    private String description;

    @Column
    private String digest;

    @Column(name="edited_at")
    private Date editedAt;

    @Column(name="ldap_group")
    private boolean ldapGroup;

    @Column
    private String name;

    @Column(name="is_system")
    private boolean system;

    @Column(name="snb_type")
    private String type;

    @Column(name="json_string")
    private String jsonString;

    /**
     * default constructor
     */
    public Group() {
        deleted = false;
        type = SNB_GROUP_TYPE;
    }

    public void applyChangesFromSnb(Group snb) {
        id = snb.getId();
        description = snb.getDescription();
        name = snb.getName();
        type = snb.getType();
        editedAt = snb.getEditedAt();
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        if (deleted) {
            sb.append("Deleted ");
        }
        sb.append(String.format("Group(%d) --> %s\n", id, name));
        sb.append(String.format("Description: %s\n", description));
        sb.append((jsonString != null) ? jsonString : "");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof IGroup) {
            IGroup igroup = (IGroup) obj;
            return Objects.equals(id,igroup.getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isLdapGroup() {
        return ldapGroup;
    }

    public boolean isModified(CompareType context, Group group) {
        return !(name.equals(group.getName())
            && description.equals(group.getDescription())
            && type.equals(group.getType())
            && ((context == CompareType.SNB) ? editedAt.equals(group.getEditedAt()) : true));
    }

    public boolean isSystem() {
        return system;
    }

    public IGroup setId(String st) {
        id = st;
        return this;
    }

    public void setCreatedAt(Date d) {
        createdAt = d;
    }

    public void setDeleted(boolean b) {
        deleted = b;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setEditedAt(Date d) {
        editedAt = d;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }

    public void setLdapGroup(boolean b) {
        ldapGroup = b;
    }

    public void setName(String n) {
        name = n;
    }

    public void setSystem(boolean b) {
        system = b;
    }

    public void setType(String t) {
        type = t;
    }
}
