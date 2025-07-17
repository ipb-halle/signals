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
import de.ipb_halle.signals.experiments.properties.ExperimentProperty;
import de.ipb_halle.signals.experiments.properties.ExperimentPropertyValue;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.sample.SampleRestService;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@LocalBean
@Stateless
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
    public static final String APPEND_CHEMICAL_DRAWING_TO_EXPERIMENT = "/entities/%s/children/%s"; // eid, filename
    private static final String CHEMICAL_DRAWINGS_ADD_REACTION_CDXML = "/chemicaldrawings/%s/reaction/%s";

    /**
     * Creates a new {@code Experiment} entity in the Signals platform by sending
     * a POST request with the provided experiment data.
     *
     * <p>This method serializes the given {@link Experiment} object into a JSON structure
     * expected by the Signals REST API. The resulting JSON is submitted to the endpoint
     * responsible for creating new experiment entities. On success, the response is parsed
     * and mapped back into a new {@code Experiment} object with the assigned EID and any
     * additional properties set by the server.
     *
     * <p>The method is annotated with {@code @TransactionAttribute(REQUIRES_NEW)} to ensure
     * that the experiment creation occurs in a new transactional context, independent of the
     * surrounding transaction.
     *
     * @param experiment the {@link Experiment} object to create in the Signals system
     * @return the newly created {@link Experiment} object, including its assigned ID and metadata
     * @throws RuntimeException if any error occurs during REST communication or response parsing
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Experiment createNewExperiment(Experiment experiment) {
        JsonObject request = prepareExperiment(experiment);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(CREATE_NEW_EXPERIMENT_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            JsonObject data = jsonResult.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);
            Experiment result = parseReply(data);
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@code chemicalDrawing} entity as a child of the given experiment entity
     * by uploading an optional CDXML structure to the Signals platform.
     *
     * <p>This method constructs the appropriate endpoint using the parent {@code experimentId}
     * and a provided filename, and uploads the CDXML content (which can be empty).
     * The Signals REST API is called via a {@code POST} request, and the ID of the newly
     * created {@code chemicalDrawing} entity is returned.
     *
     * <p><strong>Note:</strong> This method expects a {@code 201 Created} response code from the API.
     * If another code is returned, an {@link UnexpectedResponseCodeException} is thrown.
     *
     * @param experimentId the ID of the parent experiment entity (EID format)
     * @param filename     the logical filename to assign to the uploaded CDXML (e.g., {@code "empty_structure.cdxml"})
     * @param cdxmlContent the CDXML chemical structure to upload (may be an empty string)
     * @return the ID of the created {@code chemicalDrawing} entity (in Signals EID format)
     * @throws RuntimeException if any I/O, URI, or REST API errors occur
     */
    public String createNewChemicalDrawingAsExperimentChild(String experimentId, String filename, String cdxmlContent) {
        String encodedEid = URLEncoder.encode(experimentId, StandardCharsets.UTF_8);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        String endpoint = String.format(APPEND_CHEMICAL_DRAWING_TO_EXPERIMENT, encodedEid, encodedFilename);
        try {
            RestClient execute = restClient
                    .uploadChemicalDrawing(
                            experimentId,
                            filename,
                            cdxmlContent,
                            endpoint)
                    .execute();


            // Return String id of generated experiment
            JsonElement jsonResult = JsonParser.parseString(execute.getResponse().getString());
            return jsonResult.getAsJsonObject()
                    .getAsJsonObject(RestHelper.ATTR_DATA)
                    .get(RestHelper.ATTR_ID)
                    .getAsJsonPrimitive().getAsString();

        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            logger.info(" EXCEPTION By PARSING OR REQUEST = {}\n", e);
            throw new RuntimeException(e);
        }
    }

    private JsonObject prepareExperiment(Experiment experiment) {
        JsonObject resultingJson = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, Experiment.ENTITY_TYPE_EXPERIMENT);
        data.add(RestHelper.ATTR_ATTRIBUTES, prepareAttributes(experiment));
        data.add(RestHelper.ATTR_RELATIONSHIPS, prepareRelationships(experiment));
        resultingJson.add(RestHelper.ATTR_DATA, data);
        return resultingJson;
    }

    private JsonElement prepareRelationships(Experiment experiment) {
        JsonObject relationships = new JsonObject();
        relationships.add(RestHelper.ATTR_ANCESTORS, prepareAncestors(experiment));
        relationships.add(RestHelper.ATTR_TEMPLATE, prepareTemplate(experiment));
        return relationships;
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
     * Prepares the "attributes" section of a Signals entity creation payload.
     * <p>
     * This method maps {@link ExperimentPropertyValue} items from the local experiment model
     * to flat attribute key-value pairs, where keys are the human-readable property names
     * (e.g. "Three_Letter_Code") as required by the Signals API.
     * <p>
     * <strong>Important:</strong> The Signals REST API <em>does not accept</em> a nested
     * "fields" array inside the "attributes" block for entity creation (e.g. experiments).
     * Although the response JSON from Signals contains an "attributes.fields" block,
     * using this same structure in POST requests will result in the following error:
     * <pre>
     * HTTP 400 BadRequest
     * {
     *   "description": "Unrecognized data parameter name was specified: fields"
     * }
     * </pre>
     * <p>
     * Therefore, instead of using an array like:
     * <pre>
     * "fields": [
     *   { "id": "4003", "content": { "value": "ADM" } }
     * ]
     * </pre>
     * you must send:
     * <pre>
     * "Three_Letter_Code": "ADM"
     * </pre>
     * directly inside "attributes".
     *
     * @param experiment the experiment containing local property values
     * @return a JsonObject with Signals-compliant flat attributes
     */
    private JsonElement prepareAttributes(Experiment experiment) {
        // Experiment Template InhouseExperiment
        Map<String, String> propertyIdToNameMap = Map.of(
                "4003", "Three_Letter_Code",
                "4001", "Name",
                "4005", "Journal",
                "4006", "Procedure_id",
                "4004", "Individual_code",
                "4002", "Description"
        );

        JsonObject attributes = new JsonObject();
        attributes.addProperty(RestHelper.ATTR_NAME, experiment.getName());
        if (experiment.getDescription() != null) {
            attributes.addProperty(RestHelper.ATTR_DESCRIPTION, experiment.getDescription());
        }
        // Add field values to Experiment (Experiment Property Values)
        for (ExperimentPropertyValue val : experiment.getPropertyValues()) {
            String fieldName = propertyIdToNameMap.get(val.getPropertyId());
            if (fieldName != null) {
                attributes.addProperty(fieldName, val.getPropertyValue());
            }
        }
        return attributes;
    }

    /**
     * Retrieves an {@link Experiment} entity from the Signals platform based on its EID.
     *
     * <p>This method sends a GET request to the defined experiment endpoint,
     * receives the JSON representation of the experiment, and parses it into
     * a {@link Experiment} object.
     *
     * @param experimentId the EID of the experiment to fetch (e.g., "experiment:abc123...")
     * @return the {@link Experiment} object parsed from the response
     * @throws Exception if any error occurs during the fetch or parsing process
     */
    public Experiment doGetExperiment(String experimentId) throws Exception {
        JsonElement object = fetchExperiment(RECEIVE_EXPERIMENT_ENDPOINT, experimentId);
        Experiment experiment = parseReply(object);
        logger.info("EXPERIMENT-> {}\n", experiment.toString());
        return experiment;
    }

    /**
     * Sends a GET request to retrieve experiment JSON from the specified endpoint.
     *
     * <p>This helper method builds and executes the GET call using {@code restClient},
     * parses the HTTP response body into a JSON element, and extracts the 'data' section
     * of the response.
     *
     * @param receiveExperimentEndpoint the endpoint URL template for fetching the experiment
     * @param experimentId              the ID of the experiment (will be used in endpoint formatting)
     * @return the {@link JsonElement} containing the raw experiment data
     * @throws RuntimeException if an error occurs during the request or parsing
     */
    private JsonElement fetchExperiment(String receiveExperimentEndpoint, String experimentId) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setEndpoint(String.format(receiveExperimentEndpoint, experimentId))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a JSON representation of an experiment into a fully populated {@link Experiment} object.
     *
     * <p>This method extracts the 'attributes' and 'relationships' blocks from the JSON structure,
     * and sets the appropriate fields in the {@code Experiment} entity. It supports parsing of:
     * <ul>
     *     <li>Basic metadata (name, description, ID, type)</li>
     *     <li>Timestamps (createdAt, editedAt)</li>
     *     <li>Digest for optimistic locking</li>
     *     <li>Nested relationships like ancestors, children, template</li>
     *     <li>Custom fields, if present</li>
     * </ul>
     *
     * <p>If the JSON is malformed or incomplete, the method logs the error and returns a partially
     * filled {@code Experiment}.
     *
     * @param json the JSON object containing the Signals experiment data
     * @return the parsed {@link Experiment} instance
     * @throws Exception if parsing fails critically
     */
    @Override
    public Experiment parseReply(JsonElement json) throws Exception {
        Experiment experiment = new Experiment();

        if (json == null || !json.isJsonObject()) {
            logger.warn("ExperimentRestService: -> parseReply() -> Empty or invalid JSON root");
            return experiment;
        }

        JsonObject root = json.getAsJsonObject();
        JsonObject attributes = root.has(RestHelper.ATTR_ATTRIBUTES) ? root.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES) : null;
        JsonObject relationships = root.has(RestHelper.ATTR_RELATIONSHIPS) ? root.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS) : null;

        if (attributes != null) {
            experiment.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
            experiment.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
            experiment.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));


            String typeStr = RestHelper.parseString(attributes, RestHelper.ATTR_TYPE);
            try {
                EntityType type = typeStr != null ? EntityType.valueOf(typeStr) : null;
                if (type != null) {
                    experiment.setType((EntityType) dynEnumManager.valueOf(type));
                }
            } catch (IllegalStateException e) {
                logger.warn("ExperimentRestService: -> parseReply() -> Unknown experiment type: {}", typeStr);
            }

            experiment.setCreatedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_CREATED_AT));
            experiment.setEditedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_EDITED_AT));
            experiment.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));


            if (attributes.has(RestHelper.ATTR_FIELDS)) {
                parseFields(attributes, experiment);
            }
        } else {
            logger.warn("ExperimentRestService: -> parseReply() -> No 'attributes' block in experiment JSON");
        }
        if (relationships != null) {
            parseRelationships(relationships, experiment);

            if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
                parseAncestors(relationships, experiment);
            }
            if (relationships.has(RestHelper.ATTR_CHILDREN)) {
                parseChildren(relationships, experiment);
            }
            if (relationships.has(RestHelper.ATTR_TEMPLATE)) {
                parseTemplate(relationships, experiment);
            }
        } else {
            logger.warn("ExperimentRestService: -> parseReply() -> No 'relationships' block in experiment JSON");
        }

        return experiment;
    }

    /**
     * Parses the 'fields' section from the experiment's JSON attributes and populates property values.
     *
     * <p>This method extracts key-value pairs from the 'fields' JSON object,
     * where each key is treated as a property ID and its associated value is stored
     * in an {@link ExperimentPropertyValue}. All parsed values are attached to the given {@link Experiment}.
     *
     * @param attributes the JSON object containing experiment attributes
     * @param experiment the {@link Experiment} object to populate with parsed fields
     */
    private void parseFields(JsonObject attributes, Experiment experiment) {
        JsonObject fieldsJson = attributes.getAsJsonObject(RestHelper.ATTR_FIELDS);
        if (fieldsJson == null) return;

        Set<ExperimentPropertyValue> values = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : fieldsJson.entrySet()) {
            try {
                JsonObject fieldObj = fieldsJson.getAsJsonObject();
                String value = fieldObj.has("value") ? fieldObj.get("value").getAsString() : null;

                if (value != null) {
                    ExperimentPropertyValue epv = new ExperimentPropertyValue();
                    String propertyName = entry.getKey();
                    epv.setExperimentId(experiment.getId());
                    epv.setPropertyId(propertyName);
                    epv.setPropertyValue(value);
                    values.add(epv);
                }
            } catch (Exception e) {
                logger.warn("ExperimentRestService: -> parseFields() -> Failed to parse field '{}': {}", entry.getKey(), e.getMessage());
            }
            experiment.setPropertyValues(values);
        }
    }

    /**
     * Parses the basic relationship fields such as 'createdBy', 'editedBy', and 'owner' from the JSON structure.
     *
     * <p>This method extracts references to users responsible for creating and editing the experiment,
     * as well as the current owner. These are stored in {@link UserReference} fields of the {@link Experiment}.
     *
     * @param relationships the 'relationships' JSON object of the experiment
     * @param experiment    the {@link Experiment} object to populate with relationship data
     */
    private void parseRelationships(JsonObject relationships, Experiment experiment) {
        experiment.setCreatedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));
        experiment.setEditedBy(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_EDITED_BY), null)));
        experiment.setOwner(new UserReference(RestHelper.parseString(RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_OWNER), null)));
    }

    /**
     * Parses the list of ancestor entities from the experiment's relationships and adds them to the experiment.
     *
     * <p>For each ancestor entity ID found in the JSON structure, the method fetches the corresponding
     * full entity data from the server, parses it into a {@link SignalsEntity}, and attaches it to the
     * experiment as an ancestor. The most recent ancestor (last in the list) is also set as the {@code ancestorId}.
     *
     * @param relationships the 'relationships' JSON object of the experiment
     * @param experiment    the {@link Experiment} to which the ancestors will be attached
     */
    private void parseAncestors(JsonObject relationships, Experiment experiment) {
        if (!relationships.has(RestHelper.ATTR_ANCESTORS)) {
            logger.warn("ExperimentRestService: -> parseAncestors() -> No 'ancestors' in relationships");
            return;
        }
        JsonObject ancestorObj = relationships.getAsJsonObject(RestHelper.ATTR_ANCESTORS);
        if (!ancestorObj.has(RestHelper.ATTR_DATA) || !ancestorObj.get(RestHelper.ATTR_DATA).isJsonArray()) {
            logger.warn("ExperimentRestService: -> parseAncestors() -> 'ancestors' block missing or invalid");
            return;
        }

        JsonArray dataArray = ancestorObj.getAsJsonArray(RestHelper.ATTR_DATA);
        List<SignalsEntity> entities = new ArrayList<>();

        for (JsonElement element : dataArray) {
            try {
                JsonObject ancestorJson = element.getAsJsonObject();
                String id = RestHelper.parseString(ancestorJson, RestHelper.ATTR_ID);

                if (id != null) {
                    JsonElement seJson = fetchExperiment(RECEIVE_EXPERIMENT_ENDPOINT, id);
                    SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
                    entities.add(se);
                    experiment.addAncestor(se);
                }
            } catch (Exception e) {
                logger.warn("ExperimentRestService: -> parseAncestors() -> Failed to parse ancestor: {}", e.getMessage());
            }
        }

        if (!entities.isEmpty()) {
            experiment.setAncestorId(entities.get(entities.size() - 1).getId());
        }
    }

    /**
     * Parses the list of child entities from the experiment's relationships and adds them to the experiment.
     *
     * <p>This method extracts all child entity references from the JSON, then fetches and parses each child
     * into a {@link SignalsEntity}, which is added to the experiment's internal child list.
     *
     * @param relationships the 'relationships' JSON object of the experiment
     * @param experiment    the {@link Experiment} object to which the children will be attached
     */
    private void parseChildren(JsonObject relationships, Experiment experiment) {
        if (!relationships.has(RestHelper.ATTR_CHILDREN)) {
            logger.warn("ExperimentRestService: -> parseChildren() -> No 'children' in relationships");
            return;
        }

        JsonObject children = relationships.getAsJsonObject(RestHelper.ATTR_CHILDREN);
        if (!children.has(RestHelper.ATTR_DATA) || !children.get(RestHelper.ATTR_DATA).isJsonArray()) {
            logger.warn("ExperimentRestService: -> parseChildren() -> 'children' block missing or invalid");
            return;
        }

        JsonArray dataArray = children.getAsJsonArray(RestHelper.ATTR_DATA);
        for (JsonElement element : dataArray) {
            try {
                JsonObject childJson = element.getAsJsonObject();
                String id = RestHelper.parseString(childJson, RestHelper.ATTR_ID);
                if (id != null) {
                    JsonElement seJson = fetchExperiment(RECEIVE_EXPERIMENT_ENDPOINT, id);
                    SignalsEntity se = signalsEntityRestService.parseReply(seJson).createEntity();
                    experiment.addChild(se);
                }
            } catch (Exception e) {
                logger.warn("ExperimentRestService: -> parseChildren() -> Failed to parse child entity: {}", e.getMessage());
            }
        }
    }


    /**
     * Parses the system template relationship of the experiment and sets its template ID.
     *
     * <p>This method accesses the 'systemTemplate' → 'data' → 'id' field from the relationships
     * JSON and sets the extracted ID as the template reference for the experiment.
     *
     * @param relationships the 'relationships' JSON element containing the system template data
     * @param experiment    the {@link Experiment} to assign the template ID to
     */
    private void parseTemplate(JsonObject relationships, Experiment experiment) {
        try {
            if (!relationships.has(RestHelper.ATTR_SYSTEM_TEMPLATE)) {
                logger.warn("ExperimentRestService: -> parseTemplate() -> No 'systemTemplate' in relationships");
                return;
            }

            JsonObject systemTemplate = relationships.getAsJsonObject(RestHelper.ATTR_SYSTEM_TEMPLATE);
            if (!systemTemplate.has(RestHelper.ATTR_DATA)) {
                logger.warn("ExperimentRestService: -> parseTemplate() -> 'systemTemplate' has no 'data' block");
                return;
            }

            JsonObject data = systemTemplate.getAsJsonObject(RestHelper.ATTR_DATA);
            if (data.has(RestHelper.ATTR_ID)) {
                experiment.setTemplateId(data.get(RestHelper.ATTR_ID).getAsString());
            } else {
                logger.warn("ExperimentRestService: -> parseTemplate() -> 'systemTemplate.data' has no 'id'");
            }

        } catch (Exception e) {
            logger.warn("ExperimentRestService: -> parseTemplate() -> Failed to parse systemTemplate: {}", e.getMessage());
        }
    }

    /**
     * Fetches and parses the list of property definitions for a given experiment template.
     *
     * <p>This method uses the experiment's {@code templateId} to request the metadata of all
     * properties defined by its template. The response is parsed and added to the experiment as
     * {@link ExperimentProperty} objects.
     *
     * @param experiment the {@link Experiment} instance whose properties are to be populated
     */
    public void doGetExperimentProperties(Experiment experiment) {
        JsonElement json = fetchExperiment(EXPERIMENT_GET_PROPERTIES_ENDPOINT, experiment.getTemplateId());

        Iterator<JsonElement> iter = json.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement experimentPropertiesObject = iter.next();
            parseExperimentProperties(experiment, experimentPropertiesObject);
        }
    }

    /**
     * Parses a single experiment property definition JSON object and adds it to the experiment.
     *
     * <p>Extracts ID, name, and type from the given JSON structure and maps it into
     * an {@link ExperimentProperty} instance, which is added to the given {@link Experiment}.
     *
     * @param experiment                 the experiment to add the property to
     * @param experimentPropertiesObject the JSON object representing a property definition
     */
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

    /**
     * Fetches and parses all property values of the given experiment instance.
     *
     * <p>This method requests the actual values assigned to properties for the given experiment
     * (identified by its ID), and maps them into {@link ExperimentPropertyValue} objects,
     * which are added to the experiment instance.
     *
     * @param experiment the {@link Experiment} to populate with property values
     */
    public void doGetExperimentPropertyValues(Experiment experiment) {
        JsonElement json = fetchExperiment(EXPERIMENT_GET_PROPERTY_VALUES_ENDPOINT, experiment.getId());

        Iterator<JsonElement> iter = json.getAsJsonArray().iterator();
        while (iter.hasNext()) {
            JsonElement experimentPropertyValuesObject = iter.next();
            parseExperimentPropertyValues(experiment, experimentPropertyValuesObject);
        }
    }

    /**
     * Parses a single property value entry and adds it to the corresponding property in the experiment.
     *
     * <p>Matches the property name from the JSON attributes to the experiment's property list.
     * If a match is found, a new {@link ExperimentPropertyValue} is created and added to the experiment.
     * If either the property name or value is missing, the field is defaulted to an empty string.
     *
     * @param experiment                 the {@link Experiment} instance to update
     * @param experimentPropertiesObject the JSON element containing a property value
     */
    private void parseExperimentPropertyValues(Experiment experiment, JsonElement experimentPropertiesObject) {
        JsonObject propertiesObject = experimentPropertiesObject.getAsJsonObject();
        JsonObject attributes = propertiesObject.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();


        String name = Optional.ofNullable(attributes.get(RestHelper.ATTR_NAME))
                .map(JsonElement::getAsString)
                .orElse("");

        String value = Optional.ofNullable(attributes.get(RestHelper.ATTR_VALUE))
                .map(JsonElement::getAsString)
                .orElse("");

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

    /**
     * Appends a chemical structure (in CDXML format) as a reaction component
     * to a specified {@code chemicalDrawing} entity in the Signals platform.
     *
     * <p>The structure is added to the reaction diagram in the given position,
     * which can be one of: {@code reactants}, {@code products}, {@code reagents}, or {@code grid}.
     *
     * <p>The method constructs the appropriate API endpoint, builds the JSON request body,
     * and sends a POST request to the Signals REST API.
     *
     * <p>The request is made with {@code force=true} to bypass digest checks.
     *
     * @param chemicalDrawingEid the EID of the target {@code chemicalDrawing} entity
     * @param position           the reaction position to append the structure to
     *                           (valid values: {@code reactants}, {@code products}, {@code reagents}, {@code grid})
     * @param cdxmlString        the CDXML-formatted chemical structure to be appended
     * @throws RuntimeException if the REST call fails or an error occurs while building the request
     */
    public void addReactionToExperiment(String chemicalDrawingEid, String position, String cdxmlString) {
        JsonObject request = prepareReactionAppendJson(cdxmlString);
        String encodedEid = URLEncoder.encode(chemicalDrawingEid, StandardCharsets.UTF_8);
        String endpoint = String.format(CHEMICAL_DRAWINGS_ADD_REACTION_CDXML, encodedEid, position);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(endpoint)
                    .setRequestData(request.toString())
                    .putUriParameter("force", "true")
                    .execute(RestClient.HTTP_CREATED);

            JsonElement response = JsonParser.parseString(restClient.getResponse().getString());
            logger.info("Reaction append response = {}", response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to append reaction to chemicalDrawing", e);
        }
    }

    /**
     * Prepares the JSON payload for appending a chemical structure in CDXML format
     * to a reaction via the Signals REST API.
     *
     * <p>The resulting JSON structure follows this format:
     * <pre>
     * {
     *   "data": {
     *     "attributes": {
     *       "dataType": "cdxml",
     *       "data": "<CDXML content>"
     *     }
     *   }
     * }
     * </pre>
     *
     * @param cdxmlString the CDXML string to be embedded in the request body
     * @return a {@link JsonObject} representing the request payload
     */
    private JsonObject prepareReactionAppendJson(String cdxmlString) {
        JsonObject root = new JsonObject();
        JsonObject data = new JsonObject();
        JsonObject attributes = new JsonObject();

        attributes.addProperty(RestHelper.ATTR_DATA_TYPE, "cdxml");
        attributes.addProperty(RestHelper.ATTR_DATA, cdxmlString);

        data.add(RestHelper.ATTR_ATTRIBUTES, attributes);
        root.add(RestHelper.ATTR_DATA, data);

        return root;
    }
}
