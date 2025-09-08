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
import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Manager for signals locations (inventory/location API endpoint)
 */

@Local
public class LocationRestService implements RestReplyParser<Location> {

    private static final String LOCATION_ATTACHMENT_ENDPOINT = "/inventory/locations/%s/fields/%s/attachment";
    public final String LOCATION_ENDPOINT = "/inventory/locations/%s";
    public final String LOCATION_CREATE_ENDPOINT = "/inventory/locations";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private AttachmentRestService attachmentRestService;

    @Inject
    private FieldParser fieldParser;

    @Inject
    private LocationDbService locationDbService;

    private Logger logger = LoggerFactory.getLogger(LocationRestService.class);

    public Location doGetLocation(String id) {
        JsonElement data = fetch(id);
        if (data == null) {
            logger.error("LocationRestService:-> doGetLocation() -> No location found for ID ={} null will be returned\n", id);
            return null;
        }
        return parseReply(data);
    }


    @Override
    public Location parseReply(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject attributes = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        JsonArray fieldsJsonArray = attributes.get(RestHelper.ATTR_FIELDS).getAsJsonArray();

        Location loc = new Location();
        loc.setId(LocationTypeRestService.LOCATION_TYPE_ENTITY_PREFIX
                + RestHelper.parseString(jsonObj, RestHelper.ATTR_ID)
                + LocationTypeRestService.LOCATION_TYPE_ENTITY_SUFFIX);
        loc.setBarcode(RestHelper.parseString(attributes, LocationEntity.ATTR_BARCODE));
        loc.setCreatedAt(RestHelper.parseDate(attributes, LocationEntity.ATTR_CREATED_AT));
        loc.setName(RestHelper.parseString(attributes, LocationEntity.ATTR_NAME));
        loc.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        loc.setGrid(RestHelper.parseBool(attributes, LocationEntity.ATTR_GRID));
        loc.setLocationTypeId(RestHelper.parseString(attributes, LocationEntity.ATTR_TYPE_ID));
        loc.setTypeName(RestHelper.parseString(attributes, LocationEntity.ATTR_TYPE_NAME));
        loc.setUpdatedAt(RestHelper.parseDate(attributes, LocationEntity.ATTR_UPDATED_AT));

        parseAncestor(attributes.getAsJsonArray(LocationEntity.ATTR_ANCESTORS), loc);
        parseChangeRecords(jsonObj, loc);
        parseFields(fieldsJsonArray, loc);

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
            logger.error("LocationRestService: fetch() -> Unexpected response code when fetching location with ID: {}", id, ue);
        } catch (URISyntaxException me) {
            logger.error("LocationRestService: fetch() -> Malformed URL for ID: {}", id, me);
        } catch (IOException ioe) {
            logger.error("LocationRestService: fetch() -> IOException occurred while fetching location with ID: {}", id, ioe);
        }
        return null;
    }


    public void parseAncestor(JsonArray ancestors, Location loc) {
        if (ancestors.size() > 0) {
            JsonObject obj = ancestors.get(0).getAsJsonObject();
            loc.setAncestorId(RestHelper.parseString(obj, LocationEntity.ATTR_ANCESTOR_ID));
            loc.setAncestorName(RestHelper.parseString(obj, LocationEntity.ATTR_ANCESTOR_NAME));
            return;
        }
        loc.setAncestorId(null);
        loc.setAncestorName(null);
    }

    private void parseChangeRecords(JsonObject json, Location loc) {
        loc.setCreatedBy(new UserReference(RestHelper.parseString(
                RestHelper.getPrimitiveFromPath(json, LocationEntity.ATTR_CREATED_BY))));
        loc.setUpdatedBy(new UserReference(RestHelper.parseString(
                RestHelper.getPrimitiveFromPath(json, LocationEntity.ATTR_UPDATED_BY))));
    }

    private void parseFields(JsonArray fields, Location loc) {
        Iterator<JsonElement> iterator = fields.iterator();
        List<Field> fieldList = new ArrayList<>();
        while (iterator.hasNext()) {
            Field field = fieldParser.parseReply(iterator.next());
            // NOTE: field ids are NOT unique within Signals Inventory
            field.setId(field.getId());
            field.setDesignation((FieldDesignation) dynEnumManager.valueOf(FieldDesignation.valueOf(FieldDesignation.LOCATION)));
            field.setDefiningEntityId(loc.getId());
            fieldList.add(field);
        }
        loc.addFields(fieldList);
        //Parsig field Values
        LocationTypeFieldValuesParser svc = new LocationTypeFieldValuesParser();
        List<FieldValue> fieldValues = svc.parseReply(fields);
        loc.addFieldValues(new HashSet<>(fieldValues));

    }

    /**
     * Obtain a location attachment as an octet stream.
     *
     * @param location
     * @param field
     * @return path of the downloaded attachment in the staging area
     */
    public RestReply doGetLocationAttachment(Location location, Field field, String mimeType) {
        String endpoint = String.format(LOCATION_ATTACHMENT_ENDPOINT, location.getId(), field.getStripedId());
        return attachmentRestService.fetchAttachment(endpoint, mimeType);
    }

    public String parseAttachmentMimeType(FieldValue fieldValue) {
        JsonElement json = JsonParser.parseString(fieldValue.getValue());
        return RestHelper.getPrimitiveFromPath(json, Location.ATTR_ATTACHMENT_MIMETYPE).getAsString();
    }

    public void parseAttachmentRevisionInfo(AttachmentRevision newRevision, FieldValue fieldValue) {
        JsonElement json = JsonParser.parseString(fieldValue.getValue());

        newRevision.setOriginalName(RestHelper.getPrimitiveFromPath(json, Location.ATTR_ATTACHMENT_FILENAME).getAsString());
        newRevision.setMimeType(RestHelper.getPrimitiveFromPath(json, Location.ATTR_ATTACHMENT_MIMETYPE).getAsString());
        newRevision.setSize(RestHelper.getPrimitiveFromPath(json, Location.ATTR_ATTACHMENT_FILE_SIZE).getAsLong());
    }

    //toDo consider whether to return a new object or not
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String doCreateLocation(LocationType locationType, Location location) {

        JsonObject request = prepareLocation(locationType, location);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(LOCATION_CREATE_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);

            //Return String id of generated location
            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return jsonResult.getAsJsonObject()
                    .getAsJsonObject(RestHelper.ATTR_DATA)
                    .get(RestHelper.ATTR_ID)
                    .getAsJsonPrimitive().getAsString();

        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private JsonObject prepareLocation(LocationType locationType, Location location) {
        JsonObject resultingJson = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, InventoryType.inventoryLocation.toString());
        data.add(RestHelper.ATTR_ATTRIBUTES, prepareAttributes(locationType, location));
        resultingJson.add(RestHelper.ATTR_DATA, data);
        return resultingJson;
    }

    //creates gridBox as Example
    private JsonObject prepareAttributes(LocationType locationType, Location location) {
        JsonObject attributes = new JsonObject();

        attributes.addProperty(RestHelper.ATTR_TYPE_ID, locationType.getId());
        attributes.addProperty(RestHelper.ATTR_DESCRIPTION, location.getDescription());
        attributes.addProperty(RestHelper.ATTR_NAME, location.getName());

        attributes.addProperty(LocationEntity.ATTR_GRID, true);
        attributes.addProperty(LocationEntity.ATTR_ROWS, location.getRows());
        attributes.addProperty(LocationEntity.ATTR_COLUMNS, location.getColumns());
        attributes.add(LocationEntity.ATTR_ANCESTORS, prepareAncestors(location));
        attributes.add(RestHelper.ATTR_FIELDS, prepareFields(location));
        return attributes;
    }

    private JsonElement prepareAncestors(Location location) {
        JsonArray ancestors = new JsonArray();
        ancestors.add(processAncestorsOfLocation(location));

        return ancestors;
    }

    private JsonElement processAncestorsOfLocation(Location location) {
        JsonObject ancestor = new JsonObject();
        ancestor.addProperty(LocationEntity.ATTR_ANCESTOR_ID, location.getAncestorId());
        return ancestor;
    }

     JsonElement prepareFields(Location location) {
        JsonArray fields = new JsonArray();
        for (FieldValue fieldValue : location.getFieldValues()) {
            if (fieldValue.getField().getRequired()
                    || ((!fieldValue.getField().getCalculated())
                    && (!fieldValue.getField().getReadOnly()))) {
                fields.add(prepareFieldValue(fieldValue));
            }
        }
        return fields;
    }

     JsonElement prepareFieldValue(FieldValue fieldValue) {
        JsonObject field = new JsonObject();
        field.addProperty(RestHelper.ATTR_ID, fieldValue.getFieldId().split(":")[0]);
        JsonObject content = new JsonObject();
        content.addProperty(RestHelper.ATTR_VALUE, fieldValue.getValue());
        field.add(RestHelper.ATTR_CONTENT, content);
        return field;
    }
}
