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
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


/** 
 * Manager for signals entities (entities API endpoint) 
 */

@Stateless
public class SignalsEntityManager {

    @Inject
    private SignalsEntityDbService dbService;

    @Inject
    private SignalsEntityRestService restService;


    public SignalsEntity getDbEntity(String id) {
        return dbService.loadById(id);
    }


    public List<SignalsEntity> getSnbEntities(String includeTypes) {
        return restService.doGetEntities(includeTypes);
    }

    public void save(List<SignalsEntity> entities) {
        for (SignalsEntity e : entities) {
            dbService.save(e);
        }
    }
}

