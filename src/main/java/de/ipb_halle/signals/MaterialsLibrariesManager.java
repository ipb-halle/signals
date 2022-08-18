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

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.NoSuchElementException;


/** 
 * Manager for signals entities (entities API endpoint) 
 */

public class MaterialsLibrariesManager {

    private Config config;

    public MaterialsLibrariesManager(Config cfg) {
        config = cfg;
    }

    private class MaterialsLibrary {
        private JsonElement json;
        private String id;

        public MaterialsLibrary(JsonElement j) {
            json = j;
            id = json.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        }

        public void dump() {
            System.out.println(id);
            System.out.println(json.toString());
            System.out.println("============================================================");
        }
    }


    private Iterator<JsonElement> fetch() {
        JsonElement jsonResult;
        try {
            Client client = new Client(config).setMethod(Method.GET)
                .setEndpoint("/materials/libraries")
                .execute();

            jsonResult = JsonParser.parseString(client.getResponse());
            return jsonResult.getAsJsonObject().getAsJsonArray("data").iterator();

        } catch(UnexpectedResponseCodeException ue) {
            System.out.println("Unexpected code");
        } catch(MalformedURLException me) {
            System.out.println("Malformed URL");
        } catch(IOException ioe) {
            System.out.println("IOException");
            ioe.printStackTrace();
        }
        return null; 
    }

    public void fetchMaterialsLibraries() {
        Iterator<JsonElement> iter = fetch();

        while(iter.hasNext()) {
            new MaterialsLibrary(iter.next()).dump();
        }
    }
}


