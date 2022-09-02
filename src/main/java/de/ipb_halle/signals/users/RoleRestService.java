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
import de.ipb_halle.signals.RestResultIterator;
import de.ipb_halle.signals.RestService;
import de.ipb_halle.signals.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ejb.Local;
import javax.inject.Inject;


/** 
 * Signals REST API service for roles
 */

@Local
public class RoleRestService implements RestService<Role> {

    public final String ROLES_ENDPOINT = "/roles";
    public final String ROLE_ENDPOINT = "/roles/%d";

    @Inject
    private RestClient restClient;

    public Role createEntity(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        Role role = new Role();
        role.setDescription(attributes.getAsJsonPrimitive(Role.ATTR_DESCRIPTION).getAsString());
        role.setId(j.getAsJsonObject().getAsJsonPrimitive("id").getAsInt());
        role.setJsonString(j.toString());
        role.setName(attributes.getAsJsonPrimitive(Role.ATTR_NAME).getAsString());
        parsePrivileges(attributes.getAsJsonObject(Role.ATTR_PRIVILEGES), role);

        return role;
    }

    public Role doGetRole(int id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(ROLE_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return createEntity(jsonResult.getAsJsonObject().get("data"));

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
                role.addPrivilege(RolePrivilege.valueOf(key));
            }
        }
    }
}
