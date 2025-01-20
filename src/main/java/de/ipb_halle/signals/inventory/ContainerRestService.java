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
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Manager for signals containers (inventory/containers API endpoint)
 */

@Local
public class ContainerRestService implements RestReplyParser<Container> {

    public final String CONTAINER_ENDPOINT = "/inventory/containers/%s";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    private Logger logger = LoggerFactory.getLogger(ContainerRestService.class);

    @Override
    public Container parseReply(JsonElement json) {
        JsonObject j = json.getAsJsonObject();
        JsonObject attributes = j.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        JsonArray jsonArray = attributes.get(RestHelper.ATTR_FIELDS).getAsJsonArray();

        Container ct = new Container();
        ct.setId(RestHelper.parseString(j, RestHelper.ATTR_ID));
        ct.setBarcode(RestHelper.parseString(attributes, ContainerEntity.ATTR_BARCODE));
        ct.setDigest(RestHelper.parseString(attributes, RestHelper.ATTR_DIGEST));
        ct.setCreatedAt(RestHelper.parseDate(attributes, ContainerEntity.ATTR_CREATED_AT));
        ct.setContainerTypeId(RestHelper.parseString(attributes, ContainerEntity.ATTR_CONTAINER_TYPE_ID));
        ct.setContainerTypeName(RestHelper.parseString(attributes, ContainerEntity.ATTR_CONTAINER_TYPE_NAME));
        ct.setLocation(new LocationReference().setId(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(attributes, ContainerEntity.ATTR_LOCATION_ID))));
        ct.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        try {
            ct.setUnit(Unit.getUnit(RestHelper.parseString(attributes, ContainerEntity.ATTR_UNIT)));
        } catch (Exception e) {
            logger.error("ContainerRestService:-> Id of container for unit setting where error occurring is: {},", ct.getId(), e);

        }

       // parseFieldValues(attributes.getAsJsonArray(RestHelper.ATTR_FIELDS), ct);
        parseFieldValues(jsonArray, ct);
        parseMaterials(attributes.getAsJsonArray(ContainerEntity.ATTR_CONTENTS), ct);

        parseChangeRecords(j, ct);

        logger.info("ContainerRestService:-> Parsed container with ID={}", ct.getId());
        return ct;
    }


    private JsonElement fetch(String id) {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(CONTAINER_ENDPOINT, id))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException ue) {
            logger.error("ContainerRestService:-> Unexpected code for container ID={}", id, ue);
        } catch (URISyntaxException me) {
            logger.error("ContainerRestService:-> Malformed URL", me);
        } catch (IOException ioe) {
            logger.error("ContainerRestService:-> IOException", ioe);
        }
        return null;
    }

    public Container doGetContainer(String id) {
        JsonElement data = fetch(id);
        if (data == null) {
            logger.warn("ContainerRestService:-> No container found for ID={}", id);
            return null;
        }
        return parseReply(data);
    }

    private void parseChangeRecords(JsonObject json, Container ct) {
        ct.setCreatedAt(RestHelper.parseDate(json, ContainerEntity.ATTR_CREATED_AT));
        ct.setCreatedBy(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(json, ContainerEntity.ATTR_CREATED_BY))));
        ct.setUpdatedAt(RestHelper.parseDate(json, ContainerEntity.ATTR_UPDATED_AT));
        ct.setUpdatedBy(new UserReference(
                RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(json, ContainerEntity.ATTR_UPDATED_BY))));
    }


    private void parseFieldValues(JsonArray jArray, Container ct) {
        LocationTypeFieldValuesParser svc = new LocationTypeFieldValuesParser();
        ct.addFieldValues(svc.parseReply(jArray));
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
                                                    ContainerEntity.ATTR_CONTENT_ID))));
        }
    }
}
