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
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.entity.FieldDefinitionRestService;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.ejb.Local;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * REST service for material libraries
 */

@Local
public class LibraryRestService implements RestService<Library> {

    public final String MATERIALS_LIBRARIES_ENDPOINT = "/materials/libraries";

    @Inject
    private RestClient restClient;

    private Logger logger = LoggerFactory.getLogger(LibraryRestService.class.getName());

    public Library createEntity(JsonElement json) {
        Library lib = new Library();
        JsonObject j = json.getAsJsonObject();
        JsonObject attributes  = j.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        lib.setId(RestHelper.parseString(j, RestHelper.ATTR_ID));
        lib.setEnabled(RestHelper.parseBool(j, Library.ATTR_ENABLED));
        lib.setJsonString(j.toString());

        lib.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        if (attributes.has(Library.ATTR_ASSETS)) {
            parseAsset(attributes.getAsJsonObject(Library.ATTR_ASSETS), lib);
        }
        if (attributes.has(Library.ATTR_BATCHES)) {
            parseBatch(attributes.getAsJsonObject(Library.ATTR_BATCHES), lib);
        }
        parseChangeRecords(attributes, lib);

        lib.setDigest(RestHelper.parseString(attributes, RestHelper.ATTR_DIGEST));
        lib.setDisplayImage(RestHelper.getAsJsonString(attributes, Library.ATTR_DISPLAY_IMAGE));
        lib.setDisplayTable(RestHelper.getAsJsonString(attributes, Library.ATTR_DISPLAY_TABLE));
        lib.setEntityFlags(RestHelper.getAsJsonString(attributes, Library.ATTR_ENTITY_FLAGS));
        lib.setMaterialsSampleMapping(RestHelper.getAsJsonString(attributes, Library.ATTR_MATERIALS_SAMPLE_MAPPING));
        return lib;
    }
    
    private Iterator<JsonElement> fetch() {
        JsonElement jsonResult;
        try {
            restClient.setMethod(Method.GET)
                .setEndpoint(MATERIALS_LIBRARIES_ENDPOINT)
                .execute();

            jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA).iterator();

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code");
        } catch(MalformedURLException me) {
            logger.warn("Malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null; 
    }

    public List<Library> doGetLibraries() {
        List<Library> libraries = new ArrayList<> ();
        Iterator<JsonElement> iter = fetch();

        while(iter.hasNext()) {
            Library lib = createEntity(iter.next());
            libraries.add(lib);
        }
        return libraries;
    }

    private void parseAsset(JsonObject j, Library lib) {
        lib.setAssetDisplayName(RestHelper.parseString(j, Library.ATTR_ASSET_DISPLAY_NAME));
        lib.setAssetNameFieldId(RestHelper.parseString(j, Library.ATTR_ASSET_NAME_FIELD_ID));
        lib.setAssetNumberingFormat(RestHelper.parseString(RestHelper.getPrimitiveFromPath(j, Library.ATTR_NUMBERING_FORMAT)));

        if (j.has(Library.ATTR_ASSET_FIELDS)) {
            parseAssetFields(j.getAsJsonArray(Library.ATTR_ASSET_FIELDS), lib);
        }

        lib.setUniqueness(RestHelper.getAsJsonString(j, Library.ATTR_ASSET_UNIQUENESS));
    }

    private void parseAssetFields(JsonArray jArray, Library lib) {
        Iterator<JsonElement> iterator = jArray.iterator();
        FieldDefinitionRestService svc = new FieldDefinitionRestService();
        while (iterator.hasNext()) {
            lib.addAssetFieldDefinition(svc.createEntity(iterator.next()));
        }
    }

    private void parseBatch(JsonObject j, Library lib) {
        lib.setBatchDisplayName(RestHelper.parseString(j, Library.ATTR_BATCH_DISPLAY_NAME));
        lib.setBatchNumberingFormat(RestHelper.parseString(RestHelper.getPrimitiveFromPath(j, Library.ATTR_NUMBERING_FORMAT)));
        if (j.has(Library.ATTR_BATCH_FIELDS)) {
            parseBatchFields(j.getAsJsonArray(Library.ATTR_BATCH_FIELDS), lib);
        }
    }

    private void parseBatchFields(JsonArray jArray, Library lib) {
        Iterator<JsonElement> iterator = jArray.iterator();
        FieldDefinitionRestService svc = new FieldDefinitionRestService();
        while (iterator.hasNext()) {
            lib.addBatchFieldDefinition(svc.createEntity(iterator.next()));
        }
    }

    private void parseChangeRecords(JsonObject json, Library lib) {
        lib.setCreatedAt(RestHelper.parseDate(RestHelper.getPrimitiveFromPath(json, Library.ATTR_PATH_CREATED_AT)));
        lib.setCreatedBy(RestHelper.parseInt(RestHelper.getPrimitiveFromPath(json, Library.ATTR_PATH_CREATED_BY)));
        lib.setEditedAt(RestHelper.parseDate(RestHelper.getPrimitiveFromPath(json, Library.ATTR_PATH_EDITED_AT)));
        lib.setEditedBy(RestHelper.parseInt(RestHelper.getPrimitiveFromPath(json, Library.ATTR_PATH_EDITED_BY)));
    }
}
