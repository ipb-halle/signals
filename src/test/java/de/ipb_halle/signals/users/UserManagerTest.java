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
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.reporting.HtmlReport;
import de.ipb_halle.signals.rest.MockRestClient;

import java.util.HashMap;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

@RunWith(ApplicationComposer.class)
public class UserManagerTest {

    private final String TEST_RESOURCE_1 = "UserManagerTest001.json";
    private final String TEST_KEY_1 =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users?page%5Blimit%5D=20&page%5Boffset%5D=0&enabled=true";
//      "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users?page%5Blimit%5D=20&q=ThreeLast&page%5Boffset%5D=0&enabled=true";
    private final String TEST_RESOURCE_2 = "UserManagerTest002.json";
    private final String TEST_KEY_2 =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users/107";
    private final String TEST_RESOURCE_3 = "UserManagerTest003.json";
    private final String TEST_KEY_3a = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users/102/systemGroups";
    private final String TEST_KEY_3b =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users/107/systemGroups";
    private final String TEST_KEY_3c =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users/122/systemGroups";

    private final String TEST_RESOURCE_4 = "UserManagerTest004.json";
    private final String TEST_KEY_4 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/users?page%5Blimit%5D=20&page%5Boffset%5D=0&enabled=false";



    private final String TEST_USER1_ID = "102";
    private final String TEST_USER1_ALIAS = "USR3";
    private final String TEST_USER1_FIRST_NAME = "ThreeFirst";
    private final String TEST_USER1_LAST_NAME = "ThreeLast";
    private final String TEST_USER2_ID = "122";
    private final String TEST_USER2_LAST_NAME = "ThreeLast";
    private final String TEST_USER2_FIRST_NAME = "FiveFirst";
    private final String TEST_ROLE1_ID = "1";
    private final String TEST_ROLE1_NAME = "System Admin";
    private final String TEST_ROLE3_ID = "3";
    private final String TEST_ROLE3_NAME = "Standard User";
    private final String TEST_ROLE4_ID = "4";
    private final String TEST_ROLE4_NAME = "Inventory Admin";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private UserManager manager;

    @Inject
    private RoleDbService roleDbService;

    @Inject
    private UserDbService userDbService;

    @Module
    @Classes(cdi = true, value = { LdapClient.class, MockLdapAdapter.class, MockLdapAdapterFactory.class,
        MockRestClient.class, SignalsConfig.class, 
        GroupDbService.class, GroupManager.class, GroupRestService.class,
        RoleDbService.class, RoleManager.class, RoleRestService.class, 
        UserDbService.class, UserManager.class, UserRestService.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ UserEntity.class.getName() });
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
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_3a,
            getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_3b,
            getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_3c,
            getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_4,
            getClass().getResourceAsStream(TEST_RESOURCE_4));



        Role role = new Role();
        role.setId(TEST_ROLE1_ID);
        role.setName(TEST_ROLE1_NAME);
        roleDbService.save(role);
        role = new Role();
        role.setId(TEST_ROLE3_ID);
        role.setName(TEST_ROLE3_NAME);
        roleDbService.save(role);
        role = new Role();
        role.setId(TEST_ROLE4_ID);
        role.setName(TEST_ROLE4_NAME);
        roleDbService.save(role);
    }


    @Test
    public void syncFromSnbTest() {

        manager.syncDbUsersFromSnb(new RuntimeConfig());
        User user = userDbService.loadById(TEST_USER1_ID);


        assertEquals("user alias mismatch", TEST_USER1_ALIAS, user.getAlias());
        assertEquals("user first name mismatch", TEST_USER1_FIRST_NAME, user.getFirstName());
        assertEquals("user last name mismatch", TEST_USER1_LAST_NAME, user.getLastName());

        user = userDbService.loadById(TEST_USER2_ID);
        assertEquals("user first name mismatch", TEST_USER2_FIRST_NAME, user.getFirstName());
        assertEquals("user last name mismatch", TEST_USER2_LAST_NAME, user.getLastName());
    }

    @Test
    public void syncUsersFromLdapTest() {
        RuntimeConfig config = new RuntimeConfig(true, false, true, true, true);
        UserSynchronizationContext context = new UserSynchronizationContext(config);
        context.groupsByDN = new HashMap<> ();
        context.rolesByDN = new HashMap<> ();
        AccessManager.prepareReport(context,  new HtmlReport());
        manager.syncUsersFromLdap(context);
        String html = context.report.render();
        // System.out.printf("\n******************************\n%s\n******************************\n", html);
        assertTrue("report contains 'ae@somewhere.invalid'", context.report.render().contains("ae@somewhere.invalid"));
    }
}
