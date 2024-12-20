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

package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.users.IUser;

import java.util.Date;

/*
 * The interface IObjectMetaData bundles the createdAt,
 * createdBy, editedAt, editedBy and ownership informations
 * of an entity.
 */
public interface IObjectMetaData {
    Date getCreatedAt();

    void setCreatedAt(Date createdAt);

    IUser getCreatedBy();

    void setCreatedBy(IUser createdBy);

    IUser getOwner();

    void setOwner(IUser owner);

    Date getEditedAt();

    void setEditedAt(Date editedAt);

    IUser getEditedBy();

    void setEditedBy(IUser editedBy);
}
