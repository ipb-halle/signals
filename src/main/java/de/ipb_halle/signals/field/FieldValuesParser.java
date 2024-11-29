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
package de.ipb_halle.signals.field;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ipb_halle.signals.LogConfig;
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

public class FieldValuesParser implements RestReplyParser<List<FieldValue>> {

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
        List<FieldValue> fieldValues = new ArrayList<>();
        Iterator<JsonElement> iter = json.iterator();
        while (iter.hasNext()) {
            JsonObject jsonObj = iter.next().getAsJsonObject();
            FieldValue field = new FieldValue()
                    .setLinkType(FieldValue.LinkType.FIELD_ID)
                    .setFieldId(RestHelper.parseString(jsonObj, RestHelper.ATTR_ID))
                    .setValue(jsonObj.get(FieldValue.ATTR_CONTENT).toString());
            fieldValues.add(field);
        }
        return fieldValues;
    }

    private List<FieldValue> parseFieldValueObject(JsonObject fields) {
        List<FieldValue> fieldValues = new ArrayList<>();

        for (String fieldName : fields.keySet()) {
            FieldValue fieldValue = new FieldValue()
                    .setLinkType(FieldValue.LinkType.FIELD_TITLE)
                    .setFieldTitle(fieldName)
                    .setValue(fields.get(fieldName).getAsJsonObject().get("value").toString());
            fieldValues.add(fieldValue);
        }
        return fieldValues;
    }
}
