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

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestReplyParser;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.inject.Inject;


/**
 * Manager for signals entities (entities API endpoint)
 */
@Local
public class SignalsEntityRestService implements RestReplyParser<SignalsEntityDTO> {

    public final String SIGNALS_ENTITY_ENDPOINT = "/entities";
    public final String PARAMETER_INCLUDE_TYPES = "includeTypes";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    public SignalsEntityDTO parseReply(JsonElement json) {
        SignalsEntityDTO dto = new SignalsEntityDTO();
        JsonObject attributes = json.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        JsonObject relationships = json.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);

        dto.setId(json.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(attributes.getAsJsonPrimitive(RestHelper.ATTR_TYPE).getAsString())));

        System.out.printf("type: %s   \tid: %s \n", dto.getType(), dto.getId());

        dto.setEid(attributes.has(SignalsEntityDTO.ATTR_EID)?attributes.get(SignalsEntityDTO.ATTR_EID).getAsString():null);
        dto.setName(attributes.has(RestHelper.ATTR_NAME) ? attributes.get(RestHelper.ATTR_NAME).getAsString() : null);
        dto.setDescription(attributes.has(SignalsEntityDTO.ATTR_DESCRIPTION) ? attributes.get(SignalsEntityDTO.ATTR_DESCRIPTION).getAsString() : null);
        dto.setCreatedAt(attributes.has(SignalsEntityDTO.ATTR_CREATED_AT) ? Date.from(Instant.parse(attributes.get(SignalsEntityDTO.ATTR_CREATED_AT).getAsString())) : null);
        dto.setEditedAt(attributes.has(SignalsEntityDTO.ATTR_EDITED_AT) ? Date.from(Instant.parse(attributes.get(SignalsEntityDTO.ATTR_EDITED_AT).getAsString())) : null);
        dto.setDigest(attributes.has(RestHelper.ATTR_DIGEST) ? Long.parseLong(attributes.get(RestHelper.ATTR_DIGEST).getAsString()) : null);
        
        parseRelationships(relationships, dto);
        return dto;
    }

    private void parseRelationships(JsonObject relationships, SignalsEntityDTO dto) {
        dto.setCreatedBy(new UserReference().setId(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        dto.setEditedBy(new UserReference().setId(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        dto.setOwner(new UserReference().setId(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
    }

    public List<SignalsEntityDTO> doGetEntities(String includeTypes) {
        restClient.reset()
                .setMethod(Method.GET)
                .setEndpoint(SIGNALS_ENTITY_ENDPOINT)
                .putUriParameter(PARAMETER_INCLUDE_TYPES, includeTypes);

        RestResultIterator<SignalsEntityDTO> iter = new RestResultIterator<>(restClient, this, true);
        List<SignalsEntityDTO> entities = new ArrayList<>();

        while (iter.hasNext()) {
            entities.add(iter.next());
        }
        return entities;
    }
}
