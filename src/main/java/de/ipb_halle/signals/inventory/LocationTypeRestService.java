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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * Rest service for location types
 */

@Local
public class LocationTypeRestService implements RestReplyParser<LocationType> {

    public final String INVENTORY_TYPES_ENDPOINT = "/inventory/types";
    private Logger logger = LoggerFactory.getLogger(LocationTypeRestService.class);


    @Inject
    private RestClient restClient;

    @Inject
    private FieldParser fieldParser;

    @Inject
    private DynEnumManager dynEnumManager;


    public LocationType parseReply(JsonElement j) {
        LocationType lt = new LocationType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        lt.setId(attributes.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        lt.setDescription(attributes.getAsJsonPrimitive(RestHelper.ATTR_DESCRIPTION).getAsString());
        lt.setName(attributes.getAsJsonPrimitive(LocationType.ATTR_NAME).getAsString());

        if (attributes.has(RestHelper.ATTR_FIELDS)) {
            parseFields(attributes.getAsJsonArray(RestHelper.ATTR_FIELDS), lt);
        }

        return lt;
    }

    private void parseFields(JsonArray fields, LocationType lt) {
        Iterator<JsonElement> iter = fields.iterator();
        while (iter.hasNext()) {
            Field field = fieldParser.parseReply(iter.next());
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


