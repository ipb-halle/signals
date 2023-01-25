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
package de.ipb_halle.signals;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.ejb.Local;
import jakarta.inject.Inject;


/** 
 * Manager for signals entities (entities API endpoint) 
 */

@Local
public class SignalsEntityRestService implements RestService<SignalsEntity> {

    public final String SIGNALS_ENTITY_ENDPOINT = "/entities";
    public final String PARAMETER_INCLUDE_TYPES = "includeTypes";

    @Inject
    private RestClient restClient;
    

    public SignalsEntity createEntity(JsonElement json) {
        SignalsEntity entity = new SignalsEntity();
        JsonObject attributes = json.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        entity.setId(json.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        entity.setType(attributes.getAsJsonPrimitive(RestHelper.ATTR_TYPE).getAsString());
        return entity;
    }

    public List<SignalsEntity> doGetEntities(String includeTypes) {
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(SIGNALS_ENTITY_ENDPOINT)
            .putUriParameter(PARAMETER_INCLUDE_TYPES, includeTypes);

        RestResultIterator<SignalsEntity> iter = new RestResultIterator<> (restClient, this, true); 
        List<SignalsEntity> entities = new ArrayList<> ();

        while(iter.hasNext()) {
            entities.add(iter.next());
        }
        return entities;
    }
}
