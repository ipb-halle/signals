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
import de.ipb_halle.signals.entity.SignalsEntityDTO;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    @Inject
    private RestClient restClient;

    @Inject
    private SignalsEntityRestService entityRestService;

    private Logger logger = LoggerFactory.getLogger(MaterialRestService.class);

    public Material parseReply(JsonElement json) {
        Material mat = new Material();
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject attributes = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        mat.setId(RestHelper.parseString(attributes, RestHelper.ATTR_ID));
        mat.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        mat.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        mat.setDigest(RestHelper.parseLong(attributes, RestHelper.ATTR_DIGEST));
        mat.setLibraryId(RestHelper.parseString(attributes, Material.ATTR_ASSET_TYPE_ID));
        EntityType type = EntityType.valueOf(RestHelper.parseString(attributes, RestHelper.ATTR_TYPE));

        entityRestService.parseTimestamps(attributes, mat);
        entityRestService.parseRelationships(jsonObj, mat);
        parseSynonyms(attributes, mat);
        logger.info(mat.toString());
        parseFields(attributes, mat);

        return mat;
    }

    private void parseFields(JsonObject attributes, Material material) {
        FieldValuesParser fieldValuesParser = new FieldValuesParser();
        List<FieldValue> fieldValues = new ArrayList<>();
        try {
            //parse filed from jason attribute fields
            JsonObject fields = attributes.getAsJsonObject(RestHelper.ATTR_FIELDS);
            //loop through field object and extract key/value pars from each field
            fieldValues = fieldValuesParser.parseReply(fields);
        } catch (IllegalStateException e) {
            logger.error("Error parsing field values. JSON: {}", attributes.get(RestHelper.ATTR_FIELDS), e);
            throw e;
        }
        logger.info("Material title {}", material.getName());

        for (FieldValue fieldValue : fieldValues) {
            fieldValue.setEntityId(material.getId());

        }
        material.addAllFieldValues(fieldValues);
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

    public Material doGetMaterial(String id) {
        return parseReply(fetch(id));
    }
}
