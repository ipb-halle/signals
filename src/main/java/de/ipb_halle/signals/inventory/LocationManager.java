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

import de.ipb_halle.signals.entity.*;
import de.ipb_halle.signals.users.UserManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Manager for signals locations (inventory/location API endpoint)
 */

@Stateless
public class LocationManager {

    @Inject
    private LocationTypeDbService locationTypeDbService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private LocationDbService dbService;

    @Inject
    private LocationRestService restService;

    @Inject
    private UserManager userManager;

    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    public void augmentLocation(LocationEntity loc) {
/*
        cannot yet augment LocationEntity (need to introduce DTO)

        loc.setCreatedBy(userManager.getUser(loc.getCreatedBy().getId()));
        loc.setUpdatedBy(userManager.getUser(loc.getUpdatedBy().getId()));
*/
    }

    public LocationEntity loadById(String id, boolean augmented) {
        return dbService.loadById(id);
    }

    public void fetchLocations(Date[] dateRange) {
        Set<String> locationTypeIds = locationTypeDbService.getLocationTypIds();
        EntityType entityTypes[] = {EntityType.valueOf(LocationEntity.ENTITY_TYPE_LOCATION)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);
        List<SignalsEntityDTO> locations = signalsEntityDbService.load(cmap);
        for (SignalsEntityDTO entityDTO : locations) {
            /*
             * LocationTypes (e.g. building, room, shelf, ...) are represented as
             * Locations in the signalsentities table (db)! We need to exclude them.
             */
            if (!locationTypeIds.contains(entityDTO.getStrippedId())) {
                fetchSingleLocation(entityDTO);
            }
        }
    }

    /**
     * fetch single location via inventory/location/EID endpoint and
     * save the entity in the database.
     *
     * @param entityDTO
     * @return the location entity
     */
    public LocationEntity fetchSingleLocation(SignalsEntityDTO entityDTO) {
        LocationEntity location = restService.doGetLocation(entityDTO.getStrippedId());
        dbService.save(location);
        return location;
    }

    public void save(LocationEntity loc) {
        dbService.save(loc);
    }

}
