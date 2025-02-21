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
import de.ipb_halle.signals.dynEnum.DynEnum;
import de.ipb_halle.signals.dynEnum.DynEnumDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import java.util.Properties;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;



@RunWithApplicationComposer
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationTypeManagerTest {

    private final String TEST_RESOURCE_1 = "LocationTypeManagerTest001.json";
    private final String TEST_KEY_1 =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/types?page%5Blimit%5D=20&page%5Boffset%5D=0&entityType=location";
    private final String TEST_LOCATION_TYPE_ID = "017929f3-cd0e-466c-ac94-c21ce5fe8c31";
    private final String TEST_LOCATION_TYPE_NAME = "Cabinet";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private LocationTypeManager locationTypeManager;

    @Inject
    private DynEnumManager dynEnumManager;

    @Module
    @Classes(cdi = true, value = { MockRestClient.class, SignalsConfig.class, // RestResultIterator.class, RestService.class,
            DynEnumManager.class, DynEnum.class, DynEnumDbService.class,
            LocationType.class, LocationTypeManager.class, LocationTypeDbService.class,  LocationTypeRestService.class})
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ LocationType.class.getName(),
                DynEnum.class.getName()});
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @BeforeAll
    public void testSetup() {
        dynEnumManager.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }

//    @Test
//    public void locationTypeManagerTest() {
//
//        locationTypeManager.fetchLocationTypes();
//
//        LocationType lt = locationTypeManager.loadById(TEST_LOCATION_TYPE_ID, false);
//        Assertions.assertEquals(TEST_LOCATION_TYPE_NAME, lt.getName(), "LocationType name mismatch");
//    }
}
