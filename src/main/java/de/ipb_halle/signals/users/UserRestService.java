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
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ejb.Local;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Signals API REST service for users
 */

@Local
public class UserRestService implements RestService<UserEntity> {

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
    public final String USER_ENDPOINT = "/users/%d";

    private Logger logger = LoggerFactory.getLogger(UserRestService.class.getName());


    @Inject
    private RestClient restClient;
    
    /**
     * deserialize user
     */
    public UserEntity createEntity(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        UserEntity user = new UserEntity();
        user.setId(attributes.getAsJsonPrimitive(UserEntity.ATTR_USER_ID).getAsInt());
        user.setAlias(attributes.getAsJsonPrimitive(UserEntity.ATTR_ALIAS).getAsString());
        user.setCountry(attributes.getAsJsonPrimitive(UserEntity.ATTR_COUNTRY).getAsString());
        user.setCreatedAt(RestHelper.parseDate(attributes, UserEntity.ATTR_CREATED_AT, new Date()));
        user.setEmail(attributes.getAsJsonPrimitive(UserEntity.ATTR_EMAIL).getAsString());
        user.setEnabled(attributes.getAsJsonPrimitive(UserEntity.ATTR_ENABLED).getAsBoolean());
        user.setFirstName(attributes.getAsJsonPrimitive(UserEntity.ATTR_FIRST_NAME).getAsString());
        user.setJsonString(j.toString());
        user.setLastLoginAt(RestHelper.parseDate(attributes, UserEntity.ATTR_LAST_LOGIN, new Date()));
        user.setLastName(attributes.getAsJsonPrimitive(UserEntity.ATTR_LAST_NAME).getAsString());
        user.setOrganization(attributes.getAsJsonPrimitive(UserEntity.ATTR_ORGANIZATION).getAsString());
        user.setUserName(attributes.getAsJsonPrimitive(UserEntity.ATTR_USER_NAME).getAsString());

        return user;
    }

    /**
     * POST -- create user
     */
    public UserEntity doCreateUser(UserEntity user) {
        try {
            restClient.reset()
                .setMethod(Method.POST)
                .setEndpoint(USERS_ENDPOINT)
                .setRequestData(prepareJsonString(user))
                .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createEntity(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code");
        } catch(MalformedURLException me) {
            logger.warn("Malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    /**
     * GET user by id 
     */
    public UserEntity doGetUser(int id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(USER_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createEntity(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code");
        } catch(MalformedURLException me) {
            logger.warn("Malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    /**
     * GET users -- obtain list of users 
     */
    public List<UserEntity> doGetUsers(String query, Boolean enabled) {
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(USERS_ENDPOINT);

        if (query != null) {
            restClient.putUrlParameter("q", query);
        }
        if (enabled != null) {
            restClient.putUrlParameter("enabled", enabled ? "true" : "false");
        }

        RestResultIterator<UserEntity> iter = new RestResultIterator<> (restClient, this, true); 
        ArrayList<UserEntity> users = new ArrayList<> ();

        while(iter.hasNext()) {
            users.add(iter.next());
        }
        return users;
    }

    protected String prepareJsonString(UserEntity user) {
        JsonObject attributes = new JsonObject();
        attributes.addProperty(UserEntity.ATTR_ALIAS, user.getAlias());
        attributes.addProperty(UserEntity.ATTR_COUNTRY, user.getCountry());
        attributes.addProperty(UserEntity.ATTR_EMAIL, user.getEmail());
        attributes.addProperty(UserEntity.ATTR_FIRST_NAME, user.getFirstName());
        attributes.addProperty(UserEntity.ATTR_LAST_NAME, user.getLastName());
        attributes.addProperty(UserEntity.ATTR_ORGANIZATION, user.getOrganization());
        if (user.getRoles() != null) {
            attributes.add(UserEntity.ATTR_ROLES, prepareRoles(user));
        }
/*
        if (user.getSystemGroups() != null) {
            attributes.add(UserEntity.ATTR_SYSTEM_GROUPS, prepareSystemGroups(user));
        }
*/

        JsonObject data = new JsonObject();
        data.add(RestHelper.ATTR_ATTRIBUTES, attributes);

        JsonObject obj = new JsonObject();
        obj.add(RestHelper.ATTR_DATA, data);
        return obj.toString();
    }

    private JsonArray prepareRoles(UserEntity user) {
        JsonArray roles = new JsonArray();
        for (Role roleObj : user.getRoles()) {
            JsonObject role = new JsonObject();
            role.addProperty(RestHelper.ATTR_ID, roleObj.getId());
            role.addProperty(Role.ATTR_NAME, roleObj.getName());
            roles.add(role);
        }
        return roles;
    }

    private JsonArray prepareSystemGroups(UserEntity user) {
        JsonArray systemGroups = new JsonArray();
        for (Group groupObj : user.getSystemGroups()) {
            JsonObject systemGroup = new JsonObject();
            systemGroup.addProperty(RestHelper.ATTR_ID, groupObj.getId());
            systemGroup.addProperty(Group.ATTR_NAME, groupObj.getName());
            systemGroups.add(systemGroup);
        }
        return systemGroups;
    }
}

