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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.field.FieldDefinitionRestService;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestReplyParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.ejb.Local;
import jakarta.inject.Inject;


/** 
 * Rest service for container types 
 */

@Local
public class ContainerTypeRestService implements RestReplyParser<ContainerType> {

    public final String CONTAINER_TYPE_ENDPOINT = "/inventory/types";

    @Inject
    private RestClient restClient;

    @Inject
    private FieldDefinitionRestService fieldDefinitionParser;

    public ContainerType parseReply(JsonElement j) {
        ContainerType ct = new ContainerType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        ct.setId(j.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        ct.setDescription(attributes.getAsJsonPrimitive(ContainerType.ATTR_DESCRIPTION).getAsString());
        ct.setName(attributes.getAsJsonPrimitive(ContainerType.ATTR_NAME).getAsString());
        if (attributes.has(ContainerType.ATTR_ATTACHMENTS)) {
            parseAttachments(attributes.getAsJsonArray(ContainerType.ATTR_ATTACHMENTS), ct);
        }
        if (attributes.has(ContainerType.ATTR_FIELDS)) {
            parseFieldDefinitions(attributes.getAsJsonArray(ContainerType.ATTR_FIELDS), ct);
        }

        return ct;
    }

    public List<ContainerType> doGetContainerTypes() {
        List<ContainerType> containerTypes = new ArrayList<> ();
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(CONTAINER_TYPE_ENDPOINT)
            .putUriParameter("entityType","container");

        RestResultIterator<ContainerType> iter = new RestResultIterator<> (restClient, this, true);

        while(iter.hasNext()) {
            containerTypes.add(iter.next());
        }
        return containerTypes;
    }

    private void parseAttachments(JsonArray j, ContainerType ct) {
        Iterator<JsonElement> iter = j.iterator();
        AttachmentRestService svc = new AttachmentRestService();
        while (iter.hasNext()) {
            ct.addAttachment(svc.parseReply(iter.next()));
        }
    }

    private void parseFieldDefinitions(JsonArray j, ContainerType ct) {
        Iterator<JsonElement> iter = j.iterator();
        while (iter.hasNext()) {
            ct.addFieldDefinition(fieldDefinitionParser.parseReply(iter.next()));
        }
    }
}


