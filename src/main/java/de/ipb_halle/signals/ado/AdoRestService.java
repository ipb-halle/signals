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

package de.ipb_halle.signals.ado;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.ado.properties.AdoProperty;
import de.ipb_halle.signals.ado.properties.AdoPropertyValue;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@LocalBean
@Stateless
public class AdoRestService implements RestReplyParser<Ado> {

    @Inject
    RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private AdoDbService adoDbService;

    @Inject
    SignalsEntityRestService signalsEntityRestService;

    private final Logger logger = LogManager.getLogger(AdoRestService.class);

    private static final String RECEIVE_ADO_ENDPOINT = "/entities/%s";
    public static final String CREATE_NEW_ADO_ENDPOINT = "/entities?force=true";
    private static final String ADO_GET_PROPERTIES_ENDPOINT = "/entities/templates/%s/fields";
    private static final String ADO_GET_PROPERTY_VALUES_ENDPOINT = "/entities/%s/properties";

    public List<Ado> createAllIpbCustomObjects(int limit) {
        List<Ado> existingAdos = adoDbService.loadAll();
        int missingCount = limit - existingAdos.size();

        if (missingCount <= 0) {
            return existingAdos;
        }

        List<Ado> generatedAdos = new ArrayList<>(missingCount);
        for (int i = 0; i < missingCount; i++) {
            generatedAdos.add(createSingleAdo());
        }
        existingAdos.addAll(generatedAdos);
        return existingAdos;
    }

    private Ado createSingleAdo() {
        Ado ado = new Ado();
        JsonObject request = prepareAdo(ado);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(CREATE_NEW_ADO_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());

            JsonObject data = jsonResult.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);
            logger.info(data.toString());

            Ado parsedAdo = parseReply(data);
            if (parsedAdo == null || parsedAdo.getEid() == null || parsedAdo.getType() == null) {
                logger.error("Created ADO is invalid or incomplete: {}", parsedAdo.toString());
                return null;
            }
            return parsedAdo;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject prepareAdo(Ado ado) {
        JsonObject resultingJson = new JsonObject();
        JsonObject data = new JsonObject();

        data.addProperty(RestHelper.ATTR_TYPE, Ado.ENTITY_TYPE_ADO);
        data.add(RestHelper.ATTR_ATTRIBUTES, prepareAttributes(ado));
        data.add(RestHelper.ATTR_RELATIONSHIPS, prepareRelationships(ado));
        resultingJson.add(RestHelper.ATTR_DATA, data);
        return resultingJson;
    }

    private JsonElement prepareRelationships(Ado ado) {
        return null;
    }

    private JsonElement prepareAttributes(Ado ado) {


        return null;
    }


    public Ado doGetAdo(String eid) throws Exception {
        JsonElement object = fetchAdo(RECEIVE_ADO_ENDPOINT, eid);
        Ado ado = parseReply(object);
        logger.info("Ado->{}\n", ado.toString());
        return ado;
    }

    private JsonElement fetchAdo(String receiveAdoEndpoint, String eid) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setEndpoint(String.format(receiveAdoEndpoint, eid))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Ado parseReply(JsonElement j) throws Exception {
        Ado ado = new Ado();

        if (j == null || !j.isJsonObject()) {
            logger.warn("AdoRestService: -> parseReply() -> Empty or invalid JSON root");
            return ado;
        }

        JsonObject root = j.getAsJsonObject();
        JsonObject attributes = root.has(RestHelper.ATTR_ATTRIBUTES) ? root.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES) : null;
        JsonObject relationships = root.has(RestHelper.ATTR_RELATIONSHIPS) ? root.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS) : null;

        if (attributes != null) {
            ado.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
            ado.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
            ado.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));


            try {
                String typeStr = RestHelper.parseString(attributes, RestHelper.ATTR_TYPE);
                logger.info("AdoRestService-> typeStr = {}\n", typeStr);
                if (typeStr != null) {
                    EntityType type = EntityType.valueOf(typeStr);
                    ado.setType((EntityType) dynEnumManager.valueOf(type));
                }
            } catch (IllegalStateException e) {
                logger.warn("ExperimentRestService: -> parseReply() -> Unknown experiment type: {}", e.getMessage());
            }

            ado.setCreatedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_CREATED_AT));

            if (attributes.has(RestHelper.ATTR_FIELDS)) {
                parseFields(attributes, ado);
            }
        } else {
            logger.warn("ExperimentRestService: -> parseReply() -> No 'attributes' block in experiment JSON");
        }
        if (relationships != null) {
            parseRelationships(relationships, ado);

            if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
                parseAncestors(relationships, ado);
            }
            if (relationships.has(RestHelper.ATTR_CHILDREN)) {
                parseChildren(relationships, ado);
            }
            if (relationships.has(RestHelper.ATTR_TEMPLATE)) {
                parseTemplate(relationships, ado);
            }
        } else {
            logger.warn("ExperimentRestService: -> parseReply() -> No 'relationships' block in experiment JSON");
        }

        return ado;
    }

    private void parseFields(JsonObject attributes, Ado ado) {
        JsonObject fieldsJson = attributes.getAsJsonObject(RestHelper.ATTR_FIELDS);
        if (fieldsJson == null) return;

        Set<AdoPropertyValue> values = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : fieldsJson.entrySet()) {
            try {
                JsonObject fieldObj = entry.getValue().getAsJsonObject();
                String value = fieldObj.has("value") ? fieldObj.get("value").getAsString() : null;

                if (value != null) {
                    AdoPropertyValue apv = new AdoPropertyValue();
                    String propertyName = entry.getKey();
                    apv.setAdoId(ado.getId());
                    apv.setPropertyId(propertyName);
                    apv.setPropertyValue(value);
                    values.add(apv);
                }
            } catch (Exception e) {
                logger.warn("AdoRestService: -> parseFields() -> Failed to parse field '{}': {}", entry.getKey(), e.getMessage());
            }
            ado.setPropertyValues(values);
        }
    }

    private void parseRelationships(JsonObject relationships, Ado ado) {
        ado.setCreatedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        // ado.setEditedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        // ado.setOwner(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
    }

    private void parseAncestors(JsonObject relationships, Ado ado) {
        if (!relationships.has(RestHelper.ATTR_ANCESTORS)) {
            logger.warn("AdoRestService: -> parseAncestors() -> No 'ancestors' in relationships");
            return;
        }
        JsonObject ancestorObj = relationships.getAsJsonObject(RestHelper.ATTR_ANCESTORS);
        if (!ancestorObj.has(RestHelper.ATTR_DATA) || !ancestorObj.get(RestHelper.ATTR_DATA).isJsonArray()) {
            logger.warn("AdoRestService: -> parseAncestors() -> 'ancestors' block missing or invalid");
            return;
        }

        JsonArray dataArray = ancestorObj.getAsJsonArray(RestHelper.ATTR_DATA);
        List<SignalsEntity> entities = new ArrayList<>();

        for (JsonElement element : dataArray) {
            try {
                JsonObject ancestorJson = element.getAsJsonObject();
                String id = RestHelper.parseString(ancestorJson, RestHelper.ATTR_ID);

                if (id != null) {
                    JsonElement seJson = fetchAdo(RECEIVE_ADO_ENDPOINT, id);
                    SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
                    entities.add(se);
                    ado.addAncestor(se);
                }
            } catch (Exception e) {
                logger.warn("AdoRestService: -> parseAncestors() -> Failed to parse ancestor: {}", e.getMessage());
            }
        }

        if (!entities.isEmpty()) {
            ado.setAncestorId(entities.get(entities.size() - 1).getId());
        }
    }

    private void parseChildren(JsonObject relationships, Ado ado) {
        if (!relationships.has(RestHelper.ATTR_CHILDREN)) {
            logger.warn("AdoRestService: -> parseChildren() -> No 'children' in relationships");
            return;
        }

        JsonObject children = relationships.getAsJsonObject(RestHelper.ATTR_CHILDREN);
        if (!children.has(RestHelper.ATTR_DATA) || !children.get(RestHelper.ATTR_DATA).isJsonArray()) {
            logger.warn("AdoRestService: -> parseChildren() -> 'children' block missing or invalid");
            return;
        }

        JsonArray dataArray = children.getAsJsonArray(RestHelper.ATTR_DATA);
        for (JsonElement element : dataArray) {
            try {
                JsonObject childJson = element.getAsJsonObject();
                String id = RestHelper.parseString(childJson, RestHelper.ATTR_ID);
                if (id != null) {
                    JsonElement seJson = fetchAdo(RECEIVE_ADO_ENDPOINT, id);
                    SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
                    ado.addChild(se);
                }
            } catch (Exception e) {
                logger.warn("AdoRestService: -> parseChildren() -> Failed to parse child entity: {}", e.getMessage());
            }
        }
    }

    private void parseTemplate(JsonObject relationships, Ado ado) {
        try {
            if (!relationships.has(RestHelper.ATTR_SYSTEM_TEMPLATE)) {
                logger.warn("AdoRestService: -> parseTemplate() -> No 'systemTemplate' in relationships");
                return;
            }

            JsonObject systemTemplate = relationships.getAsJsonObject(RestHelper.ATTR_SYSTEM_TEMPLATE);
            if (!systemTemplate.has(RestHelper.ATTR_DATA)) {
                logger.warn("AdoRestService: -> parseTemplate() -> 'systemTemplate' has no 'data' block");
                return;
            }

            JsonObject data = systemTemplate.getAsJsonObject(RestHelper.ATTR_DATA);
            if (data.has(RestHelper.ATTR_ID)) {
                ado.setTemplateId(data.get(RestHelper.ATTR_ID).getAsString());
            } else {
                logger.warn("AdoRestService: -> parseTemplate() -> 'systemTemplate.data' has no 'id'");
            }

        } catch (Exception e) {
            logger.warn("AdoRestService: -> parseTemplate() -> Failed to parse systemTemplate: {}", e.getMessage());
        }
    }

    public void doGetAdoProperties(Ado ado) {
        JsonElement json = fetchAdo(ADO_GET_PROPERTIES_ENDPOINT, ado.getTemplateId());

        Iterator<JsonElement> iter = json.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement experimentPropertiesObject = iter.next();
            parseAdoProperties(ado, experimentPropertiesObject);
        }
    }

    private void parseAdoProperties(Ado ado, JsonElement adoPropertiesObject) {
        JsonObject propertiesObject = adoPropertiesObject.getAsJsonObject();
        JsonObject definition = propertiesObject.get(RestHelper.ATTR_META).getAsJsonObject().get(RestHelper.ATTR_DEFINITION).getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        AdoProperty adoProperty = new AdoProperty();

        //get property id
        adoProperty.setPropertyId(propertiesObject.get(RestHelper.ATTR_ID).getAsString());

        //get property name
        if (attributes.has(RestHelper.ATTR_NAME)) {
            adoProperty.setPropertyName(attributes.get(RestHelper.ATTR_NAME).getAsString());
        }

        //get property type
        adoProperty.setPropertyType(definition.get(RestHelper.ATTR_TYPE).getAsString());

        //set template id
        adoProperty.setTemplateId(ado.getTemplateId());
        ado.addProperty(adoProperty);
    }

    public void doGetAdoPropertyValues(Ado ado) {
        JsonElement json = fetchAdo(ADO_GET_PROPERTY_VALUES_ENDPOINT, ado.getId());

        Iterator<JsonElement> iter = json.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement adoPropertyValuesObject = iter.next();
            parseAdoPropertyValues(ado, adoPropertyValuesObject);
        }
    }

    private void parseAdoPropertyValues(Ado ado, JsonElement adoPropertyValuesObject) {
        JsonObject propertiesObject = adoPropertyValuesObject.getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();


        String name = Optional.ofNullable(attributes.get(RestHelper.ATTR_NAME))
                .map(JsonElement::getAsString)
                .orElse("");

        String value = Optional.ofNullable(attributes.get(RestHelper.ATTR_VALUE))
                .map(JsonElement::getAsString)
                .orElse("");

        AdoPropertyValue propertyValue = new AdoPropertyValue();

        for (AdoProperty property : ado.getProperties()) {
            if (property.getPropertyName().equalsIgnoreCase(name)) {

                propertyValue.setAdoId(ado.getId());
                propertyValue.setPropertyId(property.getPropertyId());
                propertyValue.setPropertyValue(value);

                ado.addPropertyValue(propertyValue);
            }
        }
    }
}
