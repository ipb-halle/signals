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
import de.ipb_halle.signals.users.LdapAdapter;
import de.ipb_halle.signals.users.LdapAdapterFactory;
import de.ipb_halle.signals.users.User;
import de.ipb_halle.signals.users.UserManager;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class ContainerManagerTest {

    private final String TEST_RESOURCE_1 = "ContainerManagerTest001.json";
    private final String TEST_KEY_1 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/containers/container:ef16c7af-a763-49f2-b218-294ac02fc224:ivt";
    private final String TEST_CONTAINER_ID = "container:ef16c7af-a763-49f2-b218-294ac02fc224:ivt";
    private final String TEST_CONTAINER_BARCODE = "0000000026";
    private final String TEST_CONTAINER_NAME = "item00000021";
    private final String TEST_USER1_ID = "116";
    private final String TEST_USER1_FIRST = "TwoFirst";
    private final String TEST_USER2_ID = "102";
    private final String TEST_USER2_FIRST = "ThreeFirst";
    private final String TEST_LOCATION_ID = "location:e13620f4-4933-4275-83cc-3194a4be6607:ivt";
    private final String TEST_LOCATION_NAME = "Grid96Well_Test";

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
    private UserManager userManager;

    @Inject
    @DeploymentElement
    private LocationManager locationManager;

    @Inject
    @DeploymentElement
    private ContainerManager manager;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

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

        Location loc = new Location();
        loc.setId(TEST_LOCATION_ID);
        loc.setName(TEST_LOCATION_NAME);
        loc.setCreatedAt(new Date(1000000000));
        loc.setCreatedBy(TEST_USER1_ID);
        loc.setUpdatedAt(new Date(1200000000));
        loc.setUpdatedBy(TEST_USER2_ID);
        loc.getFieldValues().forEach(fieldValue -> fieldValue.setEntityId(TEST_LOCATION_ID));
        locationManager.save(loc);

        SignalsEntityDTO dto = new SignalsEntityDTO();
        dto.setId(TEST_CONTAINER_ID);
        dto.setEid(TEST_CONTAINER_ID);
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(ContainerEntity.ENTITY_TYPE_CONTAINER)));
        dto.setCreatedAt(new Date());
        dto.setCreatedBy(user);
        dto.setEditedBy(user);
        dto.setOwner(user);
        dto.setName("ContainerManagerTest_" + TEST_CONTAINER_ID);
        signalsEntityDbService.save(dto);
    }


    @Test
    public void containerManagerTest() {

        // String strippedId = TEST_CONTAINER_ID.split(":")[1];
        Container ct = manager.getSnbContainer(TEST_CONTAINER_ID);
        String containerId = ct.getId();
        ct.getFieldValues().forEach(fieldValue -> fieldValue.setEntityId(containerId));
        manager.save(ct);
        Assertions.assertEquals(TEST_CONTAINER_NAME, ct.getName(), "Container name mismatch");

        ct = manager.getContainer(TEST_CONTAINER_ID, true);
        Assertions.assertEquals(TEST_CONTAINER_BARCODE, ct.getBarcode(), "Container barcode mismatch");
        Assertions.assertEquals(TEST_USER1_ID, ct.getCreatedBy().getId(), "Created by Id matches");
        Assertions.assertEquals(TEST_USER1_FIRST, ((User) ct.getCreatedBy()).getFirstName(), "Created by first name matches");
        Assertions.assertEquals(TEST_LOCATION_NAME, ((LocationEntity) ct.getLocation()).getName(), "Location name matches");
        Assertions.assertEquals(TEST_USER2_ID, ((LocationEntity) ct.getLocation()).getUpdatedBy(), "Location updated by matches");
    }
}
