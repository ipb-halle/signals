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

import com.google.gson.*;
import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.storage.StorageService;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    public final String ASSET_CREATE_ENDPOINT = "/materials/%s/assets";
    public final String BATCH_CREATE_ENDPOINT = "/materials/%s/assets/%s/batches";
    public final String MATERIAL_ENDPOINT = "/materials/%s";
    public final String MATERIAL_ATTACHMENT_ENDPOINT = "/materials/%s/attachments/%s";
    public final String MATERIAL_DRAWING_ENDPOINT = "/materials/%s/drawing";
    public final String MATERIAL_IMAGE_ENDPOINT = "/materials/%s/image";
    public final String MATERIAL_SEQUENCE_ENDPOINT = "/materials/%s/bioSequence";
    public final String MATERIAL_PROPERTIES_ENDPOINT = "/materials/%s/properties";

    @Inject
    private RestClient restClient;

    @Inject
    private AttachmentRestService attachmentRestService;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private StorageService storageService;

    private Logger logger = LoggerFactory.getLogger(MaterialRestService.class);

    public Material parseReply(JsonElement json) {
        Material material = new Material();
        /**
         * link to materialJson -> materials/{eid}/data
         */
        JsonObject attributes = null;
        JsonObject materialJson = null;
        try {
            materialJson = json.getAsJsonObject().get(RestHelper.ATTR_DATA).getAsJsonObject();
            attributes = materialJson.get(RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.error("MRS:-> Invalid JSON structure: {}", e.getLocalizedMessage());
        }
        material.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
        material.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        material.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        material.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));
        material.setLibraryId(RestHelper.parseString(attributes, Material.ATTR_ASSET_TYPE_ID));

        SignalsEntityRestService.parseTimestamps(attributes, material);
        SignalsEntityRestService.parseRelationships(materialJson, material);

        EntityType entityType = (EntityType) dynEnumManager.valueOf(
                EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE)));
        material.setEntityType(entityType);
        if (entityType.equals(EntityType.valueOf(Material.ENTITY_TYPE_BATCH))) {
            parseMaterialId(attributes, material);
        } else {
            // no synonyms for batches
            parseSynonyms(attributes, material);
        }
        return material;
    }

    /**
     * Parse the material id for a given batch
     *
     * @param attributes JSON data provided by the REST call
     * @param material   the Material batch object
     */
    private void parseMaterialId(JsonObject attributes, Material material) {
        material.setMaterial(new MaterialReference()
                .setId(Material.MATERIAL_ASSET_PREFIX + RestHelper.parseString(attributes, Material.ATTR_ASSET_ID)));
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
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            if (jsonObj.has(RestHelper.ATTR_DATA)) {
                JsonArray data = jsonObj.get(RestHelper.ATTR_DATA).getAsJsonArray();
                for (JsonElement fieldJsonElement : data) {
                    //parsing of filed Value from fieldJsonElement
                    values.add(parseSingleField(fieldJsonElement, fields));
                }
            }
        } else if (json.isJsonArray()) {
            logger.warn("MRS:-> Unexpected JSON structure: Array received instead of Object. Parsing Json array.");
            JsonArray array = json.getAsJsonArray();
            for (JsonElement fieldJsonElement : array) {
                values.add(parseSingleField(fieldJsonElement, fields));
            }

        } else {
            logger.error("MRS:-> Invalid JSON structure: {}", json.toString());
        }

        return values;
    }

    /**
     * @param fieldJsonElement
     * @param fieldsByLibraryId field definitions by fieldId
     * @return the FieldValue
     */
    private FieldValue parseSingleField(JsonElement fieldJsonElement, Map<String, Field> fieldsByLibraryId) {
        JsonObject metaFieldJsonObject = RestHelper.getFromPath(fieldJsonElement, RestHelper.ATTR_META).getAsJsonObject();
        JsonObject attributes = RestHelper.getFromPath(fieldJsonElement, RestHelper.ATTR_ATTRIBUTES).getAsJsonObject();

        String id = RestHelper.ATTR_ASSET_TYPE + RestHelper.parseString(attributes, RestHelper.ATTR_ID);
        String value = RestHelper.getAsJsonString(attributes, RestHelper.ATTR_VALUE);
        String name = RestHelper.parseString(attributes, RestHelper.ATTR_NAME);
        Field field = fieldsByLibraryId.get(id);

        //generation a field value objects
        FieldValue fieldValue = new FieldValue();
        fieldValue.setFieldId(id);
        fieldValue.setValue(value);
        fieldValue.setFieldTitle(name);

        //ToDO the method is almost not in use and only for emergency case, if a new filed will be discovered
        if (field == null) {
            logger.trace("MRS:-> FIELD IS NULL !!!!! \n");
            //receiving a file information from material attachment->
            fieldValue.setField(parseFieldDefinition(metaFieldJsonObject));
        }

        return fieldValue;
    }

    /**
     * extraction of field definition from json element which is not defined in library field definitions
     *
     * @param metaFieldJsonObject
     * @return
     */
    private Field parseFieldDefinition(JsonObject metaFieldJsonObject) {
        Field field = new Field();
        JsonObject definition = metaFieldJsonObject.get(Field.ATTR_DEFINITION).getAsJsonObject();
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
            logger.warn("MRS:-> Unexpected code {}", ue.getMessage(), ue);
        } catch (URISyntaxException me) {
            logger.warn("MRS:-> Malformed URL {}", me.getMessage(), me);
        } catch (IOException ioe) {
            logger.warn("MRS:-> IOException {}", ioe.getMessage(), ioe);
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
        return attachmentRestService.fetchAttachment(endpoint, mimeType);
    }

    public List<RestReply> doGetMaterialDrawing(Material material) {
        List<RestReply> result = new ArrayList<>();
        for (String type : new String[]{RestClient.CHEMICAL_CDXML,
                RestClient.CHEMICAL_MOL3000,
                RestClient.CHEMICAL_SVG}) {
            RestReply attachment = attachmentRestService.fetchAttachment(String.format(MATERIAL_DRAWING_ENDPOINT, material.getId()),
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
            RestReply attachment = attachmentRestService.fetchAttachment(String.format(MATERIAL_SEQUENCE_ENDPOINT, material.getId()),
                    type);
            if (attachment != null) {
                sequences.add(attachment);
            } else {
                break;
            }
        }

        return sequences;
    }

    /**
     * @param materialId
     * @param fieldsByLibraryId
     * @return List<FieldValue>
     */
    public List<FieldValue> doGetMaterialProperties(String materialId, Map<String, Field> fieldsByLibraryId) {

        return parseFields(
                //returns JsonElement of material
                fetch(MATERIAL_PROPERTIES_ENDPOINT, materialId),
                fieldsByLibraryId,
                materialId);
    }

    public Material doGetMaterial(String id) {
        return parseReply(fetch(MATERIAL_ENDPOINT, id));
    }

    public Material doCreateMaterial(Library lib, Material mat, Material batch) {
        JsonObject request;
        String endpoint;

        if (mat.getEntityType().equals(EntityType.valueOf(Material.ENTITY_TYPE_ASSET))) {
            endpoint = String.format(ASSET_CREATE_ENDPOINT, lib.getName());
            request = prepareAsset(lib, mat, batch);
        } else {
            endpoint = String.format(BATCH_CREATE_ENDPOINT, lib.getName(), mat.getMaterial().getId());
            request = prepareBatch(lib, mat);
        }
        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(endpoint)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            Material result = parseReply(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));
            return result;
        } catch (UnexpectedResponseCodeException ue) {
            logger.warn("doCreateMaterial() got unexpected return code from API call: {}", request);
        } catch (URISyntaxException me) {
            logger.warn("doCreateMaterial() malformed URL");
        } catch (IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        logger.info(request.toString());
        return null;
    }

    private JsonObject prepareAsset(Library lib, Material mat, Material batch) {

        JsonObject attr = new JsonObject();
        attr.add(Material.ATTR_SYNONYMS, prepareSynonyms(mat));
        attr.add(RestHelper.ATTR_FIELDS, prepareFields(mat));
        if (batch == null) {
            attr.add(RestHelper.ATTR_RELATIONSHIPS, new JsonObject());
        } else {
            /*
             * batch is mandatory, if library is configured with batches (lots):
             * "relationships": {
             *      "batch": {
             *              "data":{
             *                      "type":"batch",
             *                      "id":"bc824377-682d-4bbd-bce5-c090e3b55f12",
             *                      "attributes": {
             *                              "fields": [
             *                                      {
             *                                              "id":"6215104dab0ad27bf7942a57",
             *                                              "value":"123456789"
             *                                      }
             *                              ]
             *                      }
             *              }
             *      }
             *  }
             */
            JsonObject jsonBatch = new JsonObject();
            jsonBatch.add(Material.ATTR_BATCH, prepareBatch(lib, batch));
            attr.add(RestHelper.ATTR_RELATIONSHIPS, jsonBatch);
        }

        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_ID, mat.getStrippedId());
        data.addProperty(RestHelper.ATTR_TYPE, mat.getEntityType().getValue());
        data.add(RestHelper.ATTR_ATTRIBUTES, attr);

        JsonObject asset = new JsonObject();
        asset.add(RestHelper.ATTR_DATA, data);
        return asset;
    }

    private JsonObject prepareBatch(Library lib, Material mat) {

        JsonObject attr = new JsonObject();
        attr.add(RestHelper.ATTR_FIELDS, prepareFields(mat));

        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_ID, mat.getStrippedId());
        data.addProperty(RestHelper.ATTR_TYPE, mat.getEntityType().getValue());
        data.add(RestHelper.ATTR_ATTRIBUTES, attr);

        JsonObject batch = new JsonObject();
        batch.add(RestHelper.ATTR_DATA, data);
        return batch;
    }

    /**
     * Prepare an array of synonyms.
     *
     * @param mat
     * @return
     */
    private JsonElement prepareSynonyms(Material mat) {
        JsonArray array = new JsonArray();
        for (Synonym synonym : mat.getSynonyms()) {
            array.add(synonym.getOption());
        }
        return array;
    }

    /**
     * prepare a JsonArray of field values for transmission.
     *
     * @param mat
     * @return array of required field values
     */
    private JsonElement prepareFields(Material mat) {
        JsonArray array = new JsonArray();
        for (FieldValue fieldValue : mat.getFieldValues()) {
            if (fieldValue.getField().getRequired()
                    || ((!fieldValue.getField().getCalculated())
                    && (!fieldValue.getField().getReadOnly()))) {
                array.add(prepareFieldValue(fieldValue));
            }
        }
        return array;
    }

    /**
     * Convert a single FieldValue to a JsonObject for transmission
     *
     * @param fieldValue
     * @return JsonObject ready representing this field value formatted for
     * transmission to the REST endpoint.
     */
    private JsonElement prepareFieldValue(FieldValue fieldValue) {
        JsonObject obj = new JsonObject();
        obj.addProperty(RestHelper.ATTR_ID, fieldValue.getFieldId());
        /*
         * ToDo: Handle Attachments including chemical drawings and
         * sequences. Include the base64 encoded attachment file data.
         */
        switch (fieldValue.getField().getFieldType().getValue().toUpperCase()) {
            case FieldType.ATTACHED_FILE:
            case FieldType.SEQUENCE_FILE:
                obj.add(RestHelper.ATTR_VALUE, prepareAttachment(fieldValue));
                // obj.add(RestHelper.ATTR_VALUE,
                //        JsonParser.parseString("{\"filename\":\"hello.txt\", \"base64\":\"SGFsbG8gV2VsdCEK\"}"));
                break;
            case FieldType.CHEMICAL_DRAWING:
                Attachment attachment = fieldValue.getAttachment();
                AttachmentRevision revision = attachment.getLatestRevision();
                String fileAsString = storageService.getFileAsString(attachment.getFiles(revision.getId()));
                obj.addProperty(RestHelper.ATTR_VALUE, fileAsString);
                break;
            default:
                // works, if fieldValue contains a simple String
                // probably won't work if fieldValue contains array, number, measurement
                // or otherwise complex value.
                obj.addProperty(RestHelper.ATTR_VALUE, fieldValue.getValue());
                ;
        }
        return obj;
    }

    /**
     * create a base64 representation of the attachment file
     *
     * @param fieldValue
     * @return
     */
    private JsonElement prepareAttachment(FieldValue fieldValue) {
        JsonObject attachmentData = new JsonObject();
        Attachment attachment = fieldValue.getAttachment();
        AttachmentRevision revision = attachment.getLatestRevision();
        // ToDo: check whether all lower case in attribute names is required!
        attachmentData.addProperty(Attachment.ATTR_filename, revision.getOriginalName());
        String base64 = storageService.getFileBase64(attachment.getFiles(revision.getId()));
        attachmentData.addProperty(Attachment.ATTR_BASE64, base64);
        return attachmentData;
    }
}
