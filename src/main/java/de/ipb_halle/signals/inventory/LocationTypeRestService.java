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
package de.ipb_halle.signals.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ejb.Local;
import javax.inject.Inject;


/** 
 * Rest service for location types 
 */

@Local
public class LocationTypeRestService implements RestService<LocationType> {

    public final String CONTAINER_TYPE_ENDPOINT = "/inventory/types";

    @Inject
    private RestClient restClient;


    public LocationType createEntity(JsonElement j) {
        LocationType lt = new LocationType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        lt.setId(attributes.getAsJsonPrimitive(LocationType.ATTR_ID).getAsString());
        lt.setDescription(attributes.getAsJsonPrimitive(LocationType.ATTR_DESCRIPTION).getAsString());
        lt.setJsonString(j.toString());
        lt.setName(attributes.getAsJsonPrimitive(LocationType.ATTR_NAME).getAsString());
        return lt;
    }

    public List<LocationType> doGetLocationTypes() {
        List<LocationType> locationTypes = new ArrayList<> ();
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(CONTAINER_TYPE_ENDPOINT)
            .putUrlParameter("entityType","location");

        RestResultIterator<LocationType> iter = new RestResultIterator<> (restClient, this, true);
//      Iterator<LocationType> iter = new ArrayList<LocationType> ().iterator();

        while(iter.hasNext()) {
            locationTypes.add(iter.next());
        }
        return locationTypes;
    }
}


