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

import java.io.FileReader;

import javax.ejb.Singleton;

/** 
 * Configuration reader for Signals tool 
 */

@Singleton
public class Config {


    private JsonObject jsonConfig;

    public Integer getConfigInt(String key) {
        JsonPrimitive value = this.jsonConfig.getAsJsonPrimitive(key);
        if (value != null) {
            return Integer.valueOf(value.getAsInt());
        }
        throw new NullPointerException("getConfigInt(" + key + ") returned null");
    }

    public String getConfigString(String key) {
        JsonPrimitive value = this.jsonConfig.getAsJsonPrimitive(key);
        if (value != null) {
            return value.getAsString();
        }
        throw new NullPointerException("getConfigInt(" + key + ") returned null");
    }

    public void readConfig(String fileName) throws Exception {
        JsonElement element = JsonParser.parseReader(
            new FileReader(fileName));
        if (! element.isJsonObject()) {
            throw new Exception("readConfig() could not parse Json object");
        }
        this.jsonConfig = element.getAsJsonObject();
    }
}


