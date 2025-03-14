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

@Entity
@Table(name="groupshares")
@IdClass(GroupShare.class)
public class GroupShare implements Share {
    @Id
    @Column(name="entity_id")
    private String entityId;

    @Id
    @Column(name="group_id")
    private String groupId;

    @Column(name="can_read")
    private boolean canRead;

    @Column(name="can_write")
    private boolean canWrite;

    @Column(name="is_admin")
    private boolean isAdmin;

    @Column(name="has_full_control")
    private boolean hasFullControl;


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean canRead() {
        return canRead;
    }

    @Override
    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    @Override
    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public boolean hasFullControl() {
        return hasFullControl;
    }

    @Override
    public void setHasFullControl(boolean hasFullControl) {
        this.hasFullControl = hasFullControl;
    }

    @Override
    public ShareType getType() {
        return ShareType.GROUP;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GroupShare that)) return false;
        return Objects.equals(entityId, that.entityId)
                && Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, groupId, getType());
    }
}
