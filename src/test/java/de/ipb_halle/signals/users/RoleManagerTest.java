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
import de.ipb_halle.signals.rest.MockRestClient;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Descriptor;
import org.apache.openejb.testing.Descriptors;
import org.apache.openejb.testing.Module;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

@RunWith(ApplicationComposer.class)
public class RoleManagerTest {

    private final String TEST_RESOURCE_1 = "RoleManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/roles";
    private final int TEST_ROLE_ID = 1;
    private final String TEST_ROLE_DESCRIPTION = "Users with this role have all privileges.";
    private final String TEST_ROLE_NAME = "System Admin";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private RoleManager manager;

    @Module
    @Classes(cdi = true, value = { MockRestClient.class, SignalsConfig.class,
        Role.class, RolePriv.class, RoleDbService.class, RoleManager.class, RoleRestService.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ Role.class.getName(), RolePriv.class.getName()});
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
    }


    @Test
    public void roleManagerTest() {

        List<Role> roles = manager.getSnbRoles();
        manager.save(roles);
        Role role = manager.getDbRole(TEST_ROLE_ID);

        assertEquals("Role name mismatch", role.getName(), TEST_ROLE_NAME);
        assertEquals("Role description mismatch", role.getDescription(), TEST_ROLE_DESCRIPTION);
        assertTrue("Role has privilege canViewMaterials", role.hasPrivilege(RolePrivilege.canViewMaterials));
    }
}
