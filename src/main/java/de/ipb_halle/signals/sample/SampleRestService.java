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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SampleRestService implements RestReplyParser<Sample> {

    private static final String RECEIVE_SAMPLE_ENDPOINT = "/entities/%s";
    private static final String SAMPLE_GET_PROPERTIES_ENDPOINT = "/samples/%s/properties";
    private static final String SAMPLE_GET_PROPERTY_EXPLICITLY_ENDPOINT = "/samples/%s/properties/%s";
    //?force=true if we don't want to send a digest
    public static final String CREATE_NEW_SAMPLE_ENDPOINT = "/entities?force=true";
    //public static final String CREATE_NEW_SAMPLE_ENDPOINT = "/entities?digest=%s";
    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private SignalsEntityRestService signalsEntityRestService;

    private static final Logger logger = LogManager.getLogger(SampleRestService.class);


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createNewSample(Sample sample) {
        JsonObject request = prepareSample(sample);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(CREATE_NEW_SAMPLE_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);
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
        attributes.add(RestHelper.ATTR_FIELDS, prepareFields(sample));
        return attributes;
    }

    //toDo method doesn't load sample property values needs to be refactored
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


    public Sample doGetSample(String sampleId) throws Exception {
        JsonElement object = fetchSample(RECEIVE_SAMPLE_ENDPOINT, sampleId);
        return parseReply(object);
    }

    private JsonElement fetchSample(String endpoint, String sampleId) {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(endpoint, sampleId))
                    .execute();


            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);


        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonElement fetchProperty(String endpoint, String sampleId, String samplePropertyId) {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(endpoint, sampleId, samplePropertyId))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

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
        if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
            parseAncestors(relationships, sample);
        }
        if (relationships.has(RestHelper.ATTR_CHILDREN)) {
            parseChildren(relationships, sample);
        }
        if (relationships.has(RestHelper.ATTR_TEMPLATE)) {
            paresTemplate(relationships, sample);
        }
        return sample;
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

    private void parseChildren(JsonObject relationships, Sample sample) {
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
        //JsonObject attributes = includedObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();
        sample.setTemplateId(data.get(RestHelper.ATTR_ID).getAsString());
        //sample.setTemplateName(attributes.get(RestHelper.ATTR_NAME).getAsString());
    }


    public void doGetSampleProperties(Sample sample) {
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
            sampleProperty.setPropertyId(definition.get(RestHelper.ATTR_KEY).getAsString());
            samplePropertyValue.setPropertyId(sampleProperty.getPropertyId());
        }
        //get property name
        if (definition.has(RestHelper.ATTR_TITLE)) {
            sampleProperty.setPropertyName(definition.get(RestHelper.ATTR_TITLE).getAsString());
        }
        //get property type
        sampleProperty.setPropertyType(definition.get(RestHelper.ATTR_TYPE).getAsString());

        //get property value
        if (attributes.has(RestHelper.ATTR_CONTENT)) {
            samplePropertyValue.setPropertyValue(attributes.get(RestHelper.ATTR_CONTENT).getAsJsonObject().get(RestHelper.ATTR_VALUE).getAsString());
        }
        sample.addProperty(sampleProperty);
        sample.addPropertyValue(samplePropertyValue);
    }

    //toDo implement or delete, depends on proposal
    public void doGetEachPropertyExplicitly(Sample sample) {
        for (SampleProperty sampleProperty : sample.getProperties()) {
            JsonElement propertyDataObject = fetchProperty(SAMPLE_GET_PROPERTY_EXPLICITLY_ENDPOINT, sample.getId(), sampleProperty.getPropertyId());
            //logger.info("DO GET PROPERTY EXPLICITLY sampleId={}\n, samplePropertyKey={}\n, dataObject={}\n", sample.getId(), sampleProperty.getKey(), propertyDataObject);
        }


    }
}
