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
package de.ipb_halle.signals.attachment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.rest.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;


/**
 * service for attachments (not yet a real REST service)
 */

//@Local
public class AttachmentRestService implements RestReplyParser<Attachment> {

    public final static String ATTACHMENT_ENDPOINT = "/entities/%s";
    public final static String ATTACHMENT_ANCESTORS = "data.relationships.ancestors.data";
    public final static String ATTACHMENT_ATTRIBUTES = "data.attributes";
    public final static String ATTACHMENT_RELATIONSHIPS = "data.relationships";
    public final static String ATTACHMENT_CHILDREN = "data.relationships.children.data";
    private Logger logger = LoggerFactory.getLogger(AttachmentRestService.class);

    @Inject
    private RestClient restClient;

    @Override
    public Attachment parseReply(JsonElement resultObject) {
        Attachment attachment = new Attachment();

        //check if resultObject is an Object and 'data' exists
        if (resultObject.isJsonObject() && resultObject.getAsJsonObject().has(RestHelper.ATTR_DATA)) {
            JsonArray attachmentsChildrenDataArray = RestHelper.getFromPath(resultObject.getAsJsonObject(), ATTACHMENT_CHILDREN).getAsJsonArray();

            // iteration through "data"-array elements
            for (JsonElement attachmentChildElement : attachmentsChildrenDataArray) {
                if (attachmentChildElement.isJsonObject()) {
                    JsonObject childObject = attachmentChildElement.getAsJsonObject();

                    // extraction fields like "type" and "id"
                    String childId = childObject.has(RestHelper.ATTR_ID) ? childObject.get(RestHelper.ATTR_ID).getAsString() : null;
                    String childType = Objects.requireNonNull(childId).substring(0, childId.indexOf(":"));
                    logger.info("ARS:-> Attachment type -  Type: {}\n", childType);

                    JsonElement attachmentDescriptionJson = fetch(childId);
                    JsonObject attachmentObject = Objects.requireNonNull(attachmentDescriptionJson).getAsJsonObject();
                    JsonObject attachmentAttributes = RestHelper.getFromPath(attachmentObject, ATTACHMENT_ATTRIBUTES).getAsJsonObject();
                    JsonArray attachmentAncestors = RestHelper.getFromPath(attachmentObject, ATTACHMENT_ANCESTORS).getAsJsonArray();

                    for (JsonElement ancestor : attachmentAncestors) {
                        JsonObject ancestorObject = ancestor.getAsJsonObject();
                        attachment.setAncestorId(ancestorObject.has(RestHelper.ATTR_ID) ? ancestorObject.get(RestHelper.ATTR_ID).getAsString() : null);
                    }
                } else {
                    logger.warn("ARS:-> Child element is not a JsonObject: {}\n", attachmentChildElement);
                }
            }
        } else {
            logger.warn("ARS:-> 'children' is not a valid JSON object or does not contain 'data'.\n");
        }
        return attachment;
    }

    private JsonElement fetch(String id) {
        JsonElement resultJsonElement;
        try {
            restClient.setMethod(Method.GET)
                    .setEndpoint(String.format(ATTACHMENT_ENDPOINT, id))
                    .execute();

            String response = restClient.getResponse().getString();
            resultJsonElement = JsonParser.parseString(response);
            return resultJsonElement;
        } catch (UnexpectedResponseCodeException ue) {
            logger.warn("ARS:-> Unexpected code: {}\n", ue.getMessage(), ue);
        } catch (URISyntaxException me) {
            logger.warn("ARS:-> Malformed URL: {}\n", me.getMessage(), me);
        } catch (IOException ioe) {
            logger.warn("ARS:-> IOException {}", ioe.getMessage(), ioe);
        }
        return null;
    }

    public Attachment doGetAttachment(String id) {
        return parseReply(Objects.requireNonNull(fetch(id)));
    }

    public boolean checkIfEntityHasChildren(SignalsEntityDTO experiment) {
        JsonElement resultJsonElement = fetch(experiment.getId());
        JsonObject relationship = RestHelper.getFromPath(Objects.requireNonNull(resultJsonElement).getAsJsonObject(), ATTACHMENT_RELATIONSHIPS).getAsJsonObject();
        return relationship.has(RestHelper.ATTR_CHILDREN);
    }

    /**
     * Actually do a REST call to obtain a single attachment
     *
     * @param endpoint    the specific endpoint with id
     * @param contentType MIME type of the attachment
     * @return path of the received attachment in the staging area
     */
    public RestReply fetchAttachment(String endpoint, String contentType) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setContentType(contentType)
                    .setResponseType(RestClient.RestType.STREAM)
                    .setEndpoint(endpoint)
                    .execute();
            return restClient.getResponse();
        } catch (UnexpectedResponseCodeException e) {
            // attachment (drawing, image, sequence) may not be available
            logger.debug("MRS:-> Unexpected response code {}", restClient.getResponseCode(), e);
        } catch (IOException e) {
            logger.warn("MRS:-> caught IOException: {}", e.getMessage(), e);
        } catch (URISyntaxException e) {
            logger.warn("MRS:-> URISyntaxException: {}", e.getMessage(), e);
        }
        return null;
    }





}
