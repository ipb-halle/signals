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
package de.ipb_halle.signals.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Rest helper
 */
public class RestHelper {

    public final static String SNB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // common attribute names
    public final static String ATTR_ATTRIBUTES = "attributes";
    public final static String ATTR_RELATIONSHIPS = "relationships";
    public final static String ATTR_DATA = "data";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_DIGEST = "digest";
    public final static String ATTR_FIELDS = "fields";
    public final static String ATTR_ID = "id";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_LIBRARY = "library";
    public final static String ATTR_CHILDREN = "children";
    public final static String ATTR_META = "meta";
    public final static String ATTR_LINKS = "links";
    public final static String ATTR_SELF = "self";
    public final static String ANCESTORS = "ancestors";
    public final static String CHILDREN = "children";

    public static String formatDate(Date d) {
        DateFormat df = new SimpleDateFormat(SNB_DATE_FORMAT);
        return df.format(d);
    }

    public static String getAsJsonString(JsonObject json, String attribute) {
        return getAsJsonString(json, attribute, null);
    }

    public static String getAsJsonString(JsonObject json, String attribute, String dflt) {
        if (jsonHas(json, attribute)) {
            return json.get(attribute).toString();
        }
        return dflt;
    }

    private static JsonElement getFromPath(JsonElement json, String[] paths, int index, int last) {
        String element = paths[index];
        if ((json != null) && json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has(element)) {
                if (index < last) {
                    return getFromPath(obj.get(element), paths, index + 1, last);
                } else {
                    return obj.get(element);
                }
            }
        }
        return null;
    }

    public static JsonElement getFromPath(JsonElement json, String path) {
        String[] elements = path.split("\\.");
        return getFromPath(json, elements, 0, elements.length - 1);
    }

    public static JsonPrimitive getPrimitiveFromPath(JsonElement json, String path) {
        JsonElement elem = getFromPath(json, path);
        if ((elem != null) && elem.isJsonPrimitive()) {
            return elem.getAsJsonPrimitive();
        }
        return null;
    }

    private static boolean jsonHas(JsonObject json, String attribute) {
        return (json != null) && json.has(attribute);
    }

    public static Boolean parseBool(JsonPrimitive json) {
        return parseBool(json, null);
    }

    public static Boolean parseBool(JsonPrimitive json, Boolean dflt) {
        if (json != null) {
            return json.getAsBoolean();
        }
        return dflt;
    }

    public static Boolean parseBool(JsonObject json, String attribute) {
        return parseBool(json, attribute, null);
    }

    public static Boolean parseBool(JsonObject json, String attribute, Boolean dflt) {
        if (jsonHas(json, attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsBoolean();
        }
        return dflt;
    }


    public static Date parseDate(JsonPrimitive json) {
        return parseDate(json, null);
    }

    public static Date parseDate(JsonPrimitive json, Date dflt) {
        if (json != null) {
            DateFormat df = new SimpleDateFormat(SNB_DATE_FORMAT);
            try {
                return df.parse(json.getAsString());
            } catch (Exception e) {
            }
        }
        return dflt;
    }

    public static Date parseDate(JsonObject json, String attribute) {
        return parseDate(json, attribute, null);
    }

    public static Date parseDate(JsonObject json, String attribute, Date dflt) {
        if (jsonHas(json, attribute)) {
            return parseDate(json.getAsJsonPrimitive(attribute), dflt);
        }
        return dflt;
    }

    public static Integer parseInt(JsonPrimitive json) {
        return parseInt(json, null);
    }

    public static Integer parseInt(JsonPrimitive json, Integer dflt) {
        if (json != null) {
            return json.getAsInt();
        }
        return dflt;
    }

    public static Integer parseInt(JsonObject json, String attribute) {
        return parseInt(json, attribute, null);
    }

    public static Integer parseInt(JsonObject json, String attribute, Integer dflt) {
        if (jsonHas(json, attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsInt();
        }
        return dflt;
    }

    public static Long parseLong(JsonPrimitive json) {
        return json.getAsLong();
    }

    public static Long parseLong(JsonPrimitive json, Long dflt) {
        if (json != null) {
            return json.getAsLong();
        }
        return dflt;
    }

    public static Long parseLong(JsonObject json, String attribute) {
        return parseLong(json, attribute, null);
    }

    public static Long parseLong(JsonObject json, String attribute, Long dflt) {
        if (jsonHas(json, attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsLong();
        }
        return dflt;
    }

    public static String parseString(JsonPrimitive json) {
        return parseString(json, null);
    }

    public static String parseString(JsonPrimitive json, String dflt) {
        if (json != null) {
            return json.getAsString();
        }
        return dflt;
    }

    public static String parseString(JsonObject json, String attribute) {
        return parseString(json, attribute, null);
    }

    public static String parseString(JsonObject json, String attribute, String dflt) {
        if (jsonHas(json, attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsString();
        }
        return dflt;
    }
}
