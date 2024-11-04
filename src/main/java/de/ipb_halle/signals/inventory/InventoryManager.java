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

import de.ipb_halle.signals.inventory.LocationManager;
import de.ipb_halle.signals.rest.RestResultIterator;
import de.ipb_halle.signals.users.UserManager;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private LocationDbService locationDbService;

    @Inject
    private LocationRestService locationRestService;

    @Inject
    private LocationTypeManager locationTypeManager;

    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    public void manageInventory() {
    }

    public void syncLocationTypes() {
        locationTypeManager.syncLocationTypes();
    }
}
