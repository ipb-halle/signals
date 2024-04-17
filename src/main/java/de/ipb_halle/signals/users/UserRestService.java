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

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestClientImpl;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.NoSuchElementException;

import jakarta.ejb.Local;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Signals API REST service for users
 */

@Local
public class UserRestService implements RestService<User> {

    private enum EndPoint {
        CREATE,
        UPDATE
    }

    /*
     * Parameter 'q' is a String and it is used to 
     * filter users. Example usage: Input 'foo' as 
     * value of 'q', will return
     * 
     * Users whose first name starts with 'foo'.
     * Users whose last name starts with 'foo'.
     * Users whose email address starts with 'foo'.
     * Users who has role name (except "Standard User") contains 'foo'.
     * All of above are case insensitive.
     * 
     * Parameter 'enabled'
     * Parameter page[offset]
     * Parameter page[limit]
     */
    public final String USERS_ENDPOINT = "/users";
    public final String USER_ENDPOINT = "/users/%s";
    public final String USER_DELETE_ENDPOINT = "/users/%s";                     // could also be "/users/%s?assignResourceTo=%s";
    public final String SYSTEMGROUPS_ENDPOINT = "/users/%s/systemGroups";

    private final String SCIM_ACTIVATE_ENDPOINT = "/scim/v2/Users/user-%s";
    private final String SCIM_ACTIVATE_PAYLOAD = """
{
  "schemas": [
    "urn:ietf:params:scim:api:messages:2.0:PatchOp"
  ],
  "Operations": [
    {
      "op": "replace",
      "path": "active",
      "value": true
    }
  ]
}
""";




    private Logger logger = LoggerFactory.getLogger(UserRestService.class);


    @Inject
    private RestClientImpl restClient;

    /**
     * deserialize user
     */
    public User createEntity(JsonElement j) {
        User user = parseUser(j);

        if (user.isEnabled()) {
            JsonArray roles = RestHelper.getFromPath(j, User.ATTR_RELATIONSHIP_ROLES).getAsJsonArray();
            parseRoles(user, roles);
            doGetSystemGroupMemberships(user);
        }
        return user;
    }

    /**
     * @param user the database user to be activated
     * @return true if REST call succeeded
     */
    public boolean doActivateUser(User user) {
        try {
            restClient.reset()
                .setMethod(Method.PATCH)
                .setEndpoint(String.format(SCIM_ACTIVATE_ENDPOINT, user.getId()))
                .setContentType(RestClient.APPLICATION_SCIM_JSON)
                .setRequestData(SCIM_ACTIVATE_PAYLOAD)
                .execute(RestClient.HTTP_OK);
            return true;
        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doActivateUser() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doActivate() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return false;
    }

    /**
     * POST -- create user
     */
    public User doCreateUser(User user) {
        String request = prepareJsonString(EndPoint.CREATE, user, null, null);
        try {
            restClient.reset()
                .setMethod(Method.POST)
                .setEndpoint(USERS_ENDPOINT)
                .setRequestData(request)
                .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createEntity(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doCreateUser() got unexpected return code from API call: {}", request);
        } catch(URISyntaxException me) {
            logger.warn("doCreateUser() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    public User doDisableUser(User user) {
        try {
            restClient.reset()
                .setMethod(Method.DELETE)
                .setEndpoint(String.format(USER_DELETE_ENDPOINT, user.getId()))
                .execute(RestClient.HTTP_NO_CONTENT);

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doDisableUser() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doDisableUser() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    /**
     * GET system group memberships
     */
    public void doGetSystemGroupMemberships(User user) {
        try {
            restClient.reset()
                .setEndpoint(String.format(SYSTEMGROUPS_ENDPOINT, user.getId()))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            parseMemberships(user, jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA).getAsJsonArray());

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doGetSystemGroupMemberships() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doGetSystemGroupMemberships() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
    }

    /**
     * GET user by id
     */
    public User doGetUser(String id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(USER_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createEntity(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doGetUser() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doGetUser() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    /**
     * GET users -- obtain list of users 
     */
    public List<User> doGetUsers(String query, Boolean enabled) {
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(USERS_ENDPOINT);

        if (query != null) {
            restClient.putUriParameter("q", query);
        }
        if (enabled != null) {
            restClient.putUriParameter("enabled", enabled ? "true" : "false");
        }

        RestResultIterator<User> iter = new RestResultIterator<> (restClient, this, true);
        ArrayList<User> users = new ArrayList<> ();

        while(iter.hasNext()) {
            users.add(iter.next());
        }
        return users;
    }

    /**
     * ... update user
     */
    public User doUpdateUser(User user, Set<Group> groupsToAdd, Set<Group> groupsToRemove) {
        try {
            restClient.reset()
                .setMethod(Method.PATCH)
                .setEndpoint(String.format(USER_ENDPOINT, user.getId()))
                .setRequestData(prepareJsonString(EndPoint.UPDATE, user, groupsToAdd, groupsToRemove))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createEntity(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doUpdateUser() got unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doUpdateUser() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    private void parseMemberships(User user, JsonArray jArray) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            JsonObject json = iter.next().getAsJsonObject();
            user.addSystemGroup(new GroupReference()
                .setId(RestHelper.parseString(json, RestHelper.ATTR_ID)));
        }
    }

    private void parseRoles(User user, JsonArray jArray) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            JsonObject json = iter.next().getAsJsonObject();
            user.addRole(new RoleReference()
                .setId(RestHelper.parseString(json, RestHelper.ATTR_ID)));
        }
    }

    public User parseUser(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        User user = new User();
        user.setId(attributes.getAsJsonPrimitive(User.ATTR_USER_ID).getAsString());
        String alias = RestHelper.parseString(attributes, User.ATTR_ALIAS);
        user.setAlias((alias != null) ? alias.toUpperCase() : null);
        user.setCountry(attributes.getAsJsonPrimitive(User.ATTR_COUNTRY).getAsString());
        user.setCreatedAt(RestHelper.parseDate(attributes, User.ATTR_CREATED_AT, new Date(0)));
        user.setEmail(attributes.getAsJsonPrimitive(User.ATTR_EMAIL).getAsString().toLowerCase());
        user.setEnabled(attributes.getAsJsonPrimitive(User.ATTR_ENABLED).getAsBoolean());
        user.setFirstName(attributes.getAsJsonPrimitive(User.ATTR_FIRST_NAME).getAsString());
        user.setMutable(false);
        user.setLastLoginAt(RestHelper.parseDate(attributes, User.ATTR_LAST_LOGIN, new Date(0)));
        user.setLastName(attributes.getAsJsonPrimitive(User.ATTR_LAST_NAME).getAsString());
        user.setOrganization(attributes.getAsJsonPrimitive(User.ATTR_ORGANIZATION).getAsString());
        user.setUserName(attributes.getAsJsonPrimitive(User.ATTR_USER_NAME).getAsString().toLowerCase());
        return user;
    }

    protected String prepareJsonString(EndPoint endpoint, User user, Set<Group> groupsToAdd, Set<Group> groupsToRemove) {
        JsonObject attributes = new JsonObject();
        attributes.addProperty(User.ATTR_ALIAS, user.getAlias());
        attributes.addProperty(User.ATTR_COUNTRY, user.getCountry());
        if (endpoint == EndPoint.CREATE) {
            attributes.addProperty(User.ATTR_EMAIL_ADDRESS, user.getUserName());
        }
        attributes.addProperty(User.ATTR_FIRST_NAME, user.getFirstName());
        attributes.addProperty(User.ATTR_LAST_NAME, user.getLastName());
        attributes.addProperty(User.ATTR_ORGANIZATION, user.getOrganization());
        if (user.getRoles() != null) {
            attributes.add(User.ATTR_ROLES, prepareRoles(user));
        }
        if (endpoint == EndPoint.UPDATE) {
            attributes.add(User.ATTR_SYSTEM_GROUPS, prepareSystemGroups(user, groupsToAdd, groupsToRemove));
        }

        JsonObject data = new JsonObject();
        data.add(RestHelper.ATTR_ATTRIBUTES, attributes);

        JsonObject obj = new JsonObject();
        obj.add(RestHelper.ATTR_DATA, data);
        return obj.toString();
    }

    private JsonArray prepareRoles(User user) {
        JsonArray roles = new JsonArray();
        for (IRole roleObj : user.getRoles()) {
            JsonObject role = new JsonObject();
            role.addProperty(RestHelper.ATTR_ID, roleObj.getId());
            role.addProperty(Role.ATTR_NAME, ((Role) roleObj).getName());
            roles.add(role);
        }
        return roles;
    }

    private JsonArray prepareSystemGroups(User user, Set<Group> groupsToAdd, Set<Group> groupsToRemove) {
        JsonArray systemGroupUpdates = new JsonArray();
        for (IGroup group: groupsToAdd) {
            JsonObject systemGroup = new JsonObject();
            systemGroup.addProperty(Group.ATTR_NAME, ((Group) group).getName());
            systemGroup.addProperty(Group.ATTR_ASSOCIATE_TYPE, Group.ATTR_ASSOCIATE_TYPE_ADD);
            systemGroupUpdates.add(systemGroup);
        }
        for (Group group : groupsToRemove) {
            JsonObject systemGroup = new JsonObject();
            systemGroup.addProperty(Group.ATTR_NAME, ((Group) group).getName());
            systemGroup.addProperty(Group.ATTR_ASSOCIATE_TYPE, Group.ATTR_ASSOCIATE_TYPE_REMOVE);
            systemGroupUpdates.add(systemGroup);
        }
        return systemGroupUpdates;
    }
}

