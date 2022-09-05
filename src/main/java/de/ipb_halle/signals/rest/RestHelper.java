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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 
 * Rest helper 
 */
public class RestHelper {

    public final static String SNB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static String formatDate(Date d) {
        DateFormat df = new SimpleDateFormat(SNB_DATE_FORMAT);
        return df.format(d);
    }

    public static Boolean parseBool(JsonObject json, String attribute) {
        return parseBool(json, attribute, null);
    }

    public static Boolean parseBool(JsonObject json, String attribute, Boolean dflt) {
        if (json.has(attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsBoolean();
        }
        return dflt;
    }

    public static Date parseDate(JsonObject json, String attribute) {
        return parseDate(json, attribute, null);
    }

    public static Date parseDate(JsonObject json, String attribute, Date dflt) {
        if (json.has(attribute)) {
            DateFormat df = new SimpleDateFormat(SNB_DATE_FORMAT);
            try {
                return df.parse(json.getAsJsonPrimitive(attribute).getAsString());
            } catch(Exception e) {
            }
        }
        return dflt;
    }

    public static Integer parseInt(JsonObject json, String attribute) {
        return parseInt(json, attribute, null);
    }

    public static Integer parseInt(JsonObject json, String attribute, Integer dflt) {
        if (json.has(attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsInt();
        }
        return dflt;
    }


    public static String parseString(JsonObject json, String attribute) {
        return parseString(json, attribute, null);
    }

    public static String parseString(JsonObject json, String attribute, String dflt) {
        if (json.has(attribute)) {
            return json.getAsJsonPrimitive(attribute).getAsString();
        }
        return dflt;
    }
}
