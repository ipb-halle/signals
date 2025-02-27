/*
 *
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Single signals entity (entities API endpoint)
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Share {

    public final static String ENTITY_ID = "entity_id";
    public final static String USER_ID = "user_id";
    public final static String GROUP_ID = "group_id";

    public final static String ATTR_CAN_READ = "canRead";
    public final static String ATTR_CAN_WRITE = "canWrite";
    public final static String ATTR_IS_ADMIN = "isAdmin";
    public final static String ATTR_HAS_FULL_CONTROL = "hasFullControl";
    public final static String ATTR_GROUP = "group";
    public final static String ATTR_GROUP_ID = "group.data.id";
    public final static String ATTR_USER = "user";
    public final static String ATTR_USER_ID = "user.data.id";

    public enum ShareType {
        EFFECTIVE,
        GROUP,
        USER;
    }

    public enum SharePermission {
        READ,
        WRITE,
        ADMIN,
        FULL_CONTROL
    }

    @EmbeddedId
    @AttributeOverrides(
        @AttributeOverride(name = "id", column = @Column(name = "entity_id"))
    )
    private EmbeddedKeyValue id;

    @Column(name="can_read")
    private boolean canRead;

    @Column(name="can_write")
    private boolean canWrite;

    @Column(name="is_admin")
    private boolean isAdmin;

    @Column(name="has_full_control")
    private boolean hasFullControl;

    public Share() {
        id = new EmbeddedKeyValue();
    }

    public EmbeddedKeyValue getId() {
        return id;
    }

    public void setId(EmbeddedKeyValue id) {
        this.id = id;
    }

    public String getEntityId() {
        return getId().getId();
    }

    public void setEntityId(String entityId) {
        getId().setId(entityId);
    }

    public boolean canRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean hasFullControl() {
        return hasFullControl;
    }

    public void setHasFullControl(boolean hasFullControl) {
        this.hasFullControl = hasFullControl;
    }

    public abstract ShareType getType();

    public boolean hasPermission(SharePermission permission) {
        switch(permission) {
            case READ: return canRead;
            case WRITE: return canWrite;
            case ADMIN: return isAdmin;
            case FULL_CONTROL: return hasFullControl;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Share share)) return false;
        return Objects.equals(id, share.id)
                && Objects.equals(getType(), share.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id) + Objects.hashCode(getType());
    }
}
