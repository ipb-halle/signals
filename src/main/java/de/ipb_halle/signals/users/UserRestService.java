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
 * Signals API REST service for users
 */

@Stateless
public class UserRestService {

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


    @Inject
    private RestClient restClient;
    

    private class UserIterator implements Iterator<User> {
        private RestClient client;
        private JsonElement jsonResult;
        private Iterator<JsonElement> jsonIterator;

        public UserIterator(RestClient c, String query, Boolean enabled) {
            client = c;
            initialFetch(query, enabled);
        }

        private void initialFetch(String query, Boolean enabled) {
            try {
                client.reset()
                    .setEndpoint(USERS_ENDPOINT)
                    .putUrlParameter("page[offset]", "0")
                    .putUrlParameter("page[limit]", "20");

                if (query != null) {
                    client.putUrlParameter("q", query);
                }
                if (enabled != null) {
                    client.putUrlParameter("enabled", enabled ? "true" : "false");
                }
                client.execute();

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

        public User next() {
            if (hasNext()) {
                return User.createUser(jsonIterator.next());
            }
            throw new NoSuchElementException();
        }
    }


    private JsonElement fetch(int id) {
        try {
            restClient.reset()
                .setEndpoint(String.format(USER_ENDPOINT, id))
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
     * POST -- create user
     */
    public User doCreateUser(User user) {
        try {
            restClient.reset()
                .setMethod(Method.POST)
                .setEndpoint(USERS_ENDPOINT)
                .setRequestData(user.prepareJsonString())
                .execute(RestClient.HTTP_CREATED);

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return User.createUser(jsonResult.getAsJsonObject().get("data"));

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
     * GET user by id 
     */
    public User doGetUser(int id) {
        User user = User.createUser(fetch(id));
        return user;
    }

    /**
     * GET users -- obtain list of users 
     */
    public List<User> doGetUsers(String query, Boolean enabled) {
        UserIterator iter = new UserIterator(restClient, query,  enabled);
        ArrayList<User> users = new ArrayList<> ();

        while(iter.hasNext()) {
            users.add(iter.next());
        }
        return users;
    }
}

