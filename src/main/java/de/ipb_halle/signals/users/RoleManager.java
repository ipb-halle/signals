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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/** 
 * Manager for signals locations (inventory/location API endpoint) 
 */

@Stateless
public class RoleManager {

    public final String ROLES_ENDPOINT = "/roles";
    public final String ROLE_ENDPOINT = "/roles/%d";

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    @Inject
    private RestClient restClient;
    

    private class RoleIterator implements Iterator<Role> {
        private RestClient client;
        private JsonElement jsonResult;
        private Iterator<JsonElement> jsonIterator;

        public RoleIterator(RestClient c) {
            client = c;
            initialFetch();
        }

        private void initialFetch() {
            try {
                client.setMethod(Method.GET)
                    .setEndpoint(ROLES_ENDPOINT)
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

        private void fetchPage(String url) {
            try {
                client.setURL(url)
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
            if (jsonIterator.hasNext()) {
                return true;
            }
            JsonObject links = jsonResult.getAsJsonObject().getAsJsonObject("links");
            if (links.has("next")) {
                
                fetchPage(links.getAsJsonPrimitive("next").getAsString());
                return jsonIterator.hasNext();
            }
            return false;
        }

        public Role next() {
            if (hasNext()) {
                return Role.createRole(jsonIterator.next());
            }
            throw new NoSuchElementException();
        }
    }


    private JsonElement fetch(int id) {
        try {
            restClient.setMethod(Method.GET)
                .setEndpoint(String.format(ROLE_ENDPOINT, id))
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

    public Role fetchRole(int id) {
        Role role = Role.createRole(fetch(id));
        save(role);
        return role;
    }

    public void fetchRoles() {
        RoleIterator iter = new RoleIterator(restClient);

        while(iter.hasNext()) {
            Role u = iter.next();
            save(u);
        }
    }

    public Role loadById(int id) {
        return this.em.find(Role.class, id);
    }

    public void save(Role u) {
        this.em.merge(u);
    }
}

