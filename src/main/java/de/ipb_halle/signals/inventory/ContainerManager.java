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

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.entity.SignalsIEntityDTO;
import de.ipb_halle.signals.users.UserManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manager for containers. This class orchestrates fetching container
 * data from a remote system (via REST) and saving it locally in a
 * database. It also provides methods to augment container data with
 * relevant user and location information.
 */

@Stateless
public class ContainerManager {

    @Inject
    private ContainerDbService containerDbService;

    @Inject
    private ContainerRestService containerRestService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private UserManager userManager;

    @Inject
    private LocationManager locationManager;

    @Inject
    private ContainerTypeDbService containerTypeDbService;

    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    /**
     * Fetches containers created/updated within a specified date range
     * from the remote system, and saves them locally. Filters out container
     * type definitions if they appear in the fetched data.
     *
     * <ol>
     *     <li>Loads container type IDs from the local database.</li>
     *     <li>Loads matching entities (of container type) from the Signals database.</li>
     *     <li>Processes and saves each container locally, skipping type definitions.</li>
     * </ol>
     *
     * @param dateRange an array of dates; the first element is the start date,
     *                  and the second element (if present) is the end date
     */
    public void manageContainers(Date[] dateRange) {
        // 1) Loads set of container type ids
        Set<String> containerTypeIds = containerTypeDbService.getContainerTypeIds();

        for (String id : containerTypeIds) {
            logger.warn("Container manager:-> containerType ID={} \n", id);

        }

        // 2) Loads signals entities by date range and type
        EntityType[] entityTypes = {EntityType.valueOf(ContainerEntity.ENTITY_TYPE_CONTAINER)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        // 3) Loads all containers from db
        List<SignalsIEntityDTO> containers = signalsEntityDbService.load(cmap);

        // 4) Processes container sequentially
        for (SignalsIEntityDTO dto : containers) {
            // filter out type definitions, if signals DB put them together
            if (!containerTypeIds.contains(dto.getId())) {
                doGetContainer(dto.getId());
            }
        }
    }

    /**
     * Fetches a container by its ID from the remote REST service and saves it
     * into the local database.
     *
     * @param id the ID of the container to fetch
     */
    private void doGetContainer(String id) {
        ContainerEntity containerEntity = containerRestService.doGetContainer(id).createEntity();
        containerDbService.save(containerEntity);
        logger.info("ContainerManager:-> Fetched & saved container with ID={}", id);
    }

    /**
     * Augments a {@link Container} with user and location data. Replaces
     * the simple references to {@code createdBy}, {@code updatedBy}, and
     * {@code location} with fully loaded objects from the local database.
     *
     * @param ct the container to augment
     */
    public void augmentContainer(Container ct) {
        ct.setCreatedBy(userManager.getUser(ct.getCreatedBy().getId()));
        ct.setUpdatedBy(userManager.getUser(ct.getUpdatedBy().getId()));
        ct.setLocation(locationManager.loadById(ct.getLocation().getId(), true));
    }

    /**
     * Loads a container by its ID from the local database.
     *
     * @param id the ID of the container
     * @return the loaded container, or {@code null} if not found
     */
    public Container loadById(String id) {
        return containerDbService.loadById(id);
    }

    /**
     * Fetches a container by its ID from the remote REST service (without
     * saving it locally).
     *
     * @param id the ID of the container
     * @return the container from the remote system
     */
    public Container getSnbContainer(String id) {
        return containerRestService.doGetContainer(id);
    }


    /**
     * Attempts to load a container by its ID from the local database.
     * If not found locally, it is fetched from the remote REST service.
     * If {@code augmented} is true, the container is augmented with user
     * and location data.
     *
     * @param id        the ID of the container
     * @param augmented whether the container should be augmented
     * @return the container
     */
    public Container getContainer(String id, boolean augmented) {
        Container ct = containerDbService.loadById(id);
        if (ct == null) {
            ct = containerRestService.doGetContainer(id);
        }
        if (augmented) {
            augmentContainer(ct);
        }
        return ct;
    }

    /**
     * Saves the given container to the local database. The container is first
     * converted into a {@link ContainerEntity}, then persisted.
     *
     * @param c the container to save
     */
    public void save(Container c) {
        containerDbService.save(c.createEntity());
    }
}
