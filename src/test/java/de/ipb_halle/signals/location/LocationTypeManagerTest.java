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
import de.ipb_halle.signals.RestClient;
import de.ipb_halle.signals.TestBase;
import java.util.Properties;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(ApplicationComposer.class)
public class LocationTypeManagerTest {

    private final String TEST_RESOURCE_1 = "LocationTypeManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/types?page%5Blimit%5D=20&page%5Boffset%5D=0&entityType=location";
    private final String TEST_LOCATION_TYPE_ID = "017929f3-cd0e-466c-ac94-c21ce5fe8c31";
    private final String TEST_LOCATION_TYPE_NAME = "Cabinet";

    @Inject
    private RestClientFactory restClientFactory;

    @Inject
    private LocationTypeManager manager;

    @Module
    @Classes(cdi = true, value = { RestClientFactory.class, RestClient.class,
        LocationType.class, LocationTypeManager.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ LocationType.class.getName()});
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @Before
    public void testSetup() {
        TestBase.prepareRestClients(restClientFactory,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }

    @Test
    public void locationTypeManagerTest() {

        manager.fetchLocationTypes();

        LocationType lt = manager.loadById(TEST_LOCATION_TYPE_ID);
        assertEquals("LocationType name mismatch", lt.getName(), TEST_LOCATION_TYPE_NAME);
    }
}
