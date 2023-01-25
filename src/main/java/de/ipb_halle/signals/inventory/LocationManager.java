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

import de.ipb_halle.signals.users.UserManager;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Manager for signals locations (inventory/location API endpoint) 
 */

@Stateless
public class LocationManager {

    @Inject
    private LocationDbService dbService;

    @Inject
    private LocationRestService restService;

    @Inject
    private UserManager userManager;

    private Logger logger =  LoggerFactory.getLogger(ContainerManager.class);

    public void augmentLocation(LocationEntity loc) {
/*

        cannot yet augment LocationEntity (need to introduce DTO) 

        loc.setCreatedBy(userManager.getUser(loc.getCreatedBy().getId()));
        loc.setUpdatedBy(userManager.getUser(loc.getUpdatedBy().getId()));
*/
    }

    public LocationEntity getSnbLocation(String id) {
        return restService.doGetLocation(id);
    }

    public LocationEntity getDbLocation(String id) {
        return dbService.loadById(id);
    }

    public LocationEntity getLocation(String id, boolean augmented) {
        LocationEntity loc = dbService.loadById(id);
        if (loc == null) {
            loc = restService.doGetLocation(id);
        }
        if (augmented) {
            augmentLocation(loc);
        }
        return loc;
    }

    public void save(LocationEntity loc) {
        dbService.save(loc);
    }
}
