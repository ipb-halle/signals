// Refactored AdoRestService.java
// Main goals:
// - Clean Code principles
// - Better naming and responsibility separation
// - Remove unused endpoint (/entities/{eid}/properties)
// - Group responsibilities logically

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
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * REST service for interacting with ADO (Abstract Data Object) entities in the Signals platform.
 * <p>
 * Provides methods for creating new ADOs, retrieving existing ones, parsing JSON replies from the
 * Signals REST API, fetching template field definitions, and mapping REST data to domain objects.
 * </p>
 *
 * <p>Main responsibilities:</p>
 * <ul>
 *   <li>Create missing ADOs in bulk or individually</li>
 *   <li>Fetch ADOs and their relationships via REST calls</li>
 *   <li>Parse JSON responses into {@link Ado} domain objects</li>
 *   <li>Fetch and apply template field definitions to ADOs</li>
 *   <li>Maintain separation between JSON parsing and REST communication</li>
 * </ul>
 *
 * <p>Implements {@link RestReplyParser} for Ado objects, enabling standardized parsing logic.</p>
 *
 * <p>Depends on:</p>
 * <ul>
 *   <li>{@link RestClient} — low-level HTTP client for REST communication</li>
 *   <li>{@link DynEnumManager} — for converting string-based type fields to enum instances</li>
 *   <li>{@link SignalsEntityRestService} — for parsing related Signals entities</li>
 * </ul>
 */
@LocalBean
@Stateless
public class AdoRestService implements RestReplyParser<Ado> {

    private static final Logger logger = LogManager.getLogger(AdoRestService.class);

    private static final String ENDPOINT_GET_ADO = "/entities/%s";
    private static final String ENDPOINT_CREATE_ADO = "/entities?force=true";
    private static final String ENDPOINT_GET_TEMPLATE_FIELDS = "/entities/templates/%s/fields";
    private static final String ENDPOINT_GET_PROPERTIES = "/entities/%s/properties?force=true";

    @Inject
    private RestClient restClient;
    @Inject
    private DynEnumManager dynEnumManager;
    @Inject
    private SignalsEntityRestService signalsEntityRestService;


    public List<Ado> createAdosRange(int startInclusive, int endInclusive, String templateId) {
        if(endInclusive< startInclusive) return Collections.emptyList();

        List<Ado> generated = new ArrayList<>(endInclusive -startInclusive +1);
        for (int n = startInclusive; n <= endInclusive; n++) {
            String code = String.format("IPB_%06d", n);
            Ado created = createSingleAdo(code, templateId);
            if (created != null) {
                generated.add(created);
            } else {
                logger.warn("Failed to create ADO for code {}", code);
            }
        }
        return generated;
    }

    /**
     * Creates a single ADO with the given code and template ID.
     * <p>
     * Builds the request JSON payload, executes the POST request, and parses the reply into an {@link Ado} object.
     * </p>
     *
     * @param code       Unique IPB code for the ADO.
     * @param templateId Template ID to assign.
     * @return The created {@link Ado}, or {@code null} if creation failed validation.
     * @throws RuntimeException if the REST call fails or the reply cannot be parsed.
     */
    private Ado createSingleAdo(String code, String templateId) {
        Ado ado = new Ado();
        ado.setTemplateId(templateId);
        JsonObject request = buildAdoJsonPayload(ado, code);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(ENDPOINT_CREATE_ADO)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);

            JsonObject data = JsonParser.parseString(restClient.getResponse().getString())
                    .getAsJsonObject()
                    .getAsJsonObject(RestHelper.ATTR_DATA);

            Ado created = parseReply(data);
            if (created == null || created.getEid() == null || created.getType() == null) {
                logger.error("Invalid ADO created: {}", created);
                return null;
            }
            return created;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create ADO", e);
        }
    }

    /**
     * Builds the JSON payload for creating an ADO via the Signals REST API.
     *
     * @param ado  Ado instance with basic attributes set.
     * @param code IPB code for the ADO.
     * @return JSON object ready to be sent as the request body.
     */
    private JsonObject buildAdoJsonPayload(Ado ado, String code) {
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, Ado.ENTITY_TYPE_ADO);

        JsonObject meta = new JsonObject();
        meta.addProperty("adoTypeName", "IPB_Code");
        data.add("meta", meta);

        data.add(RestHelper.ATTR_ATTRIBUTES, buildAttributes(code));
        data.add(RestHelper.ATTR_RELATIONSHIPS, new JsonObject());

        JsonObject root = new JsonObject();
        root.add(RestHelper.ATTR_DATA, data);
        return root;
    }

    /**
     * Constructs the 'attributes' JSON object containing core fields for a new ADO.
     *
     * @param code IPB code for the ADO.
     * @return JSON object with 'name', 'IPB_Code', and 'Description' fields.
     */
    private JsonObject buildAttributes(String code) {
        JsonObject attributes = new JsonObject();
        String name = code;

        attributes.addProperty(RestHelper.ATTR_NAME, name);

        JsonObject fields = new JsonObject();
        fields.add("Name", propertyValue(name));
        fields.add("IPB_Code", propertyValue(code));
        fields.add("Description", propertyValue("Autogenerated ADO"));

        attributes.add("fields", fields);
        return attributes;
    }

    /**
     * Helper method to wrap a string value in the JSON format expected for Signals field values.
     *
     * @param value Field value.
     * @return JSON object containing the 'value' property.
     */
    private JsonObject propertyValue(String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        return obj;
    }

    /**
     * Retrieves an ADO by its EID from the Signals REST API.
     *
     * @param eid Entity identifier (EID) of the ADO.
     * @return Parsed {@link Ado} object.
     * @throws RuntimeException if the REST call or parsing fails.
     */
    public Ado doGetAdo(String eid) {
        JsonElement data = fetchJson(String.format(ENDPOINT_GET_ADO, eid));
        try {
            return parseReply(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ADO", e);
        }
    }

    /**
     * Executes a GET request to the given endpoint and parses the 'data' element from the JSON response.
     *
     * @param endpoint REST endpoint relative to the base URL.
     * @return JSON element representing the 'data' node.
     * @throws RuntimeException if the request fails or the response cannot be parsed.
     */
    private JsonElement fetchJson(String endpoint) {
        try {
            restClient.reset().setMethod(Method.GET).setEndpoint(endpoint).execute();
            return JsonParser.parseString(restClient.getResponse().getString())
                    .getAsJsonObject()
                    .get(RestHelper.ATTR_DATA);
        } catch (IOException | URISyntaxException | UnexpectedResponseCodeException e) {
            throw new RuntimeException("Failed to fetch JSON from: " + endpoint, e);
        }
    }

    /**
     * Parses a JSON element representing an ADO into a fully initialized {@link Ado} object.
     *
     * @param j JSON element containing ADO data.
     * @return The parsed {@link Ado} instance, or {@code null} if the input is invalid.
     * @throws Exception if parsing fails.
     */
    @Override
    public Ado parseReply(JsonElement j) throws Exception {
        if (j == null || !j.isJsonObject()) return null;

        JsonObject root = j.getAsJsonObject();
        Ado ado = new Ado();

        JsonObject attributes = root.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        if (attributes != null) {
            ado.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
            ado.setEid(RestHelper.parseString(attributes, RestHelper.ATTR_EID));
            ado.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
            ado.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
            ado.setCreatedAt(RestHelper.parseDate(attributes, RestHelper.ATTR_CREATED_AT));
            ado.setState(RestHelper.parseString(attributes, RestHelper.ATTR_STATE));

            String typeStr = RestHelper.parseString(attributes, RestHelper.ATTR_TYPE);
            if (typeStr != null) {
                ado.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(typeStr)));
            }

            if (attributes.has(RestHelper.ATTR_FIELDS)) {
                parseFieldValues(attributes.getAsJsonObject(RestHelper.ATTR_FIELDS), ado);
            }
        }

        JsonObject relationships = root.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);
        if (relationships != null) {
            parseRelationships(relationships, ado);
        }

        return ado;
    }

    /**
     * Parses the 'fields' section of an ADO JSON object, creating {@link AdoPropertyValue} instances
     * and populating the {@link Ado}'s property values.
     *
     * @param fieldsJson JSON object containing field name/value mappings.
     * @param ado        Target {@link Ado} object to populate.
     */
    private void parseFieldValues(JsonObject fieldsJson, Ado ado) {
        logger.info("ADO_REST_SERVICE_PARSE_FIELDS:=> {}\n", fieldsJson.toString());
        Set<AdoPropertyValue> values = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : fieldsJson.entrySet()) {
            //logger.info("ADO_REST_SERVICE_PARSSE_FIELDS:=> ENTRY.getKey() ={}, ENTRY.getValue()={}\n", entry.getKey(), entry.getValue());
            String fieldName = entry.getKey(); // e.g. Name, Description, IPB_Code
            JsonObject field = entry.getValue().getAsJsonObject(); // e.g. {"value":"IPB_000001"} for field IPB_Code (auto generated)

            if (field.has("value")) {

                String val = field.get("value").getAsString();
                //logger.info("ARS-> VALUE OF FIELD AS STRING = {}\n ", val); // e.g String IPB_000001 or name like fff
                if (fieldName.equalsIgnoreCase("IPB_Code")) {
                    ado.setIpbCode(val);
                }
                AdoPropertyValue apv = new AdoPropertyValue();
                apv.setAdoId(ado.getId());
                apv.setPropertyId(fieldName); // Later mapped to real ID
                apv.setPropertyValue(val);
                values.add(apv);
            }
        }
        ado.setPropertyValues(values);
    }

    /**
     * Parses the 'relationships' section of an ADO JSON object, including:
     * <ul>
     *     <li>Created by user reference</li>
     *     <li>Ancestor entities</li>
     *     <li>Child entities</li>
     *     <li>System template</li>
     * </ul>
     *
     * @param relationships JSON object with relationship data.
     * @param ado           Target {@link Ado} to populate.
     */
    private void parseRelationships(JsonObject relationships, Ado ado) {
        ado.setCreatedBy(new UserReference(RestHelper.parseString(
                RestHelper.getPrimitiveFromPath(relationships, SignalsEntityDTO.ATTR_CREATED_BY), null)));

        if (relationships.has(RestHelper.ATTR_ANCESTORS)) {
            parseEntityReferences(relationships.getAsJsonObject(RestHelper.ATTR_ANCESTORS), ado::addAncestor, ado::setAncestorId);
        }
        if (relationships.has(RestHelper.ATTR_CHILDREN)) {
            parseEntityReferences(relationships.getAsJsonObject(RestHelper.ATTR_CHILDREN), ado::addChild, null);
        }
        if (relationships.has(RestHelper.ATTR_SYSTEM_TEMPLATE)) {
            JsonObject templateData = relationships.getAsJsonObject(RestHelper.ATTR_SYSTEM_TEMPLATE).getAsJsonObject(RestHelper.ATTR_DATA);
            if (templateData.has(RestHelper.ATTR_ID)) {
                ado.setTemplateId(templateData.get(RestHelper.ATTR_ID).getAsString());
            }
        }
    }

    /**
     * Parses an array of entity references and applies them to an ADO via provided consumers.
     *
     * @param relationship JSON object containing 'data' array of related entities.
     * @param consumer     Consumer to process each parsed {@link SignalsEntity}.
     * @param idSetter     Optional consumer to set the entity ID.
     */
    private void parseEntityReferences(JsonObject relationship, java.util.function.Consumer<SignalsEntity> consumer, java.util.function.Consumer<String> idSetter) {
        JsonArray data = relationship.getAsJsonArray(RestHelper.ATTR_DATA);
        for (JsonElement el : data) {
            logger.info("ARS => parseEntityReferences=> {}\n", el.toString());
            try {
                String id = RestHelper.parseString(el.getAsJsonObject(), RestHelper.ATTR_ID);
                JsonElement json = fetchJson(String.format(ENDPOINT_GET_ADO, id));
                SignalsEntity entity = signalsEntityRestService.parseReply(json).createEntity();
                consumer.accept(entity);
                if (idSetter != null) idSetter.accept(entity.getId());
            } catch (Exception e) {
                logger.warn("Failed to parse related entity", e);
            }
        }
    }

    /**
     * Fetches the template fields for the given ADO from the Signals REST API and
     * updates the ADO's {@link AdoProperty} list accordingly.
     *
     * @param ado The {@link Ado} whose template fields are to be fetched.
     */
    public void fetchAdoTemplateFields(Ado ado) {
        // Hier we receive an array consisting of 3 elements (3 properties: name, description, ipb_code)
        JsonElement json = fetchJson(String.format(ENDPOINT_GET_TEMPLATE_FIELDS, ado.getTemplateId()));
        //logger.info("ARS:=> fetchAdoTemplateFields = {}\n", json.toString());
        for (JsonElement field : json.getAsJsonArray()) {
            parseTemplateField(ado, field);
        }
    }

    /**
     * Parses a single template field definition and adds it as an {@link AdoProperty} to the ADO.
     * Also updates property values to reference the correct property IDs.
     *
     * @param ado          The {@link Ado} being updated.
     * @param fieldElement JSON element representing a template field definition.
     */
    private void parseTemplateField(Ado ado, JsonElement fieldElement) {
        JsonObject field = fieldElement.getAsJsonObject();
        JsonObject def = field.getAsJsonObject(RestHelper.ATTR_META).getAsJsonObject(RestHelper.ATTR_DEFINITION);
        JsonObject attr = field.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        AdoProperty prop = new AdoProperty();
        prop.setPropertyId(field.get(RestHelper.ATTR_ID).getAsString());
        prop.setPropertyName(attr.get(RestHelper.ATTR_NAME).getAsString());
        prop.setPropertyType(def.get(RestHelper.ATTR_TYPE).getAsString());
        prop.setTemplateId(ado.getTemplateId());

        ado.addProperty(prop);
        ado.getPropertyValues().stream()
                .forEach(apv -> {
                    if (apv.getPropertyId().equalsIgnoreCase(prop.getPropertyName())) {
                        apv.setPropertyId(prop.getPropertyId());
                    }
                });
    }


    // Its make no sense to make this call, because the id are the same as the name, and autogenerated field is not there (IPB_Code)

//    public void fetchAdoProperties(Ado ado) {
//        JsonElement json = fetchJson(String.format(ENDPOINT_GET_PROPERTIES, ado.getTemplateId()));
//        logger.info("ARS:=> fetchAdoProperties = {}\n", json.toString());
//        for (JsonElement field : json.getAsJsonArray()) {
//            parseAdoProperty(ado, field);
//        }
//    }
//
//    private void parseAdoProperty(Ado ado, JsonElement field) {
//        logger.info("ARS=>PARSE_PROPERTIES PROPERTY JSON = {}\n", field);
//    }
}
