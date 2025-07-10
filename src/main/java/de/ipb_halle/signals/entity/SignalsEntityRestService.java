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
import com.google.gson.JsonParser;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Manager for signals entities (entities API endpoint)
 */
@Local
public class SignalsEntityRestService implements RestReplyParser<SignalsEntityDTO> {

    private Logger logger = LoggerFactory.getLogger(SignalsEntityRestService.class);

    public final static String SIGNALS_ENTITY_ENDPOINT = "/entities";
    public final static String SIGNALS_ENTITY_CHILDREN_ENDPOINT = "/entities/%s/children";
    public final static String SIGNALS_ENTITY_SHARES_ENDPOINT = "/entities/%s/shares";
    public final static String PARAMETER_INCLUDE_TYPES = "includeTypes";
    public final static String PARAMETER_INCLUDE_OPTIONS = "includeOptions";
    public final static String PARAMETER_START = "start";
    public final static String PARAMETER_END = "end";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    public SignalsEntityDTO parseReply(JsonElement json) {
        SignalsEntityDTO dto = new SignalsEntityDTO();
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject jsonAttributes = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        dto.setId(json.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(RestHelper.parseString(jsonAttributes, RestHelper.ATTR_TYPE))));
        dto.setEid(RestHelper.parseString(jsonAttributes, SignalsEntityDTO.ATTR_EID));
        dto.setName(RestHelper.parseString(jsonAttributes, RestHelper.ATTR_NAME));
        dto.setDescription(RestHelper.parseString(jsonAttributes, RestHelper.ATTR_DESCRIPTION));
        dto.setDigest(RestHelper.parseLong(jsonAttributes, RestHelper.ATTR_DIGEST));

        parseTimestamps(jsonAttributes, dto);
        parseRelationships(jsonObj, dto);
        return dto;
    }

    public static void parseTimestamps(JsonObject attributes, IObjectMetaData entity) {
        entity.setCreatedAt(RestHelper.parseDate(attributes, SignalsEntityDTO.ATTR_CREATED_AT));
        entity.setEditedAt(RestHelper.parseDate(attributes, SignalsEntityDTO.ATTR_EDITED_AT));
    }

    public static void parseRelationships(JsonObject relationships, IObjectMetaData entity) {
        entity.setCreatedBy(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        entity.setEditedBy(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        entity.setOwner(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
    }

    public RestResultIterator<SignalsEntityDTO> doGetEntities(Map<String, Object> cmap) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        restClient.reset()
                .setMethod(Method.GET)
                .setEndpoint(SIGNALS_ENTITY_ENDPOINT);

        configureEntityTypes(cmap);
        configureEntityOptions(cmap);
        if (cmap.containsKey(PARAMETER_START)) {
            restClient.putUriParameter(PARAMETER_START,
                    dateFormat.format((Date) cmap.get(PARAMETER_START)));
        }
        if (cmap.containsKey(PARAMETER_END)) {
            restClient.putUriParameter(PARAMETER_END,
                    dateFormat.format((Date) cmap.get(PARAMETER_END)));
        }

        return new RestResultIterator<SignalsEntityDTO>(restClient, this, true);
    }

    /**
     * children can be displayed through REST-endpoint entity/eid/children
     * fields are: eid, name, description, createdAt, editedAt, type, state, digest, fields
     */
    public RestResultIterator<SignalsEntityDTO> doGetChildren(SignalsEntityDTO parentEntity) {
        restClient.reset()
                .setMethod(Method.GET)
                .putUriParameter("include", "createdBy,editedBy,owner")
                .setEndpoint(String.format(SIGNALS_ENTITY_CHILDREN_ENDPOINT,
                        parentEntity.getStrippedId(SignalsEntityDTO.StripIdPart.SUFFIX)));
        return new RestResultIterator<SignalsEntityDTO>(restClient,
                new SignalsChildParser(dynEnumManager), true);
    }

    /**
     * @param entityDTO
     * @return
     */
    public List<Share> doGetShares(SignalsEntityDTO entityDTO) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setEndpoint(String.format(SIGNALS_ENTITY_SHARES_ENDPOINT,
                            entityDTO.getStrippedId(SignalsEntityDTO.StripIdPart.SUFFIX)))
                    .execute();

            ShareParser parser = new ShareParser();
            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            List<Share> shares = parser.parseReply(jsonResult);
            for (Share share : shares) {
                share.setEntityId(entityDTO.getId());
            }
            return shares;
        } catch (UnexpectedResponseCodeException ue) {
            logger.warn("doCreateGroup() got unexpected return code from API call");
        } catch (URISyntaxException me) {
            logger.warn("doCreateGroup() malformed URL");
        } catch (IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
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

    private void configureEntityOptions(Map<String, Object> cmap) {
        if (cmap.containsKey(PARAMETER_INCLUDE_OPTIONS)) {
            restClient.putUriParameter(PARAMETER_INCLUDE_OPTIONS,
                    (String) cmap.get(PARAMETER_INCLUDE_OPTIONS));
        }
    }

    public SignalsEntityDTO createIpbCodeCustomObjectSignalsEntity(String ancestorId, String name, String templateId) {
        try {
            JsonObject payload = new JsonObject();

            // data
            JsonObject data = new JsonObject();
            data.addProperty("type", "ado");

            // meta (adoTypeName)
            JsonObject meta = new JsonObject();
            meta.addProperty("adoTypeName", name);
            data.add("meta", meta);

            // attributes
            JsonObject attributes = new JsonObject();
            attributes.addProperty("name", name);
            data.add("attributes", attributes);

            // relationships
            JsonObject relationships = new JsonObject();

            // template
            JsonObject template = new JsonObject();
            JsonObject templateData = new JsonObject();
            templateData.addProperty("type", "ado");
            templateData.addProperty("id", templateId);
            template.add("data", templateData);
            relationships.add("template", template);

            data.add("relationships", relationships);

            payload.add("data", data);

            // send a request
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(SignalsEntityRestService.SIGNALS_ENTITY_ENDPOINT)
                    .setRequestData(payload.toString())
                    .execute();

            JsonElement jsonResponse = JsonParser.parseString(restClient.getResponse().getString());
            return parseReply(jsonResponse.getAsJsonObject().get("data"));

        } catch (IOException | URISyntaxException | UnexpectedResponseCodeException ex) {
            logger.error("Failed to create custom object for ipbCode {}", name, ex);
            return null;
        }
    }
}
