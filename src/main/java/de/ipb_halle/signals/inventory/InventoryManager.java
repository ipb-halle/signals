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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Manager for containers, container types, locations
 * and location types.
 */

@Stateless
public class InventoryManager {

    @Inject
    private ContainerDbService containerDbService;

    @Inject
    private ContainerRestService containerRestService;

    @Inject
    private ContainerTypeDbService containerTypeDbService;

    @Inject
    private ContainerTypeRestService containerTypeRestService;

    @Inject
    private LocationManager locationManager;

    @Inject
    private LocationTypeManager locationTypeManager;

    @Inject
    private ContainerManager containerManager;

    @Inject
    private ContainerTypeManager containerTypeManager;


    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    public void manageInventory(Date[] dateRange) {
        fetchLocationTypes();
        fetchContainerTypes();
        fetchLocations(dateRange);
        fetchContainers(dateRange);
    }

    public void fetchContainers(Date[] dateRange) {
        containerManager.manageContainers(dateRange);
    }

    public void fetchContainerTypes() {
        containerTypeManager.save(containerTypeManager.getSnbContainerTypes());
    }

    public void fetchLocations(Date[] dateRange) {
        locationManager.fetchLocations(dateRange);
    }

    public void fetchLocationTypes() {
        locationTypeManager.fetchLocationTypes();
    }
}
