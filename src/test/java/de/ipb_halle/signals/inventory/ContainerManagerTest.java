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

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.UpdateConfig;
import de.ipb_halle.signals.inventory.LocationEntity;
import de.ipb_halle.signals.inventory.LocationManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.users.User;
import de.ipb_halle.signals.users.UserManager;
import java.util.Date;
import java.util.Properties;
import jakarta.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(CdiTestRunner.class)
public class ContainerManagerTest {

    private final String TEST_RESOURCE_1 = "ContainerManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/containers/ef16c7af-a763-49f2-b218-294ac02fc224";
    private final String TEST_CONTAINER_ID = "ef16c7af-a763-49f2-b218-294ac02fc224";
    private final String TEST_CONTAINER_BARCODE =  "0000000026";
    private final String TEST_CONTAINER_NAME = "item00000021";
    private final String TEST_USER1_ID = "116";
    private final String TEST_USER1_FIRST = "TwoFirst";
    private final String TEST_USER2_ID = "102";
    private final String TEST_USER2_FIRST = "ThreeFirst";
    private final String TEST_LOCATION_ID = "e13620f4-4933-4275-83cc-3194a4be6607";
    private final String TEST_LOCATION_NAME = "Grid96Well_Test";

    @Inject
    private RestClient mockRestClient;

    @Inject
    private UserManager userManager;

    @Inject
    private LocationManager locationManager;

    @Inject
    private ContainerManager manager;

    @Before
    public void testSetup() {
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));

        User user = new User();
        user.setEnabled(true);
        user.setFirstName(TEST_USER1_FIRST);
        user.setLastName("TwoLast");
        user.setId(TEST_USER1_ID);
        user.setEmail("user.two@someplace.invalid");
        userManager.save(new UpdateConfig(), user);

        user = new User();
        user.setEnabled(true);
        user.setFirstName(TEST_USER2_FIRST);
        user.setLastName("ThreeLast");
        user.setId(TEST_USER2_ID);
        user.setEmail("user.three@someplace.invalid");
        userManager.save(new UpdateConfig(), user);

        LocationEntity loc = new LocationEntity();
        loc.setId(TEST_LOCATION_ID);
        loc.setName(TEST_LOCATION_NAME);
        loc.setCreatedAt(new Date(1000000000));
        loc.setCreatedBy(TEST_USER1_ID);
        loc.setUpdatedAt(new Date(1200000000));
        loc.setUpdatedBy(TEST_USER2_ID);
        locationManager.save(loc);
    }


    @Test
    public void containerManagerTest() {

        Container ct = manager.getSnbContainer(TEST_CONTAINER_ID);
        manager.save(ct);
        assertEquals("Container name mismatch", TEST_CONTAINER_NAME, ct.getName());

        ct = manager.getContainer(TEST_CONTAINER_ID, true);
        assertEquals("Container barcode mismatch", TEST_CONTAINER_BARCODE, ct.getBarcode());
        assertEquals("Created by Id matches", TEST_USER1_ID, ct.getCreatedBy().getId());
        assertEquals("Created by first name matches", TEST_USER1_FIRST, ((User) ct.getCreatedBy()).getFirstName());
        assertEquals("Location name matches", TEST_LOCATION_NAME, ((LocationEntity) ct.getLocation()).getName());
        assertEquals("Location updated by matches", TEST_USER2_ID, ((LocationEntity) ct.getLocation()).getUpdatedBy());
    }
}
