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
package de.ipb_halle.signals.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestService;
import java.util.Iterator;


// import jakarta.ejb.Local;
// import jakarta.inject.Inject;

/** 
 * service for field values (not a real REST service)
 */

public class FieldValueRestService implements RestService<FieldValue> {


    /**
     * ATTR_COLLECTION currently not implemented!
     */
    public FieldValue createEntity(JsonElement json) {
        FieldValue value = new FieldValue();
        JsonObject j = json.getAsJsonObject();
        value.setFieldDefinitionId(j.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());
        value.setRawValue(
                    RestHelper.parseBool(
                    RestHelper.getPrimitiveFromPath(
                    j,
                    FieldValue.ATTR_IS_RAW_VALUE)));
        value.setValue(
                    RestHelper.parseString(
                    RestHelper.getPrimitiveFromPath(
                    j, 
                    FieldValue.ATTR_USER_VALUE)));
        return value;
    }
}
