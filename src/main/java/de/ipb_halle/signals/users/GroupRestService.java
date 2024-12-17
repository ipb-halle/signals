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
package de.ipb_halle.signals.users;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestReplyParser;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.ejb.Local;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Signals API REST service for groups
 */

@Local
public class GroupRestService implements RestReplyParser<Group> {

    public final String GROUPS_ENDPOINT = "/groups";
    public final String GROUP_ENDPOINT = "/groups/%s";


    @Inject
    private RestClient restClient;
    
    private Logger logger = LoggerFactory.getLogger(GroupRestService.class);

    /**
     * deserialize group
     */
    public Group parseReply(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        Group group = new Group();
        group.setId(attributes.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        group.setCreatedAt(RestHelper.parseDate(attributes, Group.ATTR_CREATED_AT, new Date(0)));
        group.setDescription(getOptionalStringAttribute(attributes, RestHelper.ATTR_DESCRIPTION));
        // digest
        // eid
        group.setEditedAt(RestHelper.parseDate(attributes, Group.ATTR_EDITED_AT, new Date(0)));
        //flags
        group.setName(attributes.getAsJsonPrimitive(Group.ATTR_NAME).getAsString());
        group.setSystem(attributes.getAsJsonPrimitive(Group.ATTR_SYSTEM).getAsBoolean());
        group.setType(attributes.getAsJsonPrimitive(Group.ATTR_TYPE).getAsString());

        return group;
    }

    private String getOptionalStringAttribute(JsonObject attributes, String key) {
        JsonPrimitive jp = attributes.getAsJsonPrimitive(key);
        if (jp != null) {
            return jp.getAsString();
        }
        return null;
    }

    /**
     * POST -- create group
     */
    public Group doCreateGroup(Group group) {
        try {
            restClient.reset()
                .setMethod(Method.POST)
                .setEndpoint(GROUPS_ENDPOINT)
                .setRequestData(prepareJsonString(group))
                .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            Group snbGroup = parseReply(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));
            return snbGroup;

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doCreateGroup() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doCreateGroup() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException",  (Throwable) ioe);
        }
        return null;
    }

    /**
     * GET group by id 
     */
    public Group doGetGroup(String id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(GROUP_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse().getString());
            return parseReply(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doGetGroup() Got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doGetGroup() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException",  (Throwable) ioe);
        }
        return null;
    }

    /**
     * GET groups -- obtain list of groups 
     */
    public List<Group> doGetGroups() {
        restClient.reset()
            .setEndpoint(GROUPS_ENDPOINT);

        RestResultIterator<Group> iter = new RestResultIterator<> (restClient, this, false);
        ArrayList<Group> groups = new ArrayList<> ();

        while(iter.hasNext()) {
            groups.add(iter.next());
        }
        return groups;
    }

    /**
     * PATCH group - update group
     */
    public Group doUpdateGroup(Group group) {
        try {
            restClient.reset()
                .setMethod(Method.PATCH)
                .setEndpoint(String.format(GROUP_ENDPOINT, group.getId()))
                .setRequestData(prepareJsonString(group))
                .execute(RestClient.HTTP_CREATED);

            // endpoint does not return data
            return group;

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doUpdateGroup() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doUpdateGroup(): malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException",  (Throwable) ioe);
        }
        return null;
    }

    protected String prepareJsonString(Group group) {
        JsonObject attributes = new JsonObject();
        attributes.addProperty(Group.ATTR_NAME, group.getName());
        attributes.addProperty(RestHelper.ATTR_DESCRIPTION, group.getDescription());
        attributes.addProperty(Group.ATTR_SYSTEM, group.isSystem());

        JsonObject data = new JsonObject();
        data.add(RestHelper.ATTR_ATTRIBUTES, attributes);

        JsonObject obj = new JsonObject();
        obj.add(RestHelper.ATTR_DATA, data);
        return obj.toString();
    }
}
