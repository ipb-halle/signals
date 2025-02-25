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
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.rest.*;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;


/**
 * Rest service for location types
 */

@Local
public class LocationTypeRestService implements RestReplyParser<LocationType> {

    public final String INVENTORY_TYPES_ENDPOINT = "/inventory/types";
    public final static String LOCATION_TYPE_ENTITY_PREFIX = "location:";
    public final static String LOCATION_TYPE_ENTITY_SUFFIX = ":ivt";


    @Inject
    private RestClient restClient;

    @Inject
    private FieldParser fieldParser;

    @Inject
    private DynEnumManager dynEnumManager;


    public LocationType parseReply(JsonElement j) {
        LocationType lt = new LocationType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        lt.setId(LOCATION_TYPE_ENTITY_PREFIX + attributes.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString() + LOCATION_TYPE_ENTITY_SUFFIX);
        lt.setDescription(attributes.getAsJsonPrimitive(RestHelper.ATTR_DESCRIPTION).getAsString());
        lt.setName(attributes.getAsJsonPrimitive(LocationType.ATTR_NAME).getAsString());

        // parsing boolean values inUse and movable
        // 1) in Use
        JsonElement inUseEl = attributes.get(RestHelper.ATTR_IN_USE);
        if (inUseEl != null && !inUseEl.isJsonNull()) {
            lt.setInUse(inUseEl.getAsBoolean());
        } else {
            lt.setInUse(false);
        }

        // 2) movable
        JsonElement movableEl = attributes.get(RestHelper.ATTR_MOVABLE);
        if (movableEl != null && !movableEl.isJsonNull()) {
            lt.setMovable(movableEl.getAsBoolean());
        } else {
            lt.setMovable(false);
        }

        //parsing updatedAt and createdAt
        String createdAtStr = attributes.get(RestHelper.ATTR_CREATED_AT).getAsString();
        String updatedAtStr = attributes.get(RestHelper.ATTR_CREATED_AT).getAsString();
        Instant createdAt = Instant.parse(createdAtStr);
        Instant updatedAt = Instant.parse(updatedAtStr);
        lt.setCreatedAt(Date.from(createdAt));
        lt.setUpdatedAt(Date.from(updatedAt));

        if (attributes.has(RestHelper.ATTR_FIELDS)) {
            parseFields(attributes.getAsJsonArray(RestHelper.ATTR_FIELDS), lt);
        }

        return lt;
    }

    private void parseFields(JsonArray fields, LocationType lt) {
        Iterator<JsonElement> iter = fields.iterator();
        while (iter.hasNext()) {
            Field field = fieldParser.parseReply(iter.next());
            // NOTE: field ids are NOT unique within Signals Inventory
            field.setId(field.getId() + ":" + lt.getId());
            field.setDesignation((FieldDesignation) dynEnumManager.valueOf(FieldDesignation.valueOf(FieldDesignation.LOCATION)));
            field.setDefiningEntityId(lt.getSuffixPrefixId());
            lt.addField(field);
        }
    }

    public List<LocationType> doGetLocationTypes() {
        List<LocationType> locationTypes = new ArrayList<>();
        restClient.reset()
                .setMethod(Method.GET)
                .setEndpoint(INVENTORY_TYPES_ENDPOINT)
                .putUriParameter("entityType", LocationEntity.ENTITY_TYPE_LOCATION);

        RestResultIterator<LocationType> iter = new RestResultIterator<>(restClient, this, true);

        while (iter.hasNext()) {
            locationTypes.add(iter.next());
        }
        return locationTypes;
    }
}


