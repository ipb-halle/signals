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
package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.TestHelper;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.users.Group;
import de.ipb_halle.signals.users.GroupDbService;
import de.ipb_halle.signals.users.User;
import de.ipb_halle.signals.users.UserDbService;
import de.ipb_halle.signals.users.UserReference;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class SignalsEntityManagerTest {

    private final String TEST_MOCK_RESOURCE = "SignalsEntityManagerTestMockResources.json";

    private final String TEST_LOCATION_ID = "location:cfa1802a-6470-42b3-8c8b-9fe025c82717:ivt";

    private final String TEST_ENTITY_TYPE = "location";

    @PersistenceContext
    private EntityManager em;

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private SignalsEntityManager manager;

    @Inject
    @DeploymentElement
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @Inject
    @DeploymentElement
    private UserDbService userDbService;

    @Inject
    @DeploymentElement
    private GroupDbService groupDbService;

    private TestHelper testHelper;

    @BeforeAll
    public void testSetup() {
        TestBase.prepareRestClients(mockRestClient, this.getClass(), TEST_MOCK_RESOURCE);
        dynEnumManager.allowEnumDiscovery();
        testHelper = new TestHelper()
                .setDynEnumManager(dynEnumManager)
                .setGroupDbService(groupDbService)
                .setSignalsEntityDbService(signalsEntityDbService)
                .setUserDbService(userDbService)
                .setPrefix("");

        Group g1 = testHelper.createGroup("124");
        testHelper.createUser("100",  new Group [0]);
        testHelper.createUser("102",  new Group [0]);
        testHelper.createUser("103",  new Group [0]);
        testHelper.createUser("104",  new Group [] { g1 });
    }

    @Test
    public void entityTest() {
        dynEnumManager.allowEnumDiscovery();
        EntityType[] includedTypes = new EntityType[]{EntityType.valueOf(TEST_ENTITY_TYPE)};
        manager.manageSignalsEntities(null, includedTypes, new RuntimeConfig());
        SignalsEntityDTO entity = signalsEntityDbService.loadById(TEST_LOCATION_ID,
                new SignalsEntityDbService.SEloadInfo(true, true, true));
        System.out.print(entity.dump());
        Assertions.assertEquals(TEST_ENTITY_TYPE, entity.getType().getValue(), "entity type mismatch");

        /* ToDo: test sharing */
        UserReference u1 = new UserReference("100");
        UserReference u2 = new UserReference("102");
        UserReference u3 = new UserReference("104");
        Assertions.assertTrue(entity.hasPermission(u1, Share.SharePermission.READ), "Sharing: 100 can read");
        Assertions.assertFalse(entity.hasPermission(u1, Share.SharePermission.WRITE), "Sharing 100 cannot write");
        Assertions.assertTrue(entity.hasPermission(u2, Share.SharePermission.READ), "Sharing: 102 can read");
        Assertions.assertTrue(entity.hasPermission(u2, Share.SharePermission.WRITE), "Sharing: 102 can write");
        Assertions.assertTrue(entity.hasPermission(u2, Share.SharePermission.FULL_CONTROL), "Sharing: 102 has full control");
        Assertions.assertTrue(entity.hasPermission(u3, Share.SharePermission.READ), "Sharing: 104 can read via group");
        Assertions.assertTrue(entity.hasPermission(u3, Share.SharePermission.WRITE), "Sharing: 104 can write via group");
        Assertions.assertFalse(entity.hasPermission(u1, Share.SharePermission.WRITE), "Sharing 100 cannot write");
        Assertions.assertFalse(entity.hasPermission(u3, Share.SharePermission.FULL_CONTROL), "Sharing: 104 can't control via group");
    }
}
