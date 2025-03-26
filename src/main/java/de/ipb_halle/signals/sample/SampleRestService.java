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
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class SampleRestService implements RestReplyParser<Sample> {

    private static final String RECEIVE_SAMPLE_ENDPOINT = "/entities/%s";
    private static final String SAMPLE_GET_PROPERTIES_ENDPOINT = "/samples/%s/properties";
    private static final String SAMPLE_GET_PROPERTY_EXPLICITLY_ENDPOINT = "/samples/%s/properties/%s";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private SignalsEntityRestService signalsEntityRestService;

    private static final Logger logger = LogManager.getLogger(SampleRestService.class);

    public Sample doGetSample(String sampleId) throws Exception {
        JsonElement data = fetch(RECEIVE_SAMPLE_ENDPOINT, sampleId);
        return parseReply(data);
    }

    private JsonElement fetch(String endpoint, String sampleId) {
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

    private JsonElement fetchProperty(String endpoint, String sampleId, String samplePropertyKey) {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(endpoint, sampleId, samplePropertyKey))
                    .execute();

            return JsonParser.parseString(restClient.getResponse().getString());

        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Sample parseReply(JsonElement j) throws Exception {
        JsonObject dataObject = j.getAsJsonObject();
        JsonObject attributes = dataObject.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        JsonObject relationships = dataObject.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);

        Sample sample = new Sample();

        sample.setId(RestHelper.parseString(dataObject, RestHelper.ATTR_ID));
        sample.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        sample.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));

        sample.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE))));
        sample.setCreatedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_CREATED_AT));
        sample.setEditedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_EDITED_AT));

        sample.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));

        parseRelationships(dataObject, sample);

        if (attributes.has(Sample.ATTR_STOIC_REF)) {
            parseStoicRef(attributes, sample);
        }
        if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
            parseAncestors(relationships, sample);
        }
        if (relationships.has(RestHelper.ATTR_CHILDREN)) {
            parseChildren(relationships, sample);
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
            JsonElement seJson = fetch(RECEIVE_SAMPLE_ENDPOINT, id);
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
            JsonElement seJson = fetch(RECEIVE_SAMPLE_ENDPOINT, RestHelper.parseString(object, RestHelper.ATTR_ID));
            SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
            sample.addChild(se);
        }
    }


    public void doGetSampleProperties(Sample sample) {
        JsonElement dataArray = fetch(SAMPLE_GET_PROPERTIES_ENDPOINT, sample.getId());
        Iterator<JsonElement> iter = dataArray.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement samplesPropertyObject = iter.next();
            SampleProperty sampleProperty = parseSampleProperties(samplesPropertyObject);
            sample.addProperty(sampleProperty);
        }

    }

    private SampleProperty parseSampleProperties(JsonElement samplesPropertyObject) {
        JsonObject propertiesObject = samplesPropertyObject.getAsJsonObject();
        JsonObject definition = propertiesObject.get(RestHelper.ATTR_META).getAsJsonObject().get(RestHelper.ATTR_DEFINITION).getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();
        JsonObject data = propertiesObject
                .get(RestHelper.ATTR_RELATIONSHIPS).getAsJsonObject()
                .get(RestHelper.ATTR_SAMPLE).getAsJsonObject()
                .get(RestHelper.ATTR_DATA).getAsJsonObject();

        SampleProperty sampleProperty = new SampleProperty();

        if (definition.has(RestHelper.ATTR_TITLE)) {
            sampleProperty.setName(definition.get(RestHelper.ATTR_TITLE).getAsString());
        }
        sampleProperty.setType(definition.get(RestHelper.ATTR_TYPE).getAsString());
        if (definition.has(RestHelper.ATTR_KEY)) {
            sampleProperty.setKey(definition.get(RestHelper.ATTR_KEY).getAsString());
        }
        sampleProperty.setSampleId(data.get(RestHelper.ATTR_ID).getAsString());

        if (attributes.has(RestHelper.ATTR_CONTENT)) {
            sampleProperty.setValue(attributes.get(RestHelper.ATTR_CONTENT).getAsJsonObject().get(RestHelper.ATTR_VALUE).getAsString());
        }


        return sampleProperty;
    }

    public void doGetEachPropertyExplicitly(Sample sample) {
        for (SampleProperty sampleProperty : sample.getProperties()) {
            JsonElement propertyDataObject = fetchProperty(SAMPLE_GET_PROPERTY_EXPLICITLY_ENDPOINT, sample.getId(), sampleProperty.getKey());
            logger.info("DO GET PROPERTY EXPLICITLY sampleId={}\n, samplePropertyKey={}\n, dataObject={}\n", sample.getId(), sampleProperty.getKey(), propertyDataObject);
        }


    }
}
