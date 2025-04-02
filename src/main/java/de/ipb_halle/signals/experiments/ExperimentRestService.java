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

package de.ipb_halle.signals.experiments;

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
import de.ipb_halle.signals.sample.SampleRestService;
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


public class ExperimentRestService implements RestReplyParser<Experiment> {

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private SignalsEntityRestService signalsEntityRestService;

    private static final Logger logger = LogManager.getLogger(SampleRestService.class);

    private static final String RECEIVE_EXPERIMENT_ENDPOINT = "/entities/%s";
    private static final String EXPERIMENT_GET_PROPERTY_VALUES_ENDPOINT = "/entities/%s/properties";
    private static final String EXPERIMENT_GET_PROPERTIES_ENDPOINT = "/entities/templates/%s/fields";
    public static final String CREATE_NEW_EXPERIMENT_ENDPOINT = "/entities?force=true";

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createNewExperiment(Experiment experiment) {
        JsonObject request = prepareExperiment(experiment);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(CREATE_NEW_EXPERIMENT_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);
        } catch (IOException | URISyntaxException | UnexpectedResponseCodeException e) {
            throw new RuntimeException(e);
        }

    }

    private JsonObject prepareExperiment(Experiment experiment) {
        JsonObject resultingJson = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, Experiment.ATTR_TYPE_EXPERIMENT);
        data.add(RestHelper.ATTR_ATTRIBUTES, prepareAttributes(experiment));
        data.add(RestHelper.ATTR_RELATIONSHIPS, prepareRelationships(experiment));
        resultingJson.add(RestHelper.ATTR_DATA, data);
        return resultingJson;
    }

    private JsonElement prepareRelationships(Experiment experiment) {
        JsonObject relationships = new JsonObject();
        relationships.add(RestHelper.ATTR_ANCESTORS, prepareAncestors(experiment));
        relationships.add(RestHelper.ATTR_TEMPLATE, prepareTemplate(experiment));
        // relationships.add(RestHelper.ATTR_WORK_ORDER, prepareWorkOrder(experiment));
        return relationships;
    }

    private JsonElement prepareWorkOrder(Experiment experiment) {


        return null;
    }

    private JsonElement prepareTemplate(Experiment experiment) {
        JsonObject template = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, experiment.getTemplateId().split(":")[0]);
        data.addProperty(RestHelper.ATTR_ID, experiment.getTemplateId());
        template.add(RestHelper.ATTR_DATA, data);
        return template;
    }

    private JsonElement prepareAncestors(Experiment experiment) {
        JsonObject ancestors = new JsonObject();
        JsonArray data = new JsonArray();
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty(RestHelper.ATTR_TYPE, experiment.getAncestorId().split(":")[0]);
        dataObject.addProperty(RestHelper.ATTR_ID, experiment.getAncestorId());
        data.add(dataObject);
        ancestors.add(RestHelper.ATTR_DATA, data);
        return ancestors;
    }

    /**
     * For experiment,
     * the properties are not passed as "fields" during creation,
     * but via separate PATCH or PUT (e.g. /properties or /attributes endpoint of signals)
     * @param experiment
     * @return
     */
    private JsonElement prepareAttributes(Experiment experiment) {
        JsonObject attributes = new JsonObject();
        attributes.addProperty(RestHelper.ATTR_NAME, experiment.getName());

       // attributes.add(RestHelper.ATTR_FIELDS, prepareFields(experiment));

        return attributes;
    }

    private JsonElement prepareFields(Experiment experiment) {
        JsonArray fields = new JsonArray();
        for (ExperimentProperty experimentProperty : experiment.getProperties()) {
            for (ExperimentPropertyValue experimentPropertyValue : experiment.getPropertyValues()) {
                if (experimentPropertyValue.getPropertyId().equalsIgnoreCase(experimentProperty.getPropertyId())) {
                    fields.add(prepareField(experimentProperty, experimentPropertyValue));
                }
            }
        }

        return fields;
    }

    private JsonElement prepareField(ExperimentProperty experimentProperty, ExperimentPropertyValue experimentPropertyValue) {
        JsonObject field = new JsonObject();
        //experiment property id listed as key and in DB as property_key
        field.addProperty(RestHelper.ATTR_ID, experimentProperty.getPropertyId());
        JsonObject content = new JsonObject();

        content.addProperty(RestHelper.ATTR_VALUE, experimentPropertyValue.getPropertyValue());
        field.add(RestHelper.ATTR_CONTENT, content);
        return field;
    }


    public Experiment doGetExperiment(String experimentId) throws Exception {
        JsonElement object = fetchSample(RECEIVE_EXPERIMENT_ENDPOINT, experimentId);
        return parseReply(object);
    }

    private JsonElement fetchSample(String receiveExperimentEndpoint, String experimentId) {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(receiveExperimentEndpoint, experimentId))
                    .execute();

            return JsonParser.parseString(restClient.getResponse().getString());


        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Experiment parseReply(JsonElement j) throws Exception {
        JsonObject dataObject = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);
        JsonObject attributes = dataObject.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        JsonObject relationships = dataObject.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);

        Experiment experiment = new Experiment();

        experiment.setId(RestHelper.parseString(dataObject, RestHelper.ATTR_ID));
        experiment.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        experiment.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));

        experiment.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE))));
        experiment.setCreatedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_CREATED_AT));
        experiment.setEditedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_EDITED_AT));

        experiment.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));

        parseRelationships(dataObject, experiment);

        if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
            parseAncestors(relationships, experiment);
        }
        if (relationships.has(RestHelper.ATTR_CHILDREN)) {
            parseChildren(relationships, experiment);
        }
        if (relationships.has(RestHelper.ATTR_TEMPLATE)) {
            paresTemplate(relationships, experiment);
        }

        return experiment;
    }

    private void parseRelationships(JsonObject relationships, Experiment experiment) {
        experiment.setCreatedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        experiment.setEditedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        experiment.setOwner(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
    }

    private void parseAncestors(JsonObject relationships, Experiment experiment) {
        JsonArray dataArray = relationships
                .getAsJsonObject(RestHelper.ATTR_ANCESTORS)
                .getAsJsonArray(RestHelper.ATTR_DATA);
        List<SignalsEntity> entities = new ArrayList<>();
        for (JsonElement element : dataArray) {
            String id = RestHelper.parseString(element.getAsJsonObject(), RestHelper.ATTR_ID);
            JsonElement seJson = fetchSample(RECEIVE_EXPERIMENT_ENDPOINT, id);
            SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
            entities.add(se);
            experiment.addAncestor(se);
        }
        if (!entities.isEmpty()) {
            experiment.setAncestorId(entities.get(entities.size() - 1).getId());
        }
    }

    private void parseChildren(JsonObject relationships, Experiment experiment) {
        JsonObject children = relationships.get(RestHelper.ATTR_CHILDREN).getAsJsonObject();
        JsonArray dataArray = children.get(RestHelper.ATTR_DATA).getAsJsonArray();
        Iterator<JsonElement> iter = dataArray.iterator();
        while (iter.hasNext()) {
            JsonObject object = iter.next().getAsJsonObject();
            JsonElement seJson = fetchSample(RECEIVE_EXPERIMENT_ENDPOINT, RestHelper.parseString(object, RestHelper.ATTR_ID));
            SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
            experiment.addChild(se);
        }
    }

    private void paresTemplate(JsonElement relationships, Experiment experiment) {
        JsonObject data = relationships.getAsJsonObject()
                .getAsJsonObject(RestHelper.ATTR_SYSTEM_TEMPLATE)
                .getAsJsonObject(RestHelper.ATTR_DATA);
        experiment.setTemplateId(data.get(RestHelper.ATTR_ID).getAsString());
    }

    public void doGetExperimentProperties(Experiment experiment) {
        JsonElement json = fetchSample(EXPERIMENT_GET_PROPERTIES_ENDPOINT, experiment.getTemplateId());
        JsonArray dataArray = json.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA);

        Iterator<JsonElement> iter = dataArray.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement experimentPropertiesObject = iter.next();
            parseExperimentProperties(experiment, experimentPropertiesObject);
        }
    }

    private void parseExperimentProperties(Experiment experiment, JsonElement experimentPropertiesObject) {
        JsonObject propertiesObject = experimentPropertiesObject.getAsJsonObject();
        JsonObject definition = propertiesObject.get(RestHelper.ATTR_META).getAsJsonObject().get(RestHelper.ATTR_DEFINITION).getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        ExperimentProperty experimentProperty = new ExperimentProperty();

        //get property id
        experimentProperty.setPropertyId(propertiesObject.get(RestHelper.ATTR_ID).getAsString());

        //get property name
        if (attributes.has(RestHelper.ATTR_NAME)) {
            experimentProperty.setPropertyName(attributes.get(RestHelper.ATTR_NAME).getAsString());
        }

        //get property type
        experimentProperty.setPropertyType(definition.get(RestHelper.ATTR_TYPE).getAsString());

        //set template id
        experimentProperty.setTemplateId(experiment.getTemplateId());
        experiment.addProperty(experimentProperty);
    }

    // RECEIVE EXPERIMENT PROPERTY VALUES
    public void doGetExperimentPropertyValues(Experiment experiment) {
        JsonElement json = fetchSample(EXPERIMENT_GET_PROPERTY_VALUES_ENDPOINT, experiment.getId());
        JsonArray dataArray = json.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA);

        Iterator<JsonElement> iter = dataArray.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement experimentPropertiesObject = iter.next();
            parseExperimentPropertyValues(experiment, experimentPropertiesObject);
        }
    }

    private void parseExperimentPropertyValues(Experiment experiment, JsonElement experimentPropertiesObject) {
        JsonObject propertiesObject = experimentPropertiesObject.getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        String value = attributes.get(RestHelper.ATTR_VALUE).getAsString();
        String name = "";
        if (attributes.has(RestHelper.ATTR_NAME)) {
            name = attributes.get(RestHelper.ATTR_NAME).getAsString();
        }

        ExperimentPropertyValue propertyValue = new ExperimentPropertyValue();

        for (ExperimentProperty property : experiment.getProperties()) {
            if (property.getPropertyName().equalsIgnoreCase(name)) {

                propertyValue.setExperimentId(experiment.getId());
                propertyValue.setPropertyId(property.getPropertyId());
                propertyValue.setPropertyValue(value);

                experiment.addPropertyValue(propertyValue);
            }
        }

    }
}
