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
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.users.*;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.HashSet;

import static org.mockito.Mockito.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class LocationManagerTest {

    private final String TEST_RESOURCE_1 = "LocationManagerTest001.json";
    private final String TEST_KEY_1 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/locations/location:cfa1802a-6470-42b3-8c8b-9fe025c82717:ivt";
    private final String TEST_LOCATION_ID = "location:cfa1802a-6470-42b3-8c8b-9fe025c82717:ivt";
    private final String TEST_LOCATION_BARCODE = "0000000005";
    private final String TEST_LOCATION_NAME = "R-ABC";

    private final String TEST_USER1_ID = "116";
    private final String TEST_USER1_FIRST = "TwoFirst";
    private final String TEST_USER2_ID = "102";
    private final String TEST_USER2_FIRST = "ThreeFirst";

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.users.MockLdapAdapterFactory")
    private LdapAdapterFactory ldapAdapterFactory;

    @DeploymentElement(mock = "de.ipb_halle.signals.users.MockLdapAdapter")
    private LdapAdapter ldapAdapter;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @Inject
    @DeploymentElement
    private UserManager userManager;

    @Inject
    @DeploymentElement
    private LocationManager locationManager;

    @Inject
    @DeploymentElement
    private SignalsEntityDbService signalsEntityDbService;

    @BeforeAll
    public void testSetup() {
        dynEnumManager.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_1,
                getClass().getResourceAsStream(TEST_RESOURCE_1));

        User user = new User();
        user.setEnabled(true);
        user.setFirstName(TEST_USER1_FIRST);
        user.setLastName("TwoLast");
        user.setId(TEST_USER1_ID);
        user.setEmail("user.two@someplace.invalid");
        userManager.save(new RuntimeConfig(), user);

        user = new User();
        user.setEnabled(true);
        user.setFirstName(TEST_USER2_FIRST);
        user.setLastName("ThreeLast");
        user.setId(TEST_USER2_ID);
        user.setEmail("user.three@someplace.invalid");
        userManager.save(new RuntimeConfig(), user);

        SignalsEntityDTO dto = new SignalsEntityDTO();
        dto.setId(TEST_LOCATION_ID);
        dto.setEid(TEST_LOCATION_ID);
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(LocationEntity.ENTITY_TYPE_LOCATION)));
        dto.setCreatedAt(new Date());
        dto.setCreatedBy(user);
        dto.setEditedBy(user);
        dto.setOwner(user);
        dto.setName("LocationManagerTest_" + TEST_LOCATION_ID);
        signalsEntityDbService.save(dto);
    }


    @Test
    public void locationManagerTest() {

        Location location = locationManager.getSnbLocation(TEST_LOCATION_ID);
        String locationId = location.getId();
        location.getFieldValues().forEach(fieldValue -> fieldValue.setEntityId(locationId));
        locationManager.saveL(location);
        Assertions.assertEquals(TEST_LOCATION_NAME, location.getName(), "Container name mismatch");
        LocationReference lr = new LocationReference();
        lr.setId("testLocationReference");

        location = locationManager.getLocation(TEST_LOCATION_ID, true);
        Assertions.assertEquals(TEST_USER1_ID, location.getCreatedBy().getId(), "Created by Id matches");
        Assertions.assertEquals(TEST_LOCATION_BARCODE, location.getBarcode(), "Location barcode mismatch");
        Assertions.assertEquals(TEST_USER1_FIRST, ((User) location.getCreatedBy()).getFirstName(), "Created by first name matches");
        Assertions.assertEquals(TEST_LOCATION_NAME, location.getName(), "Location name matches");
        Assertions.assertEquals(TEST_USER2_ID, location.getUpdatedBy().getId(), "Location updated by matches");
        Assertions.assertNotNull(lr.dump());
    }

    @Test
    public void testLocationAdditionalMethods() {
        Location location = new Location();
        location.setId("loc:123");
        location.setName("TestLocation");
        location.setDescription("Test Description");
        location.setRows(10);
        location.setColumns(5);
        location.setCreatedAt(new Date());
        location.setUpdatedAt(new Date());
        location.setTypeName("Shelf");
        location.setAncestorId("ancestor:001");
        location.setAncestorName("RootLocation");

        Assertions.assertEquals("Test Description", location.getDescription());
        Assertions.assertEquals(10, location.getRows());
        Assertions.assertEquals(5, location.getColumns());
        Assertions.assertEquals("Shelf", location.getTypeName());
        Assertions.assertEquals("ancestor:001", location.getAncestorId());
        Assertions.assertEquals("RootLocation", location.getAncestorName());
        Assertions.assertNotNull(location.getCreatedAt());
        Assertions.assertNotNull(location.getUpdatedAt());
        Assertions.assertFalse(location.isGrid()); // зависит от реализации

        String stringValue = location.toString();
        Assertions.assertTrue(stringValue.contains("TestLocation"));
        Assertions.assertTrue(stringValue.contains("loc:123"));
    }
}
