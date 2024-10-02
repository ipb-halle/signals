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
package de.ipb_halle.signals.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestReplyParser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.ejb.Local;
import jakarta.inject.Inject;


/** 
 * Manager for signals entities (entities API endpoint) 
 */

@Local
public class SignalsEntityRestService implements RestReplyParser<SignalsEntity> {

    public final String SIGNALS_ENTITY_ENDPOINT = "/entities";
    public final String PARAMETER_INCLUDE_TYPES = "includeTypes";

    @Inject
    private RestClient restClient;
    

    public SignalsEntity parseReply(JsonElement json) {
        SignalsEntity entity = new SignalsEntity();
        JsonObject attributes = json.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        entity.setId(json.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        entity.setType(attributes.getAsJsonPrimitive(RestHelper.ATTR_TYPE).getAsString());
        System.out.printf("type: %s   \tid: %s \n", entity.getType(), entity.getId());

        entity.setName(attributes.has("name") ? attributes.get("name").getAsString() : null);
        entity.setDescription(attributes.has("description") ? attributes.get("description").getAsString() : null);
        entity.setCreatedAt(attributes.has("createdAt") ? Date.from(Instant.parse(attributes.get("createdAt").getAsString())) : null);
        entity.setEditedAt(attributes.has("editedAt") ? Date.from(Instant.parse(attributes.get("editedAt").getAsString())) : null);
        entity.setDigest(attributes.has("digest") ? Long.parseLong(attributes.get("digest").getAsString()) : null);
        return entity;
    }

    public List<SignalsEntity> doGetEntities(String includeTypes) {
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(SIGNALS_ENTITY_ENDPOINT)
            .putUriParameter(PARAMETER_INCLUDE_TYPES, includeTypes);

        RestResultIterator<SignalsEntity> iter = new RestResultIterator<> (restClient, this, true); 
        List<SignalsEntity> entities = new ArrayList<> ();

        while(iter.hasNext()) {
            entities.add(iter.next());
        }
        return entities;
    }
}
