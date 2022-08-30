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
public class LocationManager {

    public final String LOCATION_ENDPOINT = "/inventory/locations/%s";

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    @Inject
    private RestClient restClient;
    


    private JsonElement fetch(String id) {
        try {
            restClient.setMethod(Method.GET)
                .setEndpoint(String.format(LOCATION_ENDPOINT, id))
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

    public Location fetchLocation(String id) {
        Location loc = Location.createLocation(fetch(id));
        save(loc);
        return loc;
    }

    public Location loadById(String id) {
        return this.em.find(Location.class, id);
    }

    public void save(Location loc) {
        this.em.merge(loc);
    }

}

