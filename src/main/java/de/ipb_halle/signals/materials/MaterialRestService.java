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
package de.ipb_halle.signals.materials;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.field.FieldValuesParser;
import de.ipb_halle.signals.rest.*;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * REST service for materials
 */

@Local
public class MaterialRestService implements RestReplyParser<Material> {

    /*
     * we use the /materials/eid endpoint instead of the /entities/eid
     * endpoint because it directly delivers the library Id (assetType id)
     * for materials and the library Id and material Id for batches. The
     * same information would be available from the /entities/eid endpoint
     * but only after parsing the ancestor ids.
     */
    public final String MATERIAL_ENDPOINT = "/materials/%s";
    public final String MATERIAL_ATTACHMENT_ENDPOINT = "/materials/%s/attachments/%s";
    public final String MATERIAL_DRAWING_ENDPOINT = "/materials/%s/drawing";
    public final String MATERIAL_IMAGE_ENDPOINT = "/materials/%s/image";
    public final String MATERIAL_SEQUENCE_ENDPOINT = "/materials/%s/bioSequence";
    public final String MATERIAL_PROPERTIES_ENDPOINT = "/materials/%s/properties";

    @Inject
    private RestClient restClient;

    private Logger logger = LoggerFactory.getLogger(MaterialRestService.class);

    public Material parseReply(JsonElement json) {
        Material material = new Material();
        /**
         * link to materialJson -> materials/{eid}/data
         */
        JsonObject materialJson = json.getAsJsonObject();
        JsonObject attributes = materialJson.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        material.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
        material.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        material.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        material.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));
        material.setLibraryId(RestHelper.parseString(attributes, Material.ATTR_ASSET_TYPE_ID));
        material.setLibraryName(RestHelper.parseString(attributes, RestHelper.ATTR_LIBRARY));

        EntityType entityType = EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE));

        SignalsEntityRestService.parseTimestamps(attributes, material);
        SignalsEntityRestService.parseRelationships(materialJson, material);

        parseFields(attributes, material);
        parseSynonyms(attributes, material);
        return material;
    }

    private void parseFields(JsonObject library, Material material) {
        /**
         * example of library:
         * {"library":"Compounds","assetTypeId":"6215104dab0ad27bf7942a45","assetId":"66e80ed8ebc08a375f43022d","id":"asset:66e80ed8ebc08a375f43022d",
         * "eid":"asset:66e80ed8ebc08a375f43022d","name":"Compound000010","synonyms":["Essigsäureethylester"],"description":"",
         * "createdAt":"2024-09-16T10:56:24.746Z","editedAt":"2024-09-16T10:56:24.746Z","type":"asset","digest":"49706234",
         * "fields":{"CAS Number":{"value":"141-78-6"},"Chemical Name":{"value":"ethyl acetate"},"Description":{"value":""},
         * "Exact Mass":{"value":"88.05243"},"Material Library Type":{"value":"Compounds"},"Materials Access":{"value":["IPB"]},
         * "Molecular Formula":{"value":"C<sub>4</sub>H<sub>8</sub>O<sub>2</sub>"},"Molecular Weight":{"value":"88.11 g/mol"},
         * "Name":{"value":"Compound000010"}},"flags":{"canTrash":true}}
         */
        FieldValuesParser fieldValuesParser = new FieldValuesParser();
        /**
         * fieldValueList contains values of field title/value pair e.g.:
         * they are : "141-78-6", "ethyl acetate" from JSON OBJECT fieldTitleValuePair, which looks like:
         * "CAS Number":{"value":"141-78-6"},"Chemical Name":{"value":"ethyl acetate"}, etc.
         */
        List<FieldValue> fieldValueList = new ArrayList<>();

        try {
            //parses fields from library json, received from material/libraries/attributes/assets/fields
            JsonObject fieldTitleValuePair = library.getAsJsonObject(RestHelper.ATTR_FIELDS);

            //loop through field object and extract key/value pars from each field
            fieldValueList = fieldValuesParser.parseReply(fieldTitleValuePair);
            // fieldValueList.forEach(fieldValue -> logger.info("This is a field value: '{}'\n", fieldValue.toString()));

        } catch (IllegalStateException e) {
            logger.error("Error parsing field values. JSON: {}", library.get(RestHelper.ATTR_FIELDS), e);
            throw e;
        }
        //logger.info("Material title {}", material.getName());

        for (FieldValue fieldValue : fieldValueList) {
            fieldValue.setEntityId(material.getId());

        }
        material.addAllFieldValues(fieldValueList);
    }

    private void parseSynonyms(JsonObject attributes, Material material) {
        if (attributes.has(Material.ATTR_SYNONYMS)) {
            JsonArray array = attributes.getAsJsonArray(Material.ATTR_SYNONYMS);
            Iterator<JsonElement> iter = array.iterator();
            while (iter.hasNext()) {
                Synonym synonym = new Synonym(material.getId(), iter.next().getAsString());
                material.addSynonym(synonym);
            }
        }
    }

    private JsonElement fetch(String id) {
        JsonElement jsonResult;
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setEndpoint(String.format(MATERIAL_ENDPOINT, id))
                    .execute();

            jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code", ue);
        } catch (URISyntaxException me) {
            logger.warn("Malformed URL", me);
        } catch (IOException ioe) {
            logger.warn("IOException", ioe);
        }
        return null;
    }

    /**
     * Actually do a REST call to obtain a single attachment
     * @param endpoint the specific endpoint with id
     * @param contentType MIME type of the attachment
     * @return path of the received attachment in the staging area
     */
    private RestReply fetchAttachment(String endpoint, String contentType) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setContentType(contentType)
                    .setResponseType(RestClient.RestType.STREAM)
                    .setEndpoint(endpoint)
                    .execute();
            return restClient.getResponse();
        } catch (UnexpectedResponseCodeException e) {
            // attachment (drawing, image, sequence) may not be available
            logger.debug("Unexpected response code {}", restClient.getResponseCode(), e);
        } catch (IOException e) {
            logger.warn("caught IOException: ", e);
        } catch (URISyntaxException e) {
            logger.warn("URISyntaxException: ", e);
        }
        return null;
    }

    public Material parseFieldAttachments(Material material, JsonElement resultJson) {
        JsonObject resultObject = resultJson.getAsJsonObject();
        logger.info("result object field attachment", resultObject);

        FieldValue fieldValue = new FieldValue();

        Set<FieldValue> updatedFieldValues = material.getFieldValues();
        updatedFieldValues.add(fieldValue);
        return material;
    }

    /**
     * Obtain a material attachment as an octet stream. Chemical drawings,
     * images and sequences require special handling.
     * @param material
     * @param field
     * @return path of the downloaded attachment in the staging area
     */
    public RestReply doGetMaterialAttachment(Material material, Field field) {
        String endpoint = String.format(MATERIAL_ATTACHMENT_ENDPOINT, material.getId(), field.getId());
        return fetchAttachment(endpoint, RestClient.APPLICATION_OCTET);
    }

    public List<RestReply> doGetMaterialDrawing(Material material) {
        List<RestReply> result = new ArrayList<>();
        for (String type : new String[] {RestClient.CHEMICAL_CDXML,
                RestClient.CHEMICAL_MOL3000,
                RestClient.CHEMICAL_SVG } ) {
            RestReply attachment = fetchAttachment(String.format(MATERIAL_DRAWING_ENDPOINT, material.getId()),
                    type);
            if (attachment != null) {
                result.add(attachment);
            } else {
                break;
            }
        }
        return result;
    }

    public RestReply doGetMaterialImage(Material material) {
        return fetchAttachment(String.format(MATERIAL_IMAGE_ENDPOINT, material.getId()),
                        RestClient.APPLICATION_OCTET);
    }

    public List<RestReply> doGetMaterialSequence(Material material) {
        List<RestReply> sequences = new ArrayList<>();
        for (String type : new String[] {RestClient.SEQUENCE_GENBANK, RestClient.SEQUENCE_FASTA } ) {
            RestReply attachment = fetchAttachment(String.format(MATERIAL_SEQUENCE_ENDPOINT, material.getId()),
                    type);
            if (attachment != null) {
                sequences.add(attachment);
            } else {
                break;
            }
        }
        return sequences;
    }

    public Material doGetMaterial(String id) {
        return parseReply(fetch(id));
    }
}
