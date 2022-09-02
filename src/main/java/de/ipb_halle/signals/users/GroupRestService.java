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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.Method;
import de.ipb_halle.signals.RestClient;
import de.ipb_halle.signals.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ejb.Stateless;
import javax.inject.Inject;


/** 
 * Signals API REST service for groups
 */

@Stateless
public class GroupRestService {

    public final String GROUPS_ENDPOINT = "/groups";
    public final String GROUP_ENDPOINT = "/groups/%d";


    @Inject
    private RestClient restClient;
    

    private class GroupIterator implements Iterator<Group> {
        private RestClient client;
        private JsonElement jsonResult;
        private Iterator<JsonElement> jsonIterator;

        public GroupIterator(RestClient c) {
            client = c;
            initialFetch();
        }

        private void initialFetch() {
            try {
                client.reset()
                    .setEndpoint(GROUPS_ENDPOINT)
                    .execute();

                jsonResult = JsonParser.parseString(client.getResponse());
                jsonIterator = jsonResult.getAsJsonObject().getAsJsonArray("data").iterator();

            } catch(UnexpectedResponseCodeException ue) {
                System.out.println("Unexpected code");
            } catch(MalformedURLException me) {
                System.out.println("Malformed URL");
            } catch(IOException ioe) {
                System.out.println("IOException");
                ioe.printStackTrace();
            }
        }

        public boolean hasNext() {
            return jsonIterator.hasNext();
        }

        public Group next() {
            if (hasNext()) {
                return createGroup(jsonIterator.next());
            }
            throw new NoSuchElementException();
        }
    }

    /**
     * deserialize group
     */
    protected Group createGroup(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        Group group = new Group();
        group.setId(attributes.getAsJsonPrimitive(Group.ATTR_ID).getAsInt());
        group.setCreatedAt(group.parseDate(attributes.getAsJsonPrimitive(Group.ATTR_CREATED_AT).getAsString()));
        group.setDescription(getOptionalStringAttribute(attributes, Group.ATTR_DESCRIPTION));
        // digest
        // eid
        group.setEditedAt(group.parseDate(attributes.getAsJsonPrimitive(Group.ATTR_EDITED_AT).getAsString()));
        //flags
        group.setJsonString(j.toString());
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

    private JsonElement fetch(int id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(GROUP_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().get("data");

        } catch(UnexpectedResponseCodeException ue) {
            System.out.println("Unexpected code");
        } catch(MalformedURLException me) {
            System.out.println("Malformed URL");
        } catch(IOException ioe) {
            System.out.println("IOException");
            ioe.printStackTrace();
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

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createGroup(jsonResult.getAsJsonObject().get("data"));

        } catch(UnexpectedResponseCodeException ue) {
            System.out.println("Unexpected code");
        } catch(MalformedURLException me) {
            System.out.println("Malformed URL");
        } catch(IOException ioe) {
            System.out.println("IOException");
            ioe.printStackTrace();
        }
        return null;
    }

    /**
     * GET group by id 
     */
    public Group doGetGroup(int id) {
        return createGroup(fetch(id));
    }

    /**
     * GET groups -- obtain list of groups 
     */
    public List<Group> doGetGroups() {
        GroupIterator iter = new GroupIterator(restClient);
        ArrayList<Group> groups = new ArrayList<> ();

        while(iter.hasNext()) {
            groups.add(iter.next());
        }
        return groups;
    }

    protected String prepareJsonString(Group group) {
        JsonObject attributes = new JsonObject();
        attributes.addProperty(Group.ATTR_NAME, group.getName());
        attributes.addProperty(Group.ATTR_DESCRIPTION, group.getDescription());
        attributes.addProperty(Group.ATTR_SYSTEM, group.isSystem());

        JsonObject data = new JsonObject();
        data.add("attributes", attributes);

        JsonObject obj = new JsonObject();
        obj.add("data", data);
        return obj.toString();
    }
}
