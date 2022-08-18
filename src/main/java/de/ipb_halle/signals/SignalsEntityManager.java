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

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/** 
 * Manager for signals entities (entities API endpoint) 
 */

@Stateless
public class SignalsEntityManager {

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    @Resource(name="signalsConfig")
    private SignalsConfig signalsConfig;
    

    private class SignalsEntityIterator implements Iterator<SignalsEntity> {
        private Client client;
        private JsonElement jsonResult;
        private Iterator<JsonElement> jsonIterator;

        public SignalsEntityIterator(Client c, String includeTypes) {
            client = c;
            initialFetch(includeTypes);
        }

        private void initialFetch(String includeTypes) {
            try {
                client.setMethod(Method.GET)
                    .setEndpoint("/entities")
                    .putUrlParameter("includeTypes", includeTypes)
                    .putUrlParameter("page[offset]", "1")
                    .putUrlParameter("page[limit]", "20")
                    .execute();

                jsonResult = JsonParser.parseString(client.getResponse());
                jsonIterator = jsonResult.getAsJsonObject().getAsJsonArray("data").iterator();

//              System.out.println(client.getResponse());

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
                jsonIterator = jsonResult.getAsJsonObject().getAsJsonArray("data").iterator();

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
            JsonObject links = jsonResult.getAsJsonObject().getAsJsonObject("links");
            if (links.has("next")) {
                
                fetchPage(links.getAsJsonPrimitive("next").getAsString());
                return jsonIterator.hasNext();
            }
            return false;
        }

        public SignalsEntity next() {
            if (hasNext()) {
                return new SignalsEntity(jsonIterator.next());
            }
            throw new NoSuchElementException();
        }
    }

    /**
     * default constructor
     */
    public SignalsEntityManager() {
        System.out.println("SignalsEntityManager() called.");
    }

    public void fetchSignalsEntities(String includeTypes) {
        SignalsEntityIterator iter = new SignalsEntityIterator(new Client(signalsConfig), includeTypes);

        while(iter.hasNext()) {
            SignalsEntity entity = iter.next();
            entity.dump();
            save(entity);
        }
    }

    public void save(SignalsEntity entity) {
        this.em.merge(entity);
    }

}


