/*
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.reporting.HtmlReport;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;

@ExtendWith(PostgresqlContainerExtension.class)
public abstract class UserManagerTest {

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
    @DeploymentElement(mock="de.ipb_halle.signals.rest.MockRestClient")
    private RestClient mockRestClient;

    @Inject
    @DeploymentElement(mock="de.ipb_halle.signals.users.MockLdapAdapterFactory")
    private LdapAdapterFactory ldapAdapterFactory;

    @DeploymentElement(mock="de.ipb_halle.signals.users.MockLdapAdapter")
    private LdapAdapter ldapAdapter;

    @Inject
    @DeploymentElement
    private UserManager manager;

    @Inject
    @DeploymentElement
    private RoleDbService roleDbService;

    @Inject
    @DeploymentElement
    private UserDbService userDbService;

    @BeforeAll
    public void testSetup() {
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_2,
            getClass().getResourceAsStream(TEST_RESOURCE_2));
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_3a,
            getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_3b,
            getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
            TEST_KEY_3c,
            getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
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

        Assertions.assertEquals(TEST_USER1_ALIAS, user.getAlias(), "user alias mismatch");
        Assertions.assertEquals(TEST_USER1_FIRST_NAME, user.getFirstName(), "user first name mismatch");
        Assertions.assertEquals(TEST_USER1_LAST_NAME, user.getLastName(), "user last name mismatch");

        user = userDbService.loadById(TEST_USER2_ID);
        Assertions.assertEquals(TEST_USER2_FIRST_NAME, user.getFirstName(), "user first name mismatch");
        Assertions.assertEquals(TEST_USER2_LAST_NAME, user.getLastName(), "user last name mismatch");
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
        Assertions.assertTrue(context.report.render().contains("ae@somewhere.invalid"), "report contains 'ae@somewhere.invalid'");
    }
}
