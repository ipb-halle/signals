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

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.UpdateConfig;
import de.ipb_halle.signals.dynEnum.DynEnum;
import de.ipb_halle.signals.dynEnum.DynEnumDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;

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
public class SignalsEntityManagerTest {

    private final String TEST_RESOURCE_1 = "SignalsEntityManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities?includeTypes=location&page%5Blimit%5D=20&page%5Boffset%5D=0";
    private final String TEST_RESOURCE_2 = "SignalsEntityManagerTest002.json";
    private final String TEST_KEY_2 =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities?includeTypes=location&page[offset]=20&page[limit]=20";
    private final String TEST_LOCATION_ID = "location:cfa1802a-6470-42b3-8c8b-9fe025c82717:ivt";

    private final String TEST_ENTITY_TYPE = "location";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private SignalsEntityManager manager;

    @Inject
    private DynEnumManager dynEnumManager;

    @Module
    @Classes(cdi = true, value = { MockRestClient.class, SignalsConfig.class,
        SignalsEntity.class, SignalsEntityDbService.class, SignalsEntityManager.class,
        SignalsEntityRestService.class, DynEnum.class, DynEnumManager.class, DynEnumDbService.class,
            SignalsEntityDTO.class, EntityType.class})
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ SignalsEntity.class.getName(),
        DynEnum.class.getName(), EntityType.class.getName()});
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @Before
    public void testSetup() {
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_2,
            getClass().getResourceAsStream(TEST_RESOURCE_2));
    }


    @Test
    public void entityTest() {
        dynEnumManager.allowEnumDiscovery();
        EntityType[] includedTypes = new EntityType[] { EntityType.valueOf(TEST_ENTITY_TYPE) };
        manager.fetchSnbEntities(null, includedTypes, new UpdateConfig());
        SignalsEntityDTO entity = manager.getDbEntity(TEST_LOCATION_ID);
        System.out.print(entity.dump());
        assertEquals("entity type mismatch", TEST_ENTITY_TYPE, entity.getType().getValue());
    }
}
