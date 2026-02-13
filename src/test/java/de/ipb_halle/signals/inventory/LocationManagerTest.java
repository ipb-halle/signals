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
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.users.LdapClient;
import de.ipb_halle.signals.users.MockLdapAdapter;
import de.ipb_halle.signals.users.MockLdapAdapterFactory;
import de.ipb_halle.signals.users.GroupDbService;
import de.ipb_halle.signals.users.GroupManager;
import de.ipb_halle.signals.users.GroupRestService;
import de.ipb_halle.signals.users.RoleDbService;
import de.ipb_halle.signals.users.RoleManager;
import de.ipb_halle.signals.users.RoleRestService;
import de.ipb_halle.signals.users.UserDbService;
import de.ipb_halle.signals.users.UserManager;
import de.ipb_halle.signals.users.UserRestService;
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
public class LocationManagerTest {

    private final String TEST_RESOURCE_1 = "LocationManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/locations/cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_ID = "cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_BARCODE = "0000000005";
    private final String TEST_LOCATION_NAME = "R-ABC";

    @Inject
    private RestClient mockRestClient;

    @Inject
    private LocationManager manager;

    @Before
    public void testSetup() {
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }


    @Test
    public void locationManagerTest() {

        LocationEntity loc = manager.getSnbLocation(TEST_LOCATION_ID);
        manager.save(loc);
        assertEquals("Location name mismatch", TEST_LOCATION_NAME, loc.getName());

        loc = manager.getDbLocation(TEST_LOCATION_ID);
        assertEquals("Location barcode mismatch", TEST_LOCATION_BARCODE, loc.getBarcode());
    }
}
