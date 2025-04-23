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

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Manager for location types (inventory/types API endpoint)
 */

@Stateless
public class LocationTypeManager {

    private final static Logger logger = LogManager.getLogger(LocationTypeManager.class);

    @Inject
    private LocationTypeDbService dbService;

    @Inject
    private LocationTypeRestService restService;

    public LocationType getDbLocationType(String id) {
        return dbService.loadById(id);
    }

    public List<LocationType> getSnbLocationTypes() {
        List<LocationType> locationTypes = restService.doGetLocationTypes();
        return locationTypes;
    }

    public void save(List<LocationType> ltypes) {
        for (LocationType lt : ltypes) {
            dbService.save(lt);
        }
    }


}
