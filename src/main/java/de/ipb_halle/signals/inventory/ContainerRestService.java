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
package de.ipb_halle.signals.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.entity.FieldValueRestService;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;
import de.ipb_halle.signals.users.UserReference;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.ejb.Local;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Manager for signals containers (inventory/containers API endpoint) 
 */

@Local
public class ContainerRestService implements RestService<Container> {

    public final String CONTAINER_ENDPOINT = "/inventory/containers/%s";

    @Inject
    private RestClient restClient;

    private Logger logger = LoggerFactory.getLogger(ContainerRestService.class);
    
    public Container createEntity(JsonElement json) {
        JsonObject j = json.getAsJsonObject();
        JsonObject attributes = j.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        Container ct = new Container();
        ct.setId(RestHelper.parseString(j, RestHelper.ATTR_ID));
        ct.setBarcode(RestHelper.parseString(attributes, Container.ATTR_BARCODE));
        ct.setDigest(RestHelper.parseString(attributes, RestHelper.ATTR_DIGEST));
        ct.setContainerTypeId(RestHelper.parseString(attributes, Container.ATTR_CONTAINER_TYPE_ID));
        ct.setContainerTypeName(RestHelper.parseString(attributes, Container.ATTR_CONTAINER_TYPE_NAME));
        ct.setLocation(new LocationReference().setId(
            RestHelper.parseString(
            RestHelper.getPrimitiveFromPath(attributes, Container.ATTR_LOCATION_ID))));
        ct.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        ct.setUnit(Unit.getUnit(RestHelper.parseString(attributes, Container.ATTR_UNIT)));
        parseFieldValues(attributes.getAsJsonArray(Container.ATTR_FIELDS), ct);
        parseMaterials(attributes.getAsJsonArray(Container.ATTR_CONTENTS), ct);

        ct.setJsonString(j.toString());
        parseChangeRecords(j, ct);

        return ct;
    }


    private JsonElement fetch(String id) {
        try {
            restClient.setMethod(Method.GET)
                .setEndpoint(String.format(CONTAINER_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch(UnexpectedResponseCodeException ue) {
           logger.warn("Unexpected code");
        } catch(MalformedURLException me) {
            logger.warn("Malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException",  (Throwable) ioe);
        }
        return null;
    }

    public Container doGetContainer(String id) {
        return createEntity(fetch(id));
    }

    private void parseChangeRecords(JsonObject json, Container ct) {
        ct.setCreatedAt(RestHelper.parseDate(json, Container.ATTR_CREATED_AT));
        ct.setCreatedBy(new UserReference().setId(
                    RestHelper.parseInt(
                    RestHelper.getPrimitiveFromPath(json, Container.ATTR_CREATED_BY))));
        ct.setUpdatedAt(RestHelper.parseDate(json, Container.ATTR_UPDATED_AT));
        ct.setUpdatedBy(new UserReference().setId(
                    RestHelper.parseInt(
                    RestHelper.getPrimitiveFromPath(json, Container.ATTR_UPDATED_BY))));
    }


    private void parseFieldValues(JsonArray jArray, Container ct) {
        Iterator<JsonElement> iter = jArray.iterator();
        FieldValueRestService svc = new FieldValueRestService();
        while (iter.hasNext()) {
            ct.addFieldValue(svc.createEntity(iter.next()));
        }
    }

    private void parseMaterials(JsonArray jArray, Container ct) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            ct.addMaterial(
                        new MaterialReference()
                        .setId(
                        RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(
                        iter.next(),
                        Container.ATTR_CONTENT_ID))));
        }
    }
}
