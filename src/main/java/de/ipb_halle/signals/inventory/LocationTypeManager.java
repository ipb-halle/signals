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

import de.ipb_halle.signals.rest.RestResultIterator;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

/** 
 * Manager for location types (inventory/types API endpoint) 
 */

@Stateless
public class LocationTypeManager {

    @Inject
    private LocationTypeDbService dbService;

    @Inject
    private LocationTypeRestService restService;


    public LocationType loadById(String id, boolean augment) {
        return dbService.loadById(id);
    }

    /**
     * Fetch all location types from Signals Notebook and
     * update the local database.
     */
    public void fetchLocationTypes() {
        RestResultIterator<LocationType> locationTypeIterator = restService.doGetLocationTypes();
        while (locationTypeIterator.hasNext()) {
            dbService.save(locationTypeIterator.next());
        }
    }
}
