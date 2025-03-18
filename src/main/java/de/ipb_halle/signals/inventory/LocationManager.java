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

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.users.UserManager;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Manager for signals locations (inventory/location API endpoint)
 */

@Stateless

/**
 * Manages locations in the Signals inventory system.
 * <p>
 * This class handles the import and synchronization of locations between the
 * Signals REST API and the local database.
 * </p>
 *
 * <ul>
 *   <li>{@link #importLocation(RuntimeConfig, String)} - Imports a location from the local database into the Signals REST API.</li>
 *   <li>{@link #manageLocations(Date[])} - Fetches locations from the Signals REST API and saves them into the local database.</li>
 * </ul>
 *
 * <p>Additional functionalities include:</p>
 * <ul>
 *   <li>Loading and saving locations</li>
 *   <li>Augmenting location data with user information</li>
 *   <li>Processing individual locations</li>
 * </ul>
 *
 * @author [Your Name]
 * @version 1.0
 */
public class LocationManager {

    @Inject
    private LocationTypeDbService locationTypeDbService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private LocationDbService locationDbService;

    @Inject
    private LocationRestService locationRestService;

    @Inject
    private UserManager userManager;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private LocationProcessorBean locationProcessorBean;

    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void importLocation(RuntimeConfig runtimeConfig, String id) {
        LocationEntity locationEntity = locationDbService.loadLocationById(id);
        Location location = new Location(locationEntity);


        logger.info("Location Manager:-> importLocation-> location={}\n", location.getId());
        LocationType locationType = locationTypeDbService.loadById(location.getLocationTypeId());
        logger.info("Location Manager:-> importLocation-> locationType={}\n", location.getLocationTypeId());


        if (runtimeConfig.updateSNB) {
            locationRestService.doCreateLocation(locationType, location);
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void manageLocations(Date[] dateRange) {

        // 1) Loads a set of location type ids (checked -> location types are present)
        Set<String> locationTypeIds = locationTypeDbService.getLocationTypIds();

        // 2) Preparing criteria map for loading signals entities by date range and type
        EntityType entityTypes[] = {EntityType.valueOf(LocationEntity.ENTITY_TYPE_LOCATION)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        // 3) Loading all locations from db (checked everything is working)
        List<SignalsEntityDTO> locations = signalsEntityDbService.load(cmap);

        // 4) Process locations (checked, everything is working)
        for (SignalsEntityDTO dto : locations) {
            if (!locationTypeIds.contains(dto.getId())) {
                processLocation(dto.getId());
            }
        }
    }

    /**
     * Fetches a location by its ID from the remote REST service and saves it
     * into the local database.
     *
     * @param locationId the ID of location to fetch
     */
    public void processLocation(String locationId) {
        locationProcessorBean.processSingleLocation(locationId);
    }

    public void save(LocationEntity loc) {
        locationDbService.save(loc);
    }

    public void save(Location loc) {
        locationDbService.save(loc);
    }

    public ILocation loadById(String id, boolean augment) {
        return locationDbService.loadLocationById(id);
    }

    /**
     * Fetches a location by its ID from the remote REST service (without
     * saving it locally).
     *
     * @param id the ID of the location
     * @return the location from the remote system
     */
    public Location getSnbLocation(String id) {
        return locationRestService.doGetLocation(id);
    }

    /**
     * Attempts to load a location by its ID from the local database.
     * If not found locally, it is fetched from the remote REST service.
     * If {@code augmented} is true, the location is augmented with user
     * location data.
     *
     * @param id      the ID of the location
     * @param augment whether the location should be augmented
     * @return the location
     */
    public Location getLocation(String id, boolean augment) {
        LocationEntity locationEntity = locationDbService.loadLocationById(id);
        Location location = new Location(locationEntity);
        if (location == null) {
            location = locationRestService.doGetLocation(id);
        }
        if (augment) {
            augmentLocation(location);
        }
        return location;
    }

    /**
     * Augments a {@link Container} with user and location data. Replaces
     * the simple references to {@code createdBy}, {@code updatedBy}, and
     * {@code location} with fully loaded objects from the local database.
     *
     * @param location the container to augment
     */
    public void augmentLocation(Location location) {
        location.setCreatedBy(userManager.getUser(location.getCreatedBy().getId()));
        location.setUpdatedBy(userManager.getUser(location.getUpdatedBy().getId()));
    }
}
