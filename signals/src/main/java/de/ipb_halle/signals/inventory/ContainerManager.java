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
import de.ipb_halle.signals.users.UserManager;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Manager for containers 
 */

@Stateless
public class ContainerManager {

    @Inject
    private ContainerDbService dbService;

    @Inject
    private ContainerRestService restService;

    @Inject
    private UserManager userManager;

    @Inject
    private LocationManager locationManager;

    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    public void augmentContainer(Container ct) {
        ct.setCreatedBy(userManager.getUser(ct.getCreatedBy().getId()));
        ct.setUpdatedBy(userManager.getUser(ct.getUpdatedBy().getId()));
        ct.setLocation(locationManager.getLocation(ct.getLocation().getId(), true));
    }

    public Container getDbContainer(String id) {
        return dbService.loadById(id);
    }

    public Container getSnbContainer(String id) {
        return restService.doGetContainer(id);
    }

    public Container getContainer(String id, boolean augmented) {
        Container ct = dbService.loadById(id);
        if (ct == null) {
            ct = restService.doGetContainer(id);
        }
        if (augmented) {
            augmentContainer(ct);
        }
        return ct;
    }

    public void save(Container c) {
        dbService.save(c);
    }
}
