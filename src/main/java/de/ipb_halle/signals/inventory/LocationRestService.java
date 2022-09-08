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
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.ejb.Local;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Manager for signals locations (inventory/location API endpoint) 
 */

@Local
public class LocationRestService implements RestService<Location> {

    public final String LOCATION_ENDPOINT = "/inventory/locations/%s";

    @Inject
    private RestClient restClient;

    private Logger logger = LoggerFactory.getLogger(LocationRestService.class.getName());
    
    public Location createEntity(JsonElement j) {
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        Location loc = new Location();
        loc.setId(j.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        loc.setBarcode(attributes.getAsJsonPrimitive(Location.ATTR_BARCODE).getAsString());
        loc.setName(attributes.getAsJsonPrimitive(Location.ATTR_NAME).getAsString());
        loc.setDescription(attributes.getAsJsonPrimitive(Location.ATTR_DESCRIPTION).getAsString());
        loc.setGrid(attributes.getAsJsonPrimitive(Location.ATTR_GRID).getAsBoolean());
        loc.setJsonString(j.toString());
        loc.setTypeId(attributes.getAsJsonPrimitive(Location.ATTR_TYPE_ID).getAsString());
        loc.setTypeName(attributes.getAsJsonPrimitive(Location.ATTR_TYPE_NAME).getAsString());

        parseAncestor(loc, attributes.getAsJsonArray(Location.ATTR_ANCESTORS));
        return loc;
    }


    private JsonElement fetch(String id) {
        try {
            restClient.setMethod(Method.GET)
                .setEndpoint(String.format(LOCATION_ENDPOINT, id))
                .execute();

            JsonElement jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().get(RestHelper.ATTR_DATA);

        } catch(UnexpectedResponseCodeException ue) {
           logger.warn("Unexpected code");
        } catch(MalformedURLException me) {
            logger.warn("Malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException",  (Throwable) ioe);
        }
        return null;
    }

    public Location doGetLocation(String id) {
        return createEntity(fetch(id));
    }

    public void parseAncestor(Location loc, JsonArray ancestors) {
        if (ancestors.size() > 0) {
            JsonObject obj = ancestors.get(0).getAsJsonObject();
            loc.setAncestorId(obj.getAsJsonPrimitive(Location.ATTR_ANCESTOR_ID).getAsString());
            loc.setAncestorName(obj.getAsJsonPrimitive(Location.ATTR_ANCESTOR_NAME).getAsString());
            return;
        }
        loc.setAncestorId(null);
        loc.setAncestorName(null);
    }
}
