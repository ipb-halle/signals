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
package de.ipb_halle.signals;

import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import java.util.List;
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
    private RestClient mockRestClient;

    @Inject
    private SignalsEntityManager manager;

    @Before
    public void testSetup() {
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_2,
            getClass().getResourceAsStream(TEST_RESOURCE_2));
    }


    @Test
    public void entityTest() {

        List<SignalsEntity> entities = manager.getSnbEntities(TEST_ENTITY_TYPE);
        manager.save(entities);
        SignalsEntity entity = manager.getDbEntity(TEST_LOCATION_ID);

        assertEquals("entity type mismatch", TEST_ENTITY_TYPE, entity.getType());
    }
}
