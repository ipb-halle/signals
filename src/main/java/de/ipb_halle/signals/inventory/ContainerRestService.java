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
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.rest.*;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.users.UserReference;
import jakarta.ejb.Local;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manager for signals containers (inventory/containers API endpoint)
 */

@Local
public class ContainerRestService implements RestReplyParser<Container> {

    public final String CONTAINER_ATTACHMENT_ENDPOINT = "/inventory/containers/%s/fields/%s/attachment";
    public final String CONTAINER_ENDPOINT = "/inventory/containers/%s";
    private static final String CONTAINER_CREATE_ENDPOINT = "/inventory/containers";
    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private AttachmentRestService attachmentRestService;

    @Inject
    private FieldParser fieldParser;

    private Logger logger = LoggerFactory.getLogger(ContainerRestService.class);

    public Container doGetContainer(String id) {
        JsonElement data = fetch(id);
        if (data == null) {
            logger.error("ContainerRestService:-> doGetContainer()-> No container found for ID={} null will be returned\n", id);
            return null;
        }
        return parseReply(data);
    }

    @Override
    public Container parseReply(JsonElement json) {
        JsonObject j = json.getAsJsonObject();
        JsonObject attributes = j.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        JsonArray fieldsJsonArray = attributes.get(RestHelper.ATTR_FIELDS).getAsJsonArray();

        Container ct = new Container();
        ct.setId(ContainerTypeRestService.CONTAINER_TYPE_ENTITY_PREFIX
                + RestHelper.parseString(j, RestHelper.ATTR_ID)
                + ContainerTypeRestService.CONTAINER_TYPE_ENTITY_SUFFIX);
        ct.setDescription(RestHelper.parseString(attributes, RestHelper.ATTR_DESCRIPTION));
        ct.setBarcode(RestHelper.parseString(attributes, ContainerEntity.ATTR_BARCODE));
        ct.setDigest(RestHelper.parseString(attributes, RestHelper.ATTR_DIGEST));
        ct.setCreatedAt(RestHelper.parseDate(attributes, ContainerEntity.ATTR_CREATED_AT));
        ct.setContainerTypeId(RestHelper.parseString(attributes, ContainerEntity.ATTR_CONTAINER_TYPE_ID));
        ct.setContainerTypeName(RestHelper.parseString(attributes, ContainerEntity.ATTR_CONTAINER_TYPE_NAME));
        ct.setLocation(new LocationReference().setId(
                LocationTypeRestService.LOCATION_TYPE_ENTITY_PREFIX
                        + RestHelper.parseString(
                        RestHelper.getPrimitiveFromPath(attributes, ContainerEntity.ATTR_LOCATION_ID))
                        + LocationTypeRestService.LOCATION_TYPE_ENTITY_SUFFIX));
        ct.setName(RestHelper.parseString(attributes, RestHelper.ATTR_NAME));
        try {
            ct.setUnit(Unit.getUnit(RestHelper.parseString(attributes, ContainerEntity.ATTR_UNIT)));
        } catch (Exception e) {
            logger.error("ContainerRestService:-> Id of container for unit setting where error occurring is: {},", ct.getId(), e);
        }

        // parseFieldValues(attributes.getAsJsonArray(RestHelper.ATTR_FIELDS), ct);
        parseFields(fieldsJsonArray, ct);
        parseContainerContents(attributes.getAsJsonArray(ContainerEntity.ATTR_CONTENTS), ct);

        parseChangeRecords(j, ct);

        //logger.trace("ContainerRestService:-> Parsed container with ID={}", ct.getId());
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
            logger.error("ContainerRestService: fetch() -> Unexpected code for container ID={}", id, ue);
        } catch (URISyntaxException me) {
            logger.error("ContainerRestService: fetch() -> Malformed URL", me);
        } catch (IOException ioe) {
            logger.error("ContainerRestService: fetch() -> IOException", ioe);
        }
        return null;
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


    private void parseFields(JsonArray fields, Container ct) {
        Iterator<JsonElement> iter = fields.iterator();
        List<Field> fieldList = new ArrayList<>();
        while (iter.hasNext()) {
            Field field = fieldParser.parseReply(iter.next());
            // NOTE: field ids are NOT unique within Signals Inventory
            //field.setId(field.getId()+":"+ ct.getId());
            field.setId(field.getId());
            field.setDesignation((FieldDesignation) dynEnumManager.valueOf(FieldDesignation.valueOf(FieldDesignation.LOCATION)));
            field.setDefiningEntityId(ct.getId());
            fieldList.add(field);
        }
        ct.addFields(fieldList);
        // Parsing of field Values
        LocationTypeFieldValuesParser svc = new LocationTypeFieldValuesParser();
        List<FieldValue> values = svc.parseReply(fields);
        ct.addFieldValues(values);
    }

    /**
     * Containers may contain Materials or Samples, depending on entityType
     * (or the prefix of the entityId).
     *
     * @param jArray
     * @param ct
     */
    private void parseContainerContents(JsonArray jArray, Container ct) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            JsonElement json = iter.next();
            String type = RestHelper.parseString(RestHelper.getPrimitiveFromPath(json, ContainerEntity.ATTR_CONTENT_TYPE));
            String id = RestHelper.parseString(RestHelper.getPrimitiveFromPath(json, ContainerEntity.ATTR_CONTENT_ID));
            switch (type) {
                case ContainerEntity.CONTENT_TYPE_ASSET:
                    ct.setMaterial(new MaterialReference().setId(id));
                    break;
                case ContainerEntity.CONTENT_TYPE_BATCH:
                    ct.setMaterial(new MaterialReference().setId(id));
                    break;
                case ContainerEntity.CONTENT_TYPE_SAMPLE:
                    //ToDo: implement SAMPLE!!!
                    ct.setMaterial(new MaterialReference().setId(id));
                   // logger.warn("ContainerRestService:->Unable to assign Sample to Container with Id={}", id);
                    break;
                default:
                    throw new RuntimeException("Unknown content type for container: " + ct.getId());
            }
        }
    }

    /**
     * Obtain a container attachment as an octet stream.
     *
     * @param container
     * @param field
     * @return path of the downloaded attachment in the staging area
     */
    public RestReply doGetContainerAttachment(Container container, Field field, String mimeType) {
        String endpoint = String.format(CONTAINER_ATTACHMENT_ENDPOINT, container.getId(), field.getStripedId());
        return attachmentRestService.fetchAttachment(endpoint, mimeType);
    }

    public String parseAttachmentMimeType(FieldValue fieldValue) {
        JsonElement json = JsonParser.parseString(fieldValue.getValue());
        return RestHelper.getPrimitiveFromPath(json, Container.ATTR_ATTACHMENT_MIMETYPE).getAsString();
    }

    public void parseAttachmentRevisionInfo(AttachmentRevision newRevision, FieldValue fieldValue) {
        JsonElement json = JsonParser.parseString(fieldValue.getValue());
        /*
        json element {"attachment":
                        {"filename":"certificate.pdf",
                        "mimeType":"application/pdf",
                        "size":21568},
                     "isRawValue":false,
                     "auto":"/api/v1.0/inventory/containers/864f0a22-52bd-467d-94c5-46b3bd645032/fields/b30f5c49-75e7-44a9-a05a-f99ec4ec6618/attachments"
                        }
         */
        newRevision.setOriginalName(RestHelper.getPrimitiveFromPath(json, Container.ATTR_ATTACHMENT_FILENAME).getAsString());
        newRevision.setMimeType(RestHelper.getPrimitiveFromPath(json, Container.ATTR_ATTACHMENT_MIMETYPE).getAsString());
        newRevision.setSize(RestHelper.getPrimitiveFromPath(json, Container.ATTR_ATTACHMENT_FILESIZE).getAsLong());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void doCreateContainer(ContainerType containerType, Container container) {

        JsonObject request = prepareContainer(containerType, container);

        try {
            restClient.reset()
                    .setMethod(Method.POST)
                    .setEndpoint(CONTAINER_CREATE_ENDPOINT)
                    .setRequestData(request.toString())
                    .execute(RestClient.HTTP_CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private JsonObject prepareContainer(ContainerType containerType, Container container) {
        JsonObject resultingJson = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty(RestHelper.ATTR_TYPE, InventoryType.inventoryContainer.toString());
        data.add(RestHelper.ATTR_ATTRIBUTES, prepareAttributes(containerType, container));


        return null;
    }

    private JsonObject prepareAttributes(ContainerType containerType, Container container) {
        JsonObject attributes = new JsonObject();
        attributes.addProperty(RestHelper.ATTR_NAME, container.getName());
        attributes.addProperty(RestHelper.ATTR_DESCRIPTION, container.getDescription());
        attributes.addProperty(RestHelper.ATTR_TYPE_ID, containerType.getId().split(":")[1]);
        attributes.addProperty(LocationEntity.ATTR_GRID, true);
        attributes.addProperty(LocationEntity.ATTR_ROWS, 8);
        attributes.addProperty(LocationEntity.ATTR_COLUMNS, 12);
        attributes.add(LocationEntity.ATTR_ANCESTORS, prepareAncestors(container));
        attributes.add(RestHelper.ATTR_FIELDS, prepareFields(container));
        return attributes;
    }

    private JsonElement prepareFields(Container container) {
        return null;
    }

    private JsonElement prepareAncestors(Container container) {
        return null;
    }
}
