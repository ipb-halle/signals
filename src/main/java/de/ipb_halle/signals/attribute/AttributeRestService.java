/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.attribute;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.inventory.LocationRestService;
import de.ipb_halle.signals.rest.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AttributeRestService implements RestReplyParser<Attribute> {

    public final String ATTRIBUTES_LIST_ENDPOINT = "/attributes";
    public final String ATTRIBUTES_ENDPOINT = "/attributes/%s";

    @Inject
    private RestClient restClient;

    @Inject
    private DynEnumManager dynEnumMgr;


    private Logger logger = LoggerFactory.getLogger(LocationRestService.class);

    @Override
    public Attribute parseReply(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject jsonAttr = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        Attribute attr = new Attribute();
        attr.setId(RestHelper.parseString(jsonObj, RestHelper.ATTR_ID));
        attr.setName(RestHelper.parseString(jsonAttr, RestHelper.ATTR_NAME));
        attr.setDescription(RestHelper.parseString(jsonAttr, RestHelper.ATTR_DESCRIPTION));
        attr.setType(lookupAttributeType(RestHelper.parseString(jsonAttr, RestHelper.ATTR_TYPE)));
        switch(attr.getType().getValue()) {
            case AttributeType.CHOICE :
                parseChoice(attr, jsonAttr);
                break;
            case AttributeType.SEQUENCE:
                parseSequence(attr, jsonAttr);
                break;
        }
        return attr;
    }

    public void parseChoice(Attribute attr, JsonObject jsonAttr) {
        JsonArray options = jsonAttr.getAsJsonArray(Attribute.ATTR_OPTIONS);
        Iterator<JsonElement> iter = options.iterator();
        while (iter.hasNext()) {
            String optionString = iter.next().getAsString();
            attr.addOption(optionString);
        }
    }

    public void parseSequence(Attribute attr, JsonObject jsonAttr) {
        attr.setFormat(RestHelper.parseString(jsonAttr, Attribute.ATTR_FORMAT));
    }

    /**
     * converts a attribute type from JSON to the respective
     * database backed attribute type class instance.
     * @throws RuntimeException if field type is not yet registered and
     * auto discovery is not allowed (default).
     */
    private AttributeType lookupAttributeType(String typeString) {
        return (AttributeType) dynEnumMgr.valueOf(AttributeType.valueOf(typeString));
    }

    public Attribute doGetSingleAttribute(String id) {
        return parseReply(fetchSingleAttribute(id));
    }

    public List<Attribute> doGetAllAttributes() {
        return processListElements(fetchAllAttributes());
    }

    public JsonElement fetchSingleAttribute(String id) {
        try {
            restClient
                    .reset()
                    .setMethod(Method.GET)
                    .setEndpoint(String.format(ATTRIBUTES_ENDPOINT, id))
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected response code when fetching attribute with ID: {}", id, ue);
        } catch(URISyntaxException me) {
            logger.warn("Malformed URL for ID: {}", id, me);
        } catch (IOException ioe) {
            logger.warn("IOException occurred while fetching attribute with ID: {}", id, ioe);
        }
        return null;
    }

    public JsonArray fetchAllAttributes() {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setEndpoint(ATTRIBUTES_LIST_ENDPOINT)
                    .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA);

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected response code", ue);
        } catch(URISyntaxException me) {
            logger.warn("Malformed URL", me);
        } catch (IOException ioe) {
            logger.warn("IOException occurred while fetching attributes list", ioe);
        }
        return null;
    }

    private List<Attribute> processListElements(JsonArray jArray) {
        List<Attribute> attributes = new ArrayList<> ();
        if (jArray != null) {
            Iterator<JsonElement> iter = jArray.iterator();
            while (iter.hasNext()) {
                JsonElement listElement = iter.next();
                JsonElement attributeData = fetchSingleAttribute(
                        listElement.getAsJsonObject().get(RestHelper.ATTR_ID).getAsString());
                attributes.add(parseReply(attributeData));
            }
        }
        return attributes;
    }
}
