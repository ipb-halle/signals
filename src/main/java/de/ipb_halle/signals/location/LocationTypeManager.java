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
package de.ipb_halle.signals.location;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.Method;
import de.ipb_halle.signals.RestClient;
import de.ipb_halle.signals.RestClientFactory;
import de.ipb_halle.signals.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/** 
 * Manager for location types (inventory/types API endpoint) 
 */

@Stateless
public class LocationTypeManager {

    public final String LOCATION_TYPE_ENDPOINT = "/inventory/types";

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    @Inject
    private RestClientFactory restClientFactory;

    private class LocationTypeIterator implements Iterator<LocationType> {
        private RestClient client;
        private JsonElement jsonResult;
        private Iterator<JsonElement> jsonIterator;

        public LocationTypeIterator(RestClient c) {
            client = c;
            initialFetch();
        }

        private void initialFetch() {
            try {
                client.setMethod(Method.GET)
                    .setEndpoint(LOCATION_TYPE_ENDPOINT)
                    .putUrlParameter("entityType","location")
                    .putUrlParameter("page[offset]", "0")
                    .putUrlParameter("page[limit]", "20")
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
                System.out.println("fetchPage");
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

        public LocationType next() {
            if (hasNext()) {
                return LocationType.createLocationType(jsonIterator.next());
            }
            throw new NoSuchElementException();
        }
    }

    public void fetchLocationTypes() {
        LocationTypeIterator iter = new LocationTypeIterator(restClientFactory.getRestClient());

        while(iter.hasNext()) {
            LocationType lt = iter.next();
            save(lt);
        }
    }

    public LocationType loadById(String id) {
        return this.em.find(LocationType.class, id);
    }

    public void save(LocationType lt) {
        this.em.merge(lt);
    }
}


