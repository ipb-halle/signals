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
package de.ipb_halle.signals.users;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.UpdateConfig;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import jakarta.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

@RunWith(CdiTestRunner.class)
public class RoleManagerTest {

    private final String TEST_RESOURCE_1 = "RoleManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/roles";
    private final String TEST_ROLE_ID = "1";
    private final String TEST_ROLE_DESCRIPTION = "Users with this role have all privileges.";
    private final String TEST_ROLE_NAME = "System Admin";

    @Inject
    private RestClient mockRestClient;

    @Inject
    private RoleManager manager;

    @Inject
    private RoleDbService roleDbService;

    @Before
    public void testSetup() {
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }


    @Test
    public void roleManagerTest() {

        UserSynchronizationContext context = new UserSynchronizationContext(
                new UpdateConfig());
        manager.syncDbRolesFromSnb(context);
        Role role = roleDbService.loadById(TEST_ROLE_ID);

        assertEquals("Role name mismatch", TEST_ROLE_NAME, role.getName());
        assertEquals("Role description mismatch", TEST_ROLE_DESCRIPTION, role.getDescription());
        assertTrue("Role has privilege canViewMaterials", role.hasPrivilege("canViewMaterials"));
    }
}
