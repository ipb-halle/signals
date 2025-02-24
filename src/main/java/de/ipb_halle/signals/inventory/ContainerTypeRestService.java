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
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDesignation;
import de.ipb_halle.signals.field.FieldParser;
import de.ipb_halle.signals.rest.*;
import jakarta.ejb.Local;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Rest service for container types
 */

@Local
public class ContainerTypeRestService implements RestReplyParser<ContainerType> {

    public final String CONTAINER_TYPE_ENDPOINT = "/inventory/types";
    private final static String CONTAINER_TYPE_ENTITY_PREFIX = "container:";
    private final static String CONTAINER_TYPE_ENTITY_SUFFIX = ":ivt";


    @Inject
    private RestClient restClient;

    @Inject
    private FieldParser fieldParser;

    @Inject
    private DynEnumManager dynEnumManager;

    public ContainerType parseReply(JsonElement j) {
        ContainerType ct = new ContainerType();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        ct.setId(CONTAINER_TYPE_ENTITY_PREFIX
                        + j.getAsJsonObject().getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString()
                        + CONTAINER_TYPE_ENTITY_SUFFIX);

        ct.setDescription(attributes.getAsJsonPrimitive(RestHelper.ATTR_DESCRIPTION).getAsString());
        ct.setName(attributes.getAsJsonPrimitive(ContainerType.ATTR_NAME).getAsString());
        /*
         * We ignore the ContainerType.ATTR_ATTACHMENTS attribute,
         * which may contain e.g. a ContainerType icon (e.g. "DefaultImage_Container_Bottle.png")
         */
        if (attributes.has(RestHelper.ATTR_FIELDS)) {
            parseFields(attributes.getAsJsonArray(RestHelper.ATTR_FIELDS), ct);
        }

        return ct;
    }



    private void parseFields(JsonArray fields, ContainerType ct) {
        Iterator<JsonElement> iter = fields.iterator();
        while (iter.hasNext()) {
            Field field = fieldParser.parseReply(iter.next());
            // NOTE: field ids are NOT unique within Signals Inventory
            field.setId(field.getId() + ":" + ct.getId());
            field.setDesignation((FieldDesignation) dynEnumManager.valueOf(FieldDesignation.valueOf(FieldDesignation.CONTAINER)));
            field.setDefiningEntityId(ct.getId());
            ct.addField(field);
        }
    }

    public List<ContainerType> doGetContainerTypes() {
        List<ContainerType> containerTypes = new ArrayList<>();
        restClient.reset()
                .setMethod(Method.GET)
                .setEndpoint(CONTAINER_TYPE_ENDPOINT)
                .putUriParameter("entityType", "container");

        RestResultIterator<ContainerType> iter = new RestResultIterator<>(restClient, this, true);

        while (iter.hasNext()) {
            containerTypes.add(iter.next());
        }
        return containerTypes;
    }
}


