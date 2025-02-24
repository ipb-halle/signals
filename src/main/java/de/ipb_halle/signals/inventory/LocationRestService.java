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
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldOption;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.*;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Manager for signals locations (inventory/location API endpoint)
 */

@Local
public class LocationRestService implements RestReplyParser<LocationEntity> {

    public final String LOCATION_ENDPOINT = "/inventory/locations/%s";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    private Logger logger = LoggerFactory.getLogger(LocationRestService.class);

    public LocationEntity parseReply(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject attributes = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        LocationEntity loc = new LocationEntity();
        loc.setId(RestHelper.parseString(jsonObj, RestHelper.ATTR_ID));
        loc.setBarcode(RestHelper.parseString(attributes, LocationEntity.ATTR_BARCODE));
        loc.setCreatedAt(RestHelper.parseDate(attributes, LocationEntity.ATTR_CREATED_AT));
        loc.setName(RestHelper.parseString(attributes, LocationEntity.ATTR_NAME));
        loc.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        loc.setGrid(RestHelper.parseBool(attributes, LocationEntity.ATTR_GRID));
        loc.setTypeId(RestHelper.parseString(attributes, LocationEntity.ATTR_TYPE_ID));
        loc.setTypeName(RestHelper.parseString(attributes, LocationEntity.ATTR_TYPE_NAME));
        loc.setUpdatedAt(RestHelper.parseDate(attributes, LocationEntity.ATTR_UPDATED_AT));

        parseAncestor(attributes.getAsJsonArray(LocationEntity.ATTR_ANCESTORS), loc);
        parseChangeRecords(jsonObj, loc);
        parseFields(attributes, loc);

        return loc;
    }


    private JsonElement fetch(String id) {
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(LOCATION_ENDPOINT, id))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch (UnexpectedResponseCodeException ue) {
            logger.error("LRS:-> Unexpected response code when fetching location with ID: {}", id, ue);
        } catch (URISyntaxException me) {
            logger.error("LRS:-> Malformed URL for ID: {}", id, me);
        } catch (IOException ioe) {
            logger.error("LRS:-> IOException occurred while fetching location with ID: {}", id, ioe);
        }
        return null;
    }

    public LocationEntity doGetLocation(String id) {
        return parseReply(fetch(id));
    }

    public void parseAncestor(JsonArray ancestors, LocationEntity loc) {
        if (ancestors.size() > 0) {
            JsonObject obj = ancestors.get(0).getAsJsonObject();
            loc.setAncestorId(RestHelper.parseString(obj, LocationEntity.ATTR_ANCESTOR_ID));
            loc.setAncestorName(RestHelper.parseString(obj, LocationEntity.ATTR_ANCESTOR_NAME));
            return;
        }
        loc.setAncestorId(null);
        loc.setAncestorName(null);
    }

    private void parseChangeRecords(JsonObject json, LocationEntity loc) {
        loc.setCreatedBy(RestHelper.parseString(
                RestHelper.getPrimitiveFromPath(json, LocationEntity.ATTR_CREATED_BY)));
        loc.setUpdatedBy(RestHelper.parseString(
                RestHelper.getPrimitiveFromPath(json, LocationEntity.ATTR_UPDATED_BY)));
    }

    private void parseFields(JsonObject attributes, LocationEntity loc) {
        JsonArray fields = attributes.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_FIELDS);
        Iterator<JsonElement> iterator = fields.iterator();
        List<Field> fieldList = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            Field field = new Field();
            field.setId(jsonElement.getAsJsonObject().get(RestHelper.ATTR_ID).getAsString());
            field.setTitle(jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_TITLE).getAsString());
            String fieldType = jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_FIELD_TYPE).getAsString();
            field.setFieldType((FieldType) dynEnumManager.valueOf(FieldType.valueOf(fieldType)));
            field.setDefiningEntityId(loc.getId());
            if (jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_OPTIONS) != null) {
                JsonArray options = jsonElement.getAsJsonObject().getAsJsonObject(Field.ATTR_DEFINITION).get(Field.ATTR_OPTIONS).getAsJsonArray();
                for (JsonElement option : options) {
                    String optionString = option.getAsJsonPrimitive().getAsString();
                    field.addOption(new FieldOption(field.getId(), optionString));
                }
            }
            fieldList.add(field);
        }
        LocationTypeFieldValuesParser svc = new LocationTypeFieldValuesParser();
        JsonArray jsonArray = attributes.getAsJsonObject().get(RestHelper.ATTR_FIELDS).getAsJsonArray();
        List<FieldValue> fieldValues = svc.parseReply(jsonArray);
        loc.setFieldValues(new HashSet<>(fieldValues));

        //ToDo -> check if fields should be added to LocationEntity
        //loc.addFields(new HashSet<>(fieldList)); ??
    }
}
