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
package de.ipb_halle.signals.attachment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestService;


// import jakarta.ejb.Local;
// import jakarta.inject.Inject;

/** 
 * service for attachments (not yet a real REST service)
 */

//@Local
public class AttachmentRestService implements RestService<Attachment> {

    public final static String ATTR_ENTITY_ID = "entityId";
    public final static String ATTR_ATTACHMENT_ID = "attachmentId";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_ENTITY_TYPE = "entityType";
    public final static String ATTR_FILE_NAME = "fileName";
    public final static String ATTR_TEMPLATE = "isTemplate";
    public final static String ATTR_UPDATED_AT = "updatedAt";
    public final static String ATTR_VERSION_ID = "versionId";


    public Attachment createEntity(JsonElement j) {
        Attachment a = new Attachment();
        JsonObject def = j.getAsJsonObject();
        a.setId(RestHelper.parseString(def, Attachment.ATTR_ATTACHMENT_ID));
        a.setEntityId(RestHelper.parseString(def, Attachment.ATTR_ENTITY_ID));
        a.setCreatedAt(RestHelper.parseDate(def, Attachment.ATTR_CREATED_AT));
        a.setEntityType(RestHelper.parseString(def, Attachment.ATTR_ENTITY_TYPE));
        a.setFileName(RestHelper.parseString(def, Attachment.ATTR_FILE_NAME));
        a.setTemplate(RestHelper.parseBool(def, Attachment.ATTR_TEMPLATE));
        a.setUpdatedAt(RestHelper.parseDate(def, Attachment.ATTR_UPDATED_AT));
        a.setVersionId(RestHelper.parseString(def, Attachment.ATTR_VERSION_ID));

        return a;
    }
}
