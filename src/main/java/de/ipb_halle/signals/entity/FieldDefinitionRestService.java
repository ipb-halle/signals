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


// import javax.ejb.Local;
// import javax.inject.Inject;

/** 
 * service for field definitions (not a real REST service)
 */

//@Local
public class FieldDefinitionRestService implements RestService<FieldDefinition> {


    public FieldDefinition createEntity(JsonElement j) {
        FieldDefinition fd = new FieldDefinition();
        fd.setId(j.getAsJsonObject().getAsJsonPrimitive(FieldDefinition.ATTR_ID).getAsString());
        JsonObject def = j.getAsJsonObject().getAsJsonObject(FieldDefinition.ATTR_DEFINITION);

        fd.setAttributeListEid(RestHelper.parseString(def, FieldDefinition.ATTR_ATTRIBUTE_LIST_EID));
        fd.setDefaultUnit(RestHelper.parseString(def, FieldDefinition.ATTR_DEFAULT_UNIT));
        fd.setFieldType(FieldType.valueOf(RestHelper.parseString(def, FieldDefinition.ATTR_FIELD_TYPE))); 
        fd.setHidden(RestHelper.parseBool(def, FieldDefinition.ATTR_HIDDEN));
        fd.setKey(RestHelper.parseString(def, FieldDefinition.ATTR_KEY));
        fd.setRequired(RestHelper.parseBool(def, FieldDefinition.ATTR_REQUIRED));
        fd.setTitle(RestHelper.parseString(def, FieldDefinition.ATTR_TITLE));
        fd.setUserDefined(RestHelper.parseBool(def, FieldDefinition.ATTR_USER_DEFINED));

        if (def.has(FieldDefinition.ATTR_MEASURES)) {
            parseMeasures(def.getAsJsonArray(FieldDefinition.ATTR_MEASURES), fd);
        }

        if (def.has(FieldDefinition.ATTR_OPTIONS)) {
            parseOptions(def.getAsJsonArray(FieldDefinition.ATTR_OPTIONS), fd);
        }

        return fd;
    }

    private void parseMeasures(JsonArray jArray, FieldDefinition fd) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            JsonObject j = iter.next().getAsJsonObject();
            String measure = j.getAsJsonPrimitive(Quality.ATTR_MEASURE).getAsString();
            Quality q = MeasureMapper.getQuality(measure); 
            fd.addMeasure(q);
        }
    }

    private void parseOptions(JsonArray jArray, FieldDefinition fd) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            String option = iter.next().getAsJsonPrimitive().getAsString();
            fd.addOption(option);
        }
    }
}
