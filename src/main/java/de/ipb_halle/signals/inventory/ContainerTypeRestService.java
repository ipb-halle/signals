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

import javax.ejb.Stateless;
import javax.inject.Inject;


/** 
 * Rest service for container types 
 */

@Stateless
public class ContainerTypeRestService implements RestService<ContainerType> {

    public final String CONTAINER_TYPE_ENDPOINT = "/inventory/types";

    @Inject
    private RestClient restClient;

    public ContainerType createEntity(JsonElement j) {
        ContainerType ct = new ContainerType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        ct.setId(j.getAsJsonObject().getAsJsonPrimitive(ContainerType.ATTR_ID).getAsString());
        ct.setDescription(attributes.getAsJsonPrimitive(ContainerType.ATTR_DESCRIPTION).getAsString());
        ct.setJsonString(j.toString());
        ct.setName(attributes.getAsJsonPrimitive(ContainerType.ATTR_NAME).getAsString());
        return ct;
    }

    public List<ContainerType> doGetContainerTypes() {
        List<ContainerType> containerTypes = new ArrayList<> ();
        restClient.reset()
            .setMethod(Method.GET)
            .setEndpoint(CONTAINER_TYPE_ENDPOINT)
            .putUrlParameter("entityType","container");

        RestResultIterator<ContainerType> iter = new RestResultIterator<> (restClient, this, true);

        while(iter.hasNext()) {
            containerTypes.add(iter.next());
        }
        return containerTypes;
    }
}


