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
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.rest.MockRestClient;
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
public class LocationManagerTest {

    private final String TEST_RESOURCE_1 = "LocationManagerTest001.json";
    private final String TEST_KEY_1 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/locations/cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_ID = "cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_BARCODE = "0000000005";
    private final String TEST_LOCATION_NAME = "R-ABC";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private LocationManager manager;

    @Inject
    private DynEnumManager dynEnumManager;

    @Module
    @Classes(cdi = true, value = {LdapClient.class, MockLdapAdapter.class, MockLdapAdapterFactory.class,
            MockRestClient.class, SignalsConfig.class,
            DynEnum.class, DynEnumDbService.class, DynEnumManager.class,
            SignalsEntity.class, SignalsEntityDTO.class, SignalsEntityDbService.class,
            GroupDbService.class, GroupManager.class, GroupRestService.class,
            RoleDbService.class, RoleManager.class, RoleRestService.class,
            UserDbService.class, UserManager.class, UserRestService.class,
            LocationType.class, LocationTypeDbService.class,
            LocationDbService.class, LocationManager.class, LocationRestService.class})
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{LocationType.class.getName(),
                LocationEntity.class.getName(), DynEnum.class.getName(),
                SignalsEntity.class.getName()
        });
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @Before
    public void testSetup() {
        dynEnumManager.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_1,
                getClass().getResourceAsStream(TEST_RESOURCE_1));
    }


    @Test
    public void locationManagerTest() {
        SignalsEntityDTO dto = new SignalsEntityDTO();
        dto.setId(TEST_LOCATION_ID);
        manager.fetchSingleLocation(dto);

        LocationEntity loc = manager.loadById(TEST_LOCATION_ID, false);
        assertEquals("Location name mismatch", TEST_LOCATION_NAME, loc.getName());
        assertEquals("Location barcode mismatch", TEST_LOCATION_BARCODE, loc.getBarcode());
    }
}
