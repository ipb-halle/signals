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

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestReplyParser;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Local;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Signals REST API service for roles
 */

@Local
public class RoleRestService implements RestReplyParser<Role> {

    public final String ROLES_ENDPOINT = "/roles";
    public final String ROLE_ENDPOINT = "/roles/%s";

    @Inject
    private RestClient restClient;

    private Logger logger = LoggerFactory.getLogger(RoleRestService.class);


    public Role parseReply(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        Role role = new Role();
        role.setDescription(attributes.getAsJsonPrimitive(RestHelper.ATTR_DESCRIPTION).getAsString());
        role.setId(j.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        role.setName(attributes.getAsJsonPrimitive(Role.ATTR_NAME).getAsString());
        parsePrivileges(attributes.getAsJsonObject(Role.ATTR_PRIVILEGES), role);

        return role;
    }

    public Role doGetRole(String id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(ROLE_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return parseReply(jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA));

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("doGetRole() unexpected return code from API call");
        } catch(URISyntaxException me) {
            logger.warn("doGetRole() malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null;
    }

    public List<Role> doGetRoles() {
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(ROLES_ENDPOINT);

        RestResultIterator<Role> iter = new RestResultIterator<> (restClient, this, false);
        List<Role> roles = new ArrayList<> ();
        while(iter.hasNext()) {
            roles.add(iter.next());
        }
        return roles;
    }

    private void parsePrivileges(JsonObject json, Role role) {
        for(String key : json.keySet()) {
            if (json.getAsJsonPrimitive(key).getAsBoolean()) {
                role.addPrivilege(key);
            }
        }
    }
}
