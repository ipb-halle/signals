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

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.users.LdapAdapter;
import de.ipb_halle.signals.users.LdapAdapterFactory;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class LocationManagerTest {

    private final String TEST_RESOURCE_1 = "LocationManagerTest001.json";
    private final String TEST_KEY_1 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/locations/cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_ID = "cfa1802a-6470-42b3-8c8b-9fe025c82717";
    private final String TEST_LOCATION_BARCODE = "0000000005";
    private final String TEST_LOCATION_NAME = "R-ABC";

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private LocationManager manager;

    @Inject
    @DeploymentElement(mock="de.ipb_halle.signals.users.MockLdapAdapterFactory")
    private LdapAdapterFactory ldapAdapterFactory;

    @DeploymentElement(mock="de.ipb_halle.signals.users.MockLdapAdapter")
    private LdapAdapter ldapAdapter;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @BeforeAll
    public void testSetup() {
        dynEnumManager.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_1,
                getClass().getResourceAsStream(TEST_RESOURCE_1));
    }


//    @Test
//    public void locationManagerTest() {
//        SignalsEntityDTO dto = new SignalsEntityDTO();
//        dto.setId(TEST_LOCATION_ID);
//        manager.processLocation(dto);
//
//        LocationEntity loc = manager.loadById(TEST_LOCATION_ID, false);
//        Assertions.assertEquals(TEST_LOCATION_NAME, loc.getName(), "Location name mismatch");
//        Assertions.assertEquals(TEST_LOCATION_BARCODE, loc.getBarcode(), "Location barcode mismatch");
//    }
}
