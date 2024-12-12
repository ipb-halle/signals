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

import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.field.FieldValuesParser;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestReplyParser;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import jakarta.ejb.Local;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    @Inject
    private RestClient restClient;

    @Inject
    private SignalsEntityRestService entityRestService;

    @Inject
    private AttachmentRestService attachmentRestService;

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

        entityRestService.parseTimestamps(attributes, material);
        entityRestService.parseRelationships(materialJson, material);

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
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(MATERIAL_ENDPOINT, id))
                    .execute();

            jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code");
        } catch (URISyntaxException me) {
            logger.warn("Malformed URL");
        } catch (IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
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


    private String /*JsonElement*/ fetch(String materialId, String fieldId) {
        JsonElement jsonResult;
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(MATERIAL_ATTACHMENT_ENDPOINT, materialId, fieldId))
                    .execute();
            String response = restClient.getResponse();

            //  jsonResult = JsonParser.parseString(restClient.getResponse());
            //  return jsonResult.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);

            return response;
        } catch (UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code\n" + ue);
        } catch (URISyntaxException me) {
            logger.warn("Malformed URL\n" + me);
        } catch (IOException ioe) {
            logger.warn("IOException" + ioe);
        }
        return null;
    }

    public String /*Material*/ doGetFieldAttachments(Material material, String fieldId) {
        return fetch(material.getId(), fieldId);
        //return parseFieldAttachments(material, Objects.requireNonNull(fetch(material.getId(), fieldId)));
    }

    public Material doGetMaterial(String id) {
        return parseReply(fetch(id));
    }
}
