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
import de.ipb_halle.signals.reporting.HtmlReport;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import java.net.URL;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
/*
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
*/
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.apache.openejb.junit5.RunWithApplicationComposer;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.CdiExtensions;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
//import org.apache.openejb.util.AnnotationFinder;     wrong type
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.AnnotationFinder;                // geronimo xbean - xbean-finder
import org.apache.xbean.finder.ClassFinder;                     // geronimo xbean - xbean-finder

/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
*/

@RunWithApplicationComposer
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Dependent
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
    private RestClient mockRestClient;

    @Inject
    private UserManager manager;

    @Inject
    private RoleDbService roleDbService;

    @Inject
    private UserDbService userDbService;

    @Module
    @Classes(cdi = true, value = { })
    public EjbJar app() {
        EjbJar jar = new EjbJar();
        // jar.addEnterpriseBean(new ManagedBean(UserManager.class)); --> DuplicateDeploymentExceptionId
        return jar;
    }
/*
    @Module
    public WebModule app() {
        try {
            WebApp webApp = new WebApp();
            WebModule webModule = new WebModule(webApp, "/test",
                Thread.currentThread().getContextClassLoader(), "", "test-module");
            ClassFinder finder = new ClassFinder(Thread.currentThread().getContextClassLoader());
            List<Class<?>> clazzes = finder.findClassesInPackage("de.ipb_halle.signals", true);
            clazzes.add(this.getClass());
            clazzes.add(MockRestClient.class);
            webModule.setFinder(finder);
            return webModule;
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Module
    public Beans cdiModule() {
        Beans beans = new Beans();
        beans.addAlternativeClass(MockRestClient.class);
        beans.addAlternativeClass(MockLdapAdapterFactory.class);
        return beans;
    }
*/

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ UserEntity.class.getName() });
    }

    @Configuration
    public Properties configuration() {
        Properties prop = TestBase.configuration();
        prop.put("openejb.deployments.classpath", "true");
        prop.put("openejb.deployments.classpath.include", ".*target/(classes|test-classes).*");
        prop.put("openejb.cdi.activated", "true");
        prop.put("openejb.cdi.filter.classloader", "false");
        prop.put("openejb.scanning.default.include", "de.ipb_halle.signals.*");
//      prop.put("openejb.log.factory", "org.apache.openejb.util.Log4jLogStreamFactory");
        return prop;
    }

//  @BeforeAll
    @PostConstruct
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

        manager.syncDbUsersFromSnb(new UpdateConfig());
        User user = userDbService.loadById(TEST_USER1_ID);


        Assertions.assertEquals(TEST_USER1_ALIAS, user.getAlias(), "user alias mismatch");
        Assertions.assertEquals(TEST_USER1_FIRST_NAME, user.getFirstName(), "user first name mismatch");
        Assertions.assertEquals(TEST_USER1_LAST_NAME, user.getLastName(), "user last name mismatch");

        user = userDbService.loadById(TEST_USER2_ID);
        Assertions.assertEquals(TEST_USER2_FIRST_NAME, user.getFirstName(), "user first name mismatch");
        Assertions.assertEquals(TEST_USER2_LAST_NAME, user.getLastName(), "user last name mismatch");
    }

/*
    @Test
    public void syncUsersFromLdapTest() {
        UpdateConfig config = new UpdateConfig(true, false, true, true);
        UserSynchronizationContext context = new UserSynchronizationContext(config);
        context.groupsByDN = new HashMap<> ();
        context.rolesByDN = new HashMap<> ();
        AccessManager.prepareReport(context,  new HtmlReport());
        try {
            manager.syncUsersFromLdap(context);
        } catch (LdapConnectionErrorException ex) {
            fail("LdapConnectionErrorException");
        } catch (NamingException ex) {
            fail("NamingException");
        } catch (MissingAttributeException ex) {
            fail("MissingAttributeException");
        } catch (IOException ex) {
            fail("IOException");
        }
        String html = context.report.render();
        // System.out.printf("\n******************************\n%s\n******************************\n", html);
        Assertions.assertTrue(context.report.render().contains("ae@somewhere.invalid"), "report contains 'ae@somewhere.invalid'");
    }
*/
}
