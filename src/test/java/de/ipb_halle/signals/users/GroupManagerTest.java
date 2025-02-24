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

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.rest.MockRestClient;
import java.util.Properties;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;


@RunWithApplicationComposer
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public class GroupManagerTest {

    private final String TEST_RESOURCE_1 = "GroupManagerTest001.json";
    private final String TEST_KEY_1 =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/groups";
    private final String TEST_GROUP_ID = "132";
    private final String TEST_GROUP_DESCRIPTION = "Research Group 1, Gamma department";
    private final String TEST_GROUP_NAME = "RG Gamma 1";
    private final String TEST_GROUP_TYPE = "group";
    private final boolean TEST_GROUP_SYSTEM = true;

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private GroupManager manager;

    @Inject
    private GroupDbService groupDbService;

    @Module
    @Classes(cdi = true, value = { LdapClient.class, MockLdapAdapter.class, MockLdapAdapterFactory.class,
        MockRestClient.class, SignalsConfig.class,
        Group.class, GroupDbService.class, GroupManager.class, GroupRestService.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ Group.class.getName() });
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @BeforeAll
    public void testSetup() {
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }


    @Test
    public void groupManagerTest() {

        Group testGroup = groupDbService.loadById(TEST_GROUP_ID);
        if (testGroup != null) {
            testGroup.setDescription("--- WRONG ---");
            testGroup.setName("--- WRONG ---");
            groupDbService.save(testGroup);
        }

        manager.syncDbGroupsFromSnb(new RuntimeConfig());
        Group group = groupDbService.loadById(TEST_GROUP_ID);

        Assertions.assertEquals(TEST_GROUP_NAME, group.getName(), "Group name mismatch");
        Assertions.assertEquals(TEST_GROUP_DESCRIPTION, group.getDescription(), "Group description mismatch");
        Assertions.assertEquals(TEST_GROUP_TYPE, group.getType(), "Group type mismatch");
        Assertions.assertEquals(TEST_GROUP_SYSTEM, group.isSystem(), "Group systemGroup mismatch");
    }
}
