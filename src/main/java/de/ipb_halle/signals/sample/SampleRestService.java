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

package de.ipb_halle.signals.sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class SampleRestService implements RestReplyParser<Sample> {

    private static final String RECEIVE_SAMPLE_ENDPOINT = "/entities/%s";
    private static final String SAMPLE_GET_PROPERTIES_ENDPOINT = "/samples/%s/properties";
    // private static final String SAMPLE_GET_PROPERTY_EXPLICITLY_ENDPOINT = "/samples/%s/properties/%s";
    //?force=true if we don't want to send a digest
    public static final String CREATE_NEW_SAMPLE_ENDPOINT = "/entities?force=true";
    //public static final String CREATE_NEW_SAMPLE_ENDPOINT = "/entities?digest=%s";
    private static final String PATCH_UPDATE_SAMPLE_PROPERTIES = "/samples/%s/properties?force=true";
    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private SignalsEntityRestService signalsEntityRestService;

    private static final Logger logger = LogManager.getLogger(SampleRestService.class);

    public SampleRestService(RestClient restClient) {
        this.restClient = restClient;
    }

    public SampleRestService() {
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String createNewSample(Sample sample) {
        JsonObject request = prepareSample(sample);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(CREATE_NEW_SAMPLE_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);

            //Return String id of generated container
            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            logger.info("JSON RESULT=================================================\n{}\n", jsonResult);
            JsonElement dataElement = jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

            if (dataElement.isJsonArray()) {
                JsonObject firstObj = dataElement.getAsJsonArray().get(0).getAsJsonObject();
                return firstObj.get(RestHelper.ATTR_ID).getAsString();
            } else {
                JsonObject obj = dataElement.getAsJsonObject();
                return obj.get(RestHelper.ATTR_ID).getAsString();
            }

        } catch (IOException | URISyntaxException | UnexpectedResponseCodeException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject prepareSample(Sample sample) {
        JsonObject resultingJson = new JsonObject();
        JsonObject data = new JsonObject();

        data.addProperty(RestHelper.ATTR_TYPE, Sample.ATTR_SAMPLE);
        data.add(RestHelper.ATTR_ATTRIBUTES, prepareAttributes(sample));
        data.add(RestHelper.ATTR_RELATIONSHIPS, prepareRelationships(sample));

        resultingJson.add(RestHelper.ATTR_DATA, data);
        return resultingJson;
    }

    private JsonElement prepareRelationships(Sample sample) {
        JsonObject relationships = new JsonObject();
        relationships.add(RestHelper.ATTR_ANCESTORS, prepareAncestors(sample));
        relationships.add(RestHelper.ATTR_TEMPLATE, prepareTemplate(sample));
        return relationships;
    }

    private JsonElement prepareAncestors(Sample sample) {
        JsonObject ancestors = new JsonObject();
        JsonArray data = new JsonArray();
        JsonObject dataObject = new JsonObject();

        dataObject.addProperty(RestHelper.ATTR_TYPE, sample.getAncestorId().split(":")[0]);
        dataObject.addProperty(RestHelper.ATTR_ID, sample.getAncestorId());
        data.add(dataObject);
        ancestors.add(RestHelper.ATTR_DATA, data);
        return ancestors;
    }

    private JsonElement prepareTemplate(Sample sample) {
        JsonObject template = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, sample.getTemplateId().split(":")[0]);
        data.addProperty(RestHelper.ATTR_ID, sample.getTemplateId());
        template.add(RestHelper.ATTR_DATA, data);
        return template;
    }

    private JsonElement prepareAttributes(Sample sample) {
        JsonObject attributes = new JsonObject();

        // For non-chemical sample case
        if (!sample.isChemicalSample()) {
            attributes.add(RestHelper.ATTR_FIELDS, prepareFields(sample));
        }

        // For chemical sample case
        if (sample.isChemicalSample()) {
            attributes.add(RestHelper.ATTR_STOIC_REF, prepareStoicRef(sample));
        }

        /** chemical sample structure
         * {
         *   "data": {
         *     "type": "entity",
         *     "attributes": {
         *       "type": "sample",
         *       "stoicRef": {
         *         "eid": "chemicalDrawing:abc12345",
         *         "rowId": "1"
         *       }
         *     },
         *     "relationships": {
         *       "ancestors": {
         *         "data": [
         *           {
         *             "type": "entity",
         *             "id": "samplesContainer:xyz"
         *           }
         *         ]
         *       },
         *       "template": {
         *         "data": {
         *           "type": "template",
         *           "id": "testcompound-template-id"
         *         }
         *       }
         *     }
         *   }
         * }
         */
        return attributes;
    }

    private JsonElement prepareStoicRef(Sample sample) {
        JsonObject stoicRef = new JsonObject();
        stoicRef.addProperty(RestHelper.ATTR_EID, sample.getStoicRef().getEid());
        stoicRef.addProperty(RestHelper.ATTR_ROW_ID, sample.getStoicRef().getRowId());
        return stoicRef;
    }

    private JsonArray prepareFields(Sample sample) {
        JsonArray fields = new JsonArray();
        for (SampleProperty sampleProperty : sample.getProperties()) {
            for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
                if (samplePropertyValue.getPropertyId() != null &&
                        samplePropertyValue.getPropertyId().equalsIgnoreCase(sampleProperty.getPropertyId())) {
                    fields.add(prepareField(sampleProperty, samplePropertyValue));
                }
            }
        }
        return fields;
    }

    private JsonElement prepareField(SampleProperty sampleProperty, SamplePropertyValue samplePropertyValue) {
        JsonObject field = new JsonObject();
        //samples property id listed as key and in DB as property_key
        field.addProperty(RestHelper.ATTR_ID, sampleProperty.getPropertyId());
        JsonObject content = new JsonObject();

        content.addProperty(RestHelper.ATTR_VALUE, samplePropertyValue.getPropertyValue());
        field.add(RestHelper.ATTR_CONTENT, content);
        return field;
    }

    //tested
    public Sample doGetSample(String sampleId) throws Exception {
        JsonElement object = fetchSample(RECEIVE_SAMPLE_ENDPOINT, sampleId);
        Sample sample = parseReply(object);

        JsonObject relationships = object.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);
        getSampleAncestors(sample, relationships);
        getSampleChildren(sample, relationships);
        return sample;
    }

    //tested
    JsonElement fetchSample(String endpoint, String sampleId) throws IOException, UnexpectedResponseCodeException, URISyntaxException {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(endpoint, sampleId))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);
        } catch (UnexpectedResponseCodeException e) {
            throw new UnexpectedResponseCodeException();
        } catch (IOException e) {
            throw new IOException(e);
        } catch (URISyntaxException e) {
            throw new URISyntaxException("Exception: ", e.getMessage());
        }
    }

    //tested
    @Override
    public Sample parseReply(JsonElement j) throws Exception {

        JsonObject data = j.getAsJsonObject();

        JsonObject attributes = data.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();
        JsonObject relationships = data.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);

        Sample sample = new Sample();

        sample.setId(RestHelper.parseString(data, RestHelper.ATTR_ID));
        sample.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        sample.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));

        sample.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE))));
        sample.setCreatedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_CREATED_AT));
        sample.setEditedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_EDITED_AT));

        sample.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));

        parseRelationships(data, sample);

        if (attributes.has(Sample.ATTR_STOIC_REF)) {
            parseStoicRef(attributes, sample);
        }

        if (relationships.has(RestHelper.ATTR_TEMPLATE)) {
            paresTemplate(relationships, sample);
        }
        return sample;
    }

    //tested
    void getSampleAncestors(Sample sample, JsonElement element) throws Exception {
        JsonObject relationships = element.getAsJsonObject();
        if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
            parseAncestors(relationships, sample);
        }
    }

    //tested
    void getSampleChildren(Sample sample, JsonElement element) throws Exception {
        JsonObject relationships = element.getAsJsonObject();
        if (relationships.has(RestHelper.ATTR_CHILDREN)) {
            parseChildren(relationships, sample);
        }
    }

    private void parseRelationships(JsonObject relationships, Sample sample) {
        sample.setCreatedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        sample.setEditedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        sample.setOwner(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
    }

    private void parseStoicRef(JsonObject attributes, Sample sample) {
        JsonObject stoicRef = attributes.get(Sample.ATTR_STOIC_REF).getAsJsonObject();
        sample.setStoicRef(new StoicRef()
                .setEid(stoicRef.get(StoicRef.ATTR_EID).getAsString())
                .setRowId(stoicRef.get(StoicRef.ATTR_ROW_ID).getAsString())
        );
    }

    private void parseAncestors(JsonObject relationships, Sample sample) throws Exception {
        System.out.println("PARSE ANCESTOR");
        JsonArray dataArray = relationships
                .getAsJsonObject(RestHelper.ATTR_ANCESTORS)
                .getAsJsonArray(RestHelper.ATTR_DATA);
        List<SignalsEntity> entities = new ArrayList<>();
        for (JsonElement element : dataArray) {
            String id = RestHelper.parseString(element.getAsJsonObject(), RestHelper.ATTR_ID);
            JsonElement seJson = fetchSample(RECEIVE_SAMPLE_ENDPOINT, id);
            SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
            entities.add(se);
            sample.addAncestor(se);
        }
        if (!entities.isEmpty()) {
            sample.setAncestorId(entities.get(entities.size() - 1).getId());
        }
    }

    private void parseChildren(JsonObject relationships, Sample sample) throws UnexpectedResponseCodeException, IOException, URISyntaxException {
        JsonObject children = relationships.get(RestHelper.ATTR_CHILDREN).getAsJsonObject();
        JsonArray dataArray = children.get(RestHelper.ATTR_DATA).getAsJsonArray();
        Iterator<JsonElement> iter = dataArray.iterator();
        while (iter.hasNext()) {
            JsonObject object = iter.next().getAsJsonObject();
            JsonElement seJson = fetchSample(RECEIVE_SAMPLE_ENDPOINT, RestHelper.parseString(object, RestHelper.ATTR_ID));
            SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
            sample.addChild(se);
        }
    }

    private void paresTemplate(JsonElement relationships, Sample sample) {
        JsonObject data = relationships.getAsJsonObject()
                .getAsJsonObject(RestHelper.ATTR_TEMPLATE)
                .getAsJsonObject(RestHelper.ATTR_DATA);
        sample.setTemplateId(data.get(RestHelper.ATTR_ID).getAsString());
    }

    public void doGetSampleProperties(Sample sample) throws UnexpectedResponseCodeException, IOException, URISyntaxException {
        JsonElement json = fetchSample(SAMPLE_GET_PROPERTIES_ENDPOINT, sample.getId());

        Iterator<JsonElement> iter = json.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement samplesPropertyObject = iter.next();
            parseSampleProperties(sample, samplesPropertyObject);
        }
    }

    private void parseSampleProperties(Sample sample, JsonElement samplesPropertyObject) {
        JsonObject propertiesObject = samplesPropertyObject.getAsJsonObject();
        JsonObject definition = propertiesObject.get(RestHelper.ATTR_META).getAsJsonObject().get(RestHelper.ATTR_DEFINITION).getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        SampleProperty sampleProperty = new SampleProperty();
        SamplePropertyValue samplePropertyValue = new SamplePropertyValue();
        samplePropertyValue.setSampleId(sample.getId());

        //get propertyID
        if (definition.has(RestHelper.ATTR_KEY)) {
            String propertyId = optStr(definition, RestHelper.ATTR_KEY);
            sampleProperty.setPropertyId(propertyId);
            samplePropertyValue.setPropertyId(sampleProperty.getPropertyId());
        }
        //get property name
        if (definition.has(RestHelper.ATTR_TITLE)) {
            String propertyName = optStr(definition, RestHelper.ATTR_TITLE);
            sampleProperty.setPropertyName(propertyName);
        }
        //get property type
        String propertyType = optStr(definition, RestHelper.ATTR_TYPE);
        sampleProperty.setPropertyType(propertyType);

        //get property value
        if (attributes.has(RestHelper.ATTR_CONTENT)) {
            JsonObject content = attributes.get(RestHelper.ATTR_CONTENT).getAsJsonObject();
            String value = extractContentString(content);
            samplePropertyValue.setPropertyValue(value);
        }

        sample.addProperty(sampleProperty);
        sample.addPropertyValue(samplePropertyValue);
    }

    /**
     * Updates the property values of a given sample by sending a PATCH request to the
     * Signals REST API endpoint "/samples/{sampleId}/properties".
     * <p>
     * This method constructs the appropriate JSON body with the given property-value pairs
     * and performs the request using {@code force=true} to bypass digest validation.
     * <p>
     * Note: Only editable/open samples can be updated. Ensure that the sample is not closed.
     *
     * @param kvm      a map of property IDs to their new string values
     * @param sampleId the Signals EID of the sample to be updated (e.g. "sample:abc123")
     * @throws RuntimeException if the REST call fails or returns an unexpected response code
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateSamplePropertyValues(HashMap<String, String> kvm, String sampleId) {
        JsonObject request = prepareFieldValueJson(kvm);

        try {
            restClient.reset()
                    .setMethod(Method.PATCH)
                    .setEndpoint(String.format(PATCH_UPDATE_SAMPLE_PROPERTIES, sampleId))
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_OK);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            logger.info("SampleRestService=> jsonresult = {}\n", jsonResult.toString());

        } catch (IOException | URISyntaxException | UnexpectedResponseCodeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prepares the JSON request body for updating sample properties.
     * <p>
     * The generated JSON follows the expected structure for PATCH requests to
     * the "/samples/{sampleId}/properties" endpoint, including required "type" and nested
     * "attributes" and "content" fields for each property.
     *
     * @param kvm a map of property IDs to their desired string values
     * @return a JsonObject representing the request payload
     */
    private JsonObject prepareFieldValueJson(HashMap<String, String> kvm) {
        JsonObject root = new JsonObject();
        JsonObject data = new JsonObject();
        JsonObject attributes = new JsonObject();
        JsonArray dataArray = new JsonArray();

        for (Map.Entry<String, String> entry : kvm.entrySet()) {
            JsonObject propertyObj = getJsonObject(entry);
            dataArray.add(propertyObj);
        }

        attributes.add(RestHelper.ATTR_DATA, dataArray);
        data.add(RestHelper.ATTR_ATTRIBUTES, attributes);
        root.add(RestHelper.ATTR_DATA, data);

        return root;
    }

    /**
     * Builds a single JSON object representing one property update entry.
     * <p>
     * The object includes the property ID, type ("text"), and its new value
     * in the required nested "attributes" -> "content" -> "value" structure.
     *
     * @param entry a key-value pair representing a property ID and its new value
     * @return a JsonObject for this individual property update
     */
    private static JsonObject getJsonObject(Map.Entry<String, String> entry) {
        //Property is a field by sample
        String propertyId = entry.getKey();
        String value = entry.getValue();

        JsonObject propertyObj = new JsonObject();
        propertyObj.addProperty(RestHelper.ATTR_ID, propertyId);

        // Define the type of the field depended on ID
        String type = propertyId.equals("109") ? "link" : "text";
        propertyObj.addProperty(RestHelper.ATTR_TYPE, type);

        // Define content
        JsonObject propertyAttributes = new JsonObject();
        JsonObject content = new JsonObject();
        JsonArray valuesArray = new JsonArray();
        JsonObject valueJson = new JsonObject();

        //This property with id 109 is reference on custom object type "ada" which stands for IBP code (value consist of type, name, eid)
        if (propertyId.equals("109")) {
            valueJson.addProperty(RestHelper.ATTR_TYPE, value.split(";")[0]);
            valueJson.addProperty(RestHelper.ATTR_NAME, value.split(";")[1]);
            valueJson.addProperty(RestHelper.ATTR_EID, value.split(";")[2]);
            valuesArray.add(valueJson);
            content.add(RestHelper.ATTR_VALUES, valuesArray);
        } else {
            content.addProperty(RestHelper.ATTR_VALUE, value);
        }

        propertyAttributes.add(RestHelper.ATTR_CONTENT, content);
        propertyObj.add(RestHelper.ATTR_ATTRIBUTES, propertyAttributes);

        return propertyObj;
    }

    private static JsonObject optObj(JsonObject src, String key) {
        return (src != null && src.has(key) && src.get(key).isJsonObject())
                ? src.getAsJsonObject(key) : null;
    }

    private static String optStr(JsonObject src, String key) {
        if (src == null) return null;
        JsonElement e = src.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    private static String extractContentString(JsonObject content) {
        if (content == null) return null;

        // 1) regular value
        if (content.has("value")) {
            JsonElement v = content.get("value");
            if (v == null || v.isJsonNull()) return null;
            if (v.isJsonPrimitive()) {
                var p = v.getAsJsonPrimitive();
                if (p.isBoolean()) return Boolean.toString(p.getAsBoolean());
                if (p.isNumber()) return p.getAsNumber().toString();
                return p.getAsString();
            }
            // array / json — return as JSON
            return v.toString();
        }
        // 2) Some fields received like "user", "rawValue"
        if (content.has("user")) return optStr(content, "user");
        if (content.has("rawValue")) return optStr(content, "rawValue");

        return null;
    }


}
