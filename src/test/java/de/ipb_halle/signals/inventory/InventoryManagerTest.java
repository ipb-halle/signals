/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.RuntimeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InventoryManagerTest {

    private InventoryManager inventoryManager;

    private LocationManager locationManager;
    private ContainerManager containerManager;
    private LocationTypeManager locationTypeManager;
    private ContainerTypeManager containerTypeManager;

    @BeforeEach
    public void setUp() {
        inventoryManager = new InventoryManager();

        locationManager = mock(LocationManager.class);
        containerManager = mock(ContainerManager.class);
        locationTypeManager = mock(LocationTypeManager.class);
        containerTypeManager = mock(ContainerTypeManager.class);

        inventoryManager.setLocationManager(locationManager);
        inventoryManager.setContainerManager(containerManager);
        inventoryManager.setLocationTypeManager(locationTypeManager);
        inventoryManager.setContainerTypeManager(containerTypeManager);
    }


    @Test
    public void manageInventoryTest() {
        Date[] dateRange = {new Date(), new Date()};
        inventoryManager.manageInventory(dateRange);

        verify(locationTypeManager).save(any());
        verify(containerTypeManager).save(any());
        verify(locationManager).manageLocations(dateRange);
        verify(containerManager).manageContainers(dateRange);
    }

    @Test
    public void testImportLocation(){
        RuntimeConfig runtimeConfig = mock(RuntimeConfig.class);
        String id = "testId";

        inventoryManager.importLocation(runtimeConfig, id);

        verify(locationManager).importLocation(runtimeConfig, id);
    }

    @Test
    public void testImportContainer() {
        RuntimeConfig runtimeConfig = mock(RuntimeConfig.class);
        String id = "testId";

        inventoryManager.importContainer(runtimeConfig, id);

        verify(containerManager).importContainer(runtimeConfig, id);
    }
}
