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
package de.ipb_halle.signals.inventory;


import javax.ejb.Stateless;
import javax.inject.Inject;


/** 
 * Manager for signals locations (inventory/location API endpoint) 
 */

@Stateless
public class LocationManager {

    @Inject
    private LocationDbService dbService;

    @Inject
    private LocationRestService restService;


    public LocationEntity getSnbLocation(String id) {
        return restService.doGetLocation(id);
    }

    public LocationEntity getDbLocation(String id) {
        return dbService.loadById(id);
    }

    public void save(LocationEntity loc) {
        dbService.save(loc);
    }
}
