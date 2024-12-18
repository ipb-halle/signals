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
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.*;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

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

        JsonObject materialJson = json.getAsJsonObject().get(RestHelper.ATTR_DATA).getAsJsonObject();
        JsonObject attributes = materialJson.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        material.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
        material.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        material.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        material.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));
        material.setLibraryId(RestHelper.parseString(attributes, Material.ATTR_ASSET_TYPE_ID));
        material.setLibraryName(RestHelper.parseString(attributes, RestHelper.ATTR_LIBRARY));

        EntityType entityType = EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE));

        SignalsEntityRestService.parseTimestamps(attributes, material);
        SignalsEntityRestService.parseRelationships(materialJson, material);

        parseSynonyms(attributes, material);
        return material;
    }

    /**
     * @param json
     * @param fields     all fields of a given material mapped by fieldId
     * @param definingId material id (asset-Id or batch-Id)
     * @return list of field values. Previously unknown Fields are assigned to
     * the field adHocField.
     */
    private List<FieldValue> parseFields(JsonElement json, Map<String, Field> fields, String definingId) {
        List<FieldValue> values = new ArrayList<>();
        JsonObject jsonObj = json.getAsJsonObject();
        if (jsonObj.has(RestHelper.ATTR_DATA)) {
            JsonArray data = jsonObj.get(RestHelper.ATTR_DATA).getAsJsonArray();
            for (JsonElement datum : data) {
                values.add(parseSingleField(datum, fields));
            }
        }
        return values;
    }

    /**
     *
     * @param json
     * @param fields field definitions by fieldId
     * @return the FieldValue
     */
    private FieldValue parseSingleField(JsonElement json, Map<String, Field> fields) {
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject meta = RestHelper.getFromPath(json, RestHelper.ATTR_META).getAsJsonObject();
        JsonObject attributes = RestHelper.getFromPath(json, RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        String id = RestHelper.parseString(attributes, RestHelper.ATTR_ID);
        String value = RestHelper.getAsJsonString(attributes, RestHelper.ATTR_VALUE);
        String name = RestHelper.parseString(attributes, RestHelper.ATTR_NAME);
        Field field = fields.get(id);

        FieldValue fieldValue = new FieldValue();
        fieldValue.setFieldId(id);
        fieldValue.setValue(value);
        fieldValue.setFieldTitle(name);
        if (field == null) {
            fieldValue.setAdHocField(parseFieldDefinition(meta));
        }
        return fieldValue;
    }

    private Field parseFieldDefinition(JsonObject json) {
        Field field = new Field();
        JsonObject definition = json.get(Field.ATTR_DEFINITION).getAsJsonObject();
        field.setId(RestHelper.parseString(definition, RestHelper.ATTR_ID));
        field.setTitle(RestHelper.parseString(definition, RestHelper.ATTR_NAME));
        field.setFieldType(FieldType.valueOf(RestHelper.parseString(definition, Field.ATTR_DATA_TYPE)));
        field.setHidden(RestHelper.parseBool(definition, Field.ATTR_HIDDEN, false));
        field.setCalculated(RestHelper.parseBool(definition, Field.ATTR_CALCULATED, false));
        field.setReadOnly(RestHelper.parseBool(definition, Field.ATTR_READ_ONLY, false));
        boolean required = RestHelper.parseBool(definition, Field.ATTR_REQUIRED, false)
                || RestHelper.parseBool(definition, Field.ATTR_MANDATORY, false);
        field.setRequired(required);
        field.setDefinedBy(RestHelper.parseString(definition, Field.ATTR_DEFINED_BY));
        return field;
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

    public void parseAttachmentRevisionInfo(AttachmentRevision revision, FieldValue value) {
        JsonObject json = JsonParser.parseString(value.getValue()).getAsJsonObject();

        revision.setOriginalName(RestHelper.parseString(json, Attachment.ATTR_FILE_NAME));
        revision.setMimeType(RestHelper.parseString(json, Attachment.ATTR_MIME_TYPE));
        revision.setFileId(RestHelper.parseString(json, Attachment.ATTR_FILE_ID));
        revision.setSize(RestHelper.parseLong(json, Attachment.ATTR_FILE_SIZE));
    }

    public String parseAttachmentMimeType(FieldValue value) {
        JsonObject json = JsonParser.parseString(value.getValue()).getAsJsonObject();
        return RestHelper.parseString(json, Attachment.ATTR_MIME_TYPE);
    }

    private JsonElement fetch(String endpoint, String id) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setEndpoint(String.format(endpoint, id))
                    .execute();

            return JsonParser.parseString(restClient.getResponse().getString());

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
     *
     * @param endpoint    the specific endpoint with id
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

    /**
     * Obtain a material attachment as an octet stream. Chemical drawings,
     * images and sequences require special handling.
     *
     * @param material
     * @param field
     * @return path of the downloaded attachment in the staging area
     */
    public RestReply doGetMaterialAttachment(Material material, Field field, String mimeType) {
        String endpoint = String.format(MATERIAL_ATTACHMENT_ENDPOINT, material.getId(), field.getId());
        return fetchAttachment(endpoint, mimeType);
    }

    public List<RestReply> doGetMaterialDrawing(Material material) {
        List<RestReply> result = new ArrayList<>();
        for (String type : new String[]{RestClient.CHEMICAL_CDXML,
                RestClient.CHEMICAL_MOL3000,
                RestClient.CHEMICAL_SVG}) {
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

    public List<RestReply> doGetMaterialSequence(Material material) {
        List<RestReply> sequences = new ArrayList<>();
        for (String type : new String[]{RestClient.SEQUENCE_GENBANK, RestClient.SEQUENCE_FASTA}) {
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

    public List<FieldValue> doGetMaterialProperties(String id, Map<String, Field> fields) {
        return parseFields(fetch(MATERIAL_PROPERTIES_ENDPOINT, id), fields, id);
    }

    public Material doGetMaterial(String id) {
        return parseReply(fetch(MATERIAL_ENDPOINT, id));
    }
}
