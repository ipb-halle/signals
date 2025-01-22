/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */
package de.ipb_halle.signals.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestReplyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * service for field values (not a real REST service)
 */
public class LocationTypeFieldValuesParser implements RestReplyParser<List<FieldValue>> {

    private Logger logger = LoggerFactory.getLogger(LocationTypeFieldValuesParser.class);


    /**
     * ATTR_COLLECTION currently not implemented!
     */
    public List<FieldValue> parseReply(JsonElement json) {
        if (json.isJsonArray()) {
            return parseFieldValueArray(json.getAsJsonArray());
        }
        return parseFieldValueObject(json.getAsJsonObject());
    }

    private List<FieldValue> parseFieldValueArray(JsonArray json) {
        List<FieldValue> fieldValueList = new ArrayList<>();
        Iterator<JsonElement> iter = json.iterator();
        while (iter.hasNext()) {
            JsonElement jsonElement = iter.next().getAsJsonObject();
            FieldValue fieldValue = new FieldValue()
                    .setLinkType(FieldValue.LinkType.FIELD_ID)
                    .setFieldId(RestHelper.parseString(jsonElement.getAsJsonObject(), RestHelper.ATTR_ID));
            if (jsonElement.getAsJsonObject().getAsJsonObject("value") != null ) {
                logger.trace("LTFVP:-> there is a value!");
                fieldValue.setValue(jsonElement.getAsJsonObject().getAsJsonObject("value").toString());
            }
            if (jsonElement.getAsJsonObject().getAsJsonObject(FieldValue.ATTR_CONTENT) != null) {
                logger.trace("LTFVP:-> there is a content!");
                fieldValue.setValue(jsonElement.getAsJsonObject().getAsJsonObject(FieldValue.ATTR_CONTENT).toString());
            }else {
                logger.trace("LTFVP:-> there is nothing\"\"");
                fieldValue.setValue("");
            }

            fieldValueList.add(fieldValue);
        }
        return fieldValueList;
    }

    private List<FieldValue> parseFieldValueObject(JsonObject fieldTitleValuePair) {
        /**
         * fieldTitleValuePair contains e.g.:
         * {"CAS Number":{"value":"141-78-6"},"Chemical Name":{"value":"ethyl acetate"},"Description":{"value":""},"Exact Mass":{"value":"88.05243"},
         * "Material Library Type":{"value":"Compounds"},"Materials Access":{"value":["IPB"]},"Molecular Formula":{"value":"C<sub>4</sub>H<sub>8</sub>O<sub>2</sub>"},
         * "Molecular Weight":{"value":"88.11 g/mol"},"Name":{"value":"Compound000010"}}
         */
        List<FieldValue> fieldValues = new ArrayList<>();

        for (String fieldName : fieldTitleValuePair.keySet()) {
            /**
             *  fieldsJsonObject keySet contains, e.g.:
             *  Chemical Name, Description, Exact Mass, Material Library Type, Materials Access, Molecular Formula, Molecular Weight, Name
             */
            FieldValue fieldValue = new FieldValue()
                    .setLinkType(FieldValue.LinkType.FIELD_TITLE)
                    .setFieldTitle(fieldName)
                    .setValue(fieldTitleValuePair.get(fieldName).getAsJsonObject().get("value").toString());
            fieldValues.add(fieldValue);
        }
        return fieldValues;
    }
}
