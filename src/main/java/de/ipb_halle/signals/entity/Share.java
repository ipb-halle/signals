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
public interface Share {

    public final static String ENTITY_ID = "entityId";
    public final static String USER_ID = "userId";
    public final static String GROUP_ID = "groupId";

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

    public String getEntityId();

    public void setEntityId(String entityId);

    public boolean canRead();

    public void setCanRead(boolean canRead);

    public boolean canWrite();

    public void setCanWrite(boolean canWrite);

    public boolean isAdmin();

    public void setIsAdmin(boolean admin);

    public boolean hasFullControl();

    public void setHasFullControl(boolean hasFullControl);

    public  ShareType getType();

    default boolean hasPermission(SharePermission permission) {
        switch(permission) {
            case READ: return canRead();
            case WRITE: return canWrite();
            case ADMIN: return isAdmin();
            case FULL_CONTROL: return hasFullControl();
        }
        return false;
    }
}
