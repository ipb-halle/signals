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
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.inject.Inject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Manager for signals entities (entities API endpoint)
 */
@Local
public class SignalsEntityRestService implements RestReplyParser<SignalsEntityDTO> {

    public final static String SIGNALS_ENTITY_ENDPOINT = "/entities";
    public final static String PARAMETER_INCLUDE_TYPES = "includeTypes";
    public final static String PARAMETER_START = "start";
    public final static String PARAMETER_END = "end";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    public SignalsEntityDTO parseReply(JsonElement json) {
        SignalsEntityDTO dto = new SignalsEntityDTO();
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject attributes = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        dto.setId(json.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(attributes.getAsJsonPrimitive(RestHelper.ATTR_TYPE).getAsString())));
        dto.setEid(attributes.has(SignalsEntityDTO.ATTR_EID)?attributes.get(SignalsEntityDTO.ATTR_EID).getAsString():null);
        dto.setName(attributes.has(RestHelper.ATTR_NAME) ? attributes.get(RestHelper.ATTR_NAME).getAsString() : null);
        dto.setDescription(attributes.has(SignalsEntityDTO.ATTR_DESCRIPTION) ? attributes.get(SignalsEntityDTO.ATTR_DESCRIPTION).getAsString() : null);
        dto.setCreatedAt(attributes.has(SignalsEntityDTO.ATTR_CREATED_AT) ? Date.from(Instant.parse(attributes.get(SignalsEntityDTO.ATTR_CREATED_AT).getAsString())) : null);
        dto.setEditedAt(attributes.has(SignalsEntityDTO.ATTR_EDITED_AT) ? Date.from(Instant.parse(attributes.get(SignalsEntityDTO.ATTR_EDITED_AT).getAsString())) : null);
        dto.setDigest(attributes.has(RestHelper.ATTR_DIGEST) ? Long.parseLong(attributes.get(RestHelper.ATTR_DIGEST).getAsString()) : null);
        
        parseRelationships(jsonObj, dto);
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

    public RestResultIterator<SignalsEntityDTO> doGetEntities(Map<String, Object> cmap) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        restClient.reset()
                .setMethod(Method.GET)
                .setEndpoint(SIGNALS_ENTITY_ENDPOINT);

        configureEntityTypes(cmap);
        if (cmap.containsKey(PARAMETER_START)) {
            restClient.putUriParameter(PARAMETER_START,
                    dateFormat.format((Date) cmap.get(PARAMETER_START)));
        }
        if (cmap.containsKey(PARAMETER_END)) {
            restClient.putUriParameter(PARAMETER_END,
                    dateFormat.format((Date) cmap.get(PARAMETER_END)));
        }

        return new RestResultIterator<SignalsEntityDTO> (restClient, this, true);
    }

    private void configureEntityTypes(Map<String, Object> cmap) {
        if (cmap.containsKey(PARAMETER_INCLUDE_TYPES)) {
            StringBuilder sb = new StringBuilder();
            AtomicReference<String> sep = new AtomicReference<>("");
            for (EntityType et : (EntityType[]) cmap.get(PARAMETER_INCLUDE_TYPES)) {
                sb.append(sep.getAndSet(","));
                sb.append(et.getValue());
            }
            restClient.putUriParameter(PARAMETER_INCLUDE_TYPES, sb.toString());
        }
    }
}
