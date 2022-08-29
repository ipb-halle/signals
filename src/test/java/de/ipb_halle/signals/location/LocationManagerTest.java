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
package de.ipb_halle.signals.location;

import de.ipb_halle.signals.RestClientFactory;
import de.ipb_halle.signals.TestBase;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;


public class LocationManagerTest {

    private final String TEST_RESOURCE_1 = "LocationManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/locations/cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_ID = "cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_BARCODE = "0000000005";
    private final String TEST_LOCATION_NAME = "R-ABC";

    @Inject
    private RestClientFactory restClientFactory;

    @Inject
    private LocationManager manager;

    @Before
    public void testSetup() {
        TestBase.getTestContext(this);

        TestBase.prepareRestClients(restClientFactory,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }


    @Test
    public void entityTest() {

        Location loc = manager.fetchLocation(TEST_LOCATION_ID);
        assertEquals("Location name mismatch", loc.getName(), TEST_LOCATION_NAME);

        loc = manager.loadById(TEST_LOCATION_ID);
        assertEquals("Location barcode mismatch", loc.getBarcode(), TEST_LOCATION_BARCODE);
    }
}
