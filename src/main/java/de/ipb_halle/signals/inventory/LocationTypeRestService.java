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
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldOption;
import de.ipb_halle.signals.field.FieldType;
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
    private DynEnumManager dynEnumManager;


    public LocationType parseReply(JsonElement j) {
        LocationType lt = new LocationType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        lt.setId(attributes.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        lt.setDescription(attributes.getAsJsonPrimitive(RestHelper.ATTR_DESCRIPTION).getAsString());
        lt.setName(attributes.getAsJsonPrimitive(LocationType.ATTR_NAME).getAsString());
        parseFields(lt, attributes.getAsJsonArray(RestHelper.ATTR_FIELDS));

        return lt;
    }

    private void parseFields(LocationType locationType, JsonArray fields) {
        // ToDO: parse the field -> implementation must be controlled
        Iterator<JsonElement> iterator = fields.iterator();
        List<Field> fieldList = new ArrayList<>();
        while (iterator.hasNext()) {
            Field field = null;

            try {
                JsonElement jsonElement = iterator.next();
                field = new Field();
                field.setId(jsonElement.getAsJsonObject().get(RestHelper.ATTR_ID).getAsString());
                field.setTitle(jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_TITLE).getAsString());
                String fieldType = jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_FIELD_TYPE).getAsString();
                field.setFieldType((FieldType) dynEnumManager.valueOf(FieldType.valueOf(fieldType)));
                field.setDefiningEntityId(locationType.getId());
                if (jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_OPTIONS) != null) {
                    JsonArray options = jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_OPTIONS).getAsJsonArray();
                    for (JsonElement option : options) {
                        String optionString = option.getAsJsonPrimitive().getAsString();
                        field.addOption(new FieldOption(field.getId(), optionString));
                    }
                    fieldList.add(field);
                }
            } catch (Exception e) {
                logger.error("LocationTypeRestService:-> Method parseFields() threw an exception for ID= {} ", field.getId(), e);
            }
        }
        locationType.addFields(new HashSet<>(fieldList));
    }

    public RestResultIterator<LocationType> doGetLocationTypes() {
        restClient.reset()
                .setMethod(Method.GET)
                .setEndpoint(INVENTORY_TYPES_ENDPOINT)
                .putUriParameter("entityType", LocationEntity.ENTITY_TYPE_LOCATION);

        return new RestResultIterator<>(restClient, this, true);
    }
}


