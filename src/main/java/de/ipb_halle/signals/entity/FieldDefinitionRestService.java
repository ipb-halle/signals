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


    /**
     * ATTR_COLLECTION currently not implemented!
     */
    public FieldDefinition createEntity(JsonElement json) {
        FieldDefinition fd = new FieldDefinition();
        JsonObject j = json.getAsJsonObject();
        fd.setId(j.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());

        if (j.has(FieldDefinition.ATTR_DEFINITION)) {
            parseDefinition(j.getAsJsonObject(FieldDefinition.ATTR_DEFINITION), fd);
        } else {
            fd.setAttributeListEid(RestHelper.parseString(j, FieldDefinition.ATTR_ATTRIBUTE));
            fd.setCalculated(RestHelper.parseBool(j, FieldDefinition.ATTR_CALCULATED));
            fd.setDefinedBy(RestHelper.parseString(j, FieldDefinition.ATTR_DEFINED_BY));
            fd.setFieldType(FieldType.valueOfAnyCase(RestHelper.parseString(j, FieldDefinition.ATTR_DATA_TYPE)));
            fd.setHidden(RestHelper.parseBool(j, FieldDefinition.ATTR_HIDDEN));
            fd.setRequired(RestHelper.parseBool(j, FieldDefinition.ATTR_MANDATORY));
            fd.setTitle(RestHelper.parseString(j, RestHelper.ATTR_NAME));

            if (j.has(FieldDefinition.ATTR_MEASURE_OPTIONS)) {
                parseMeasures(j.getAsJsonArray(FieldDefinition.ATTR_MEASURE_OPTIONS), fd);
            }
            if (j.has(FieldDefinition.ATTR_OPTIONS)) {
                parseOptions(j.getAsJsonArray(FieldDefinition.ATTR_OPTIONS), fd);
            }
        }
        return fd;
    }

    private void parseDefinition(JsonObject def, FieldDefinition fd) {
        fd.setAttributeListEid(RestHelper.parseString(def, FieldDefinition.ATTR_ATTRIBUTE_LIST_EID));
        fd.setDefaultUnit(RestHelper.parseString(def, FieldDefinition.ATTR_DEFAULT_UNIT));
        fd.setDefinedBy(RestHelper.parseString(def, FieldDefinition.ATTR_DEFINED_BY));
        fd.setFieldType(FieldType.valueOfAnyCase(RestHelper.parseString(def, FieldDefinition.ATTR_FIELD_TYPE))); 
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
