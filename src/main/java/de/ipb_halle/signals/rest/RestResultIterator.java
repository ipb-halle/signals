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

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.NoSuchElementException;


/** 
 * Iterator for rest services
 */

public class RestResultIterator<T> implements Iterator {
    private boolean paged;
    private RestClient client;
    private RestService<T> service;
    private JsonElement jsonResult;
    private Iterator<JsonElement> jsonIterator;

    /**
     * constructor
     * @param c the pre-configured RestClient
     * @param p if the request parameters page[offset] and page[limit] should be added to the request.
     * The values for offset and limit are 0 and 20, respectively.
     */
    public RestResultIterator(RestClient c, RestService<T> svc, boolean p) {
        client = c;
        paged = p;
        service = svc;
        if (p) {
            client.putUrlParameter("page[offset]", "0");
            client.putUrlParameter("page[limit]", "20");
        }
        initialFetch();
    }

    private void initialFetch() {
        try {
            client.execute();

            jsonResult = JsonParser.parseString(client.getResponse());
            jsonIterator = jsonResult.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA).iterator();

        } catch(UnexpectedResponseCodeException ue) {
            System.out.println("Unexpected code");
        } catch(MalformedURLException me) {
            System.out.println("Malformed URL");
        } catch(IOException ioe) {
            System.out.println("IOException");
            ioe.printStackTrace();
        }
    }

    private void fetchPage(String url) {
        try {
            System.out.println("fetchPage");
            client.setURL(url)
                .execute();

            jsonResult = JsonParser.parseString(client.getResponse());
            jsonIterator = jsonResult.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA).iterator();

        } catch(UnexpectedResponseCodeException ue) {
            System.out.println("Unexpected code");
        } catch(MalformedURLException me) {
            System.out.println("Malformed URL");
        } catch(IOException ioe) {
            System.out.println("IOException");
            ioe.printStackTrace();
        }
    }

    public boolean hasNext() {
        if (jsonIterator.hasNext()) {
            return true;
        }
        if (paged) {
            JsonObject links = jsonResult.getAsJsonObject().getAsJsonObject("links");
            if (links.has("next")) {
                
                fetchPage(links.getAsJsonPrimitive("next").getAsString());
                return jsonIterator.hasNext();
            }
        }
        return false;
    }

    public T next() {
        if (hasNext()) {
            return service.createEntity(jsonIterator.next());
        }
        throw new NoSuchElementException();
    }
}


