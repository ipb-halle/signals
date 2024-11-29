/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE))));
        dto.setEid(RestHelper.parseString(attributes, SignalsEntityDTO.ATTR_EID));
        dto.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        dto.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        dto.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));

        parseTimestamps(attributes, dto);
        parseRelationships(jsonObj, dto);
        return dto;
    }

    public void parseTimestamps(JsonObject attributes, EntityRelationships entityRel) {
        entityRel.setCreatedAt(RestHelper.parseDate(attributes, SignalsEntityDTO.ATTR_CREATED_AT));
        entityRel.setEditedAt(RestHelper.parseDate(attributes, SignalsEntityDTO.ATTR_EDITED_AT));
    }

    public void parseRelationships(JsonObject relationships, EntityRelationships entityRel) {
        entityRel.setCreatedBy(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        entityRel.setEditedBy(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        entityRel.setOwner(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
        entityRel.addAllAncestors(parseAncestors(relationships));
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

    private List<ISignalsEntity> parseAncestors(JsonObject json) {
        return new ArrayList<ISignalsEntity>();
    }
}
