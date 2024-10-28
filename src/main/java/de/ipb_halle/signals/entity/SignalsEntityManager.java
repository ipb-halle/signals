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

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;


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


    public List<SignalsEntityDTO> getSnbEntities(String includeTypes) {
        return restService.doGetEntities(includeTypes);
    }

    public void save(List<SignalsEntityDTO> entities) {
        for (SignalsEntityDTO e : entities) {
            dbService.save(e);
        }
    }
}

