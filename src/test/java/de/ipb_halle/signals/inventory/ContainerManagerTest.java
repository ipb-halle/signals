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

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.users.MockLdapClient;
import de.ipb_halle.signals.users.UserDbService;
import de.ipb_halle.signals.users.UserRestService;
import de.ipb_halle.signals.users.UserEntity;
import de.ipb_halle.signals.users.UserManager;
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
import static org.junit.Assert.assertThrows;

@RunWith(ApplicationComposer.class)
public class ContainerManagerTest {

    private final String TEST_RESOURCE_1 = "ContainerManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/containers/ef16c7af-a763-49f2-b218-294ac02fc224";
    private final String TEST_CONTAINER_ID = "ef16c7af-a763-49f2-b218-294ac02fc224";
    private final String TEST_CONTAINER_BARCODE =  "0000000026";
    private final String TEST_CONTAINER_NAME = "item00000021";
    private final Integer TEST_USER_ID = 116;
    private final String TEST_USER_FIRST = "TwoFirst";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private UserManager userManager;

    @Inject
    private ContainerManager manager;

    @Module
    @Classes(cdi = true, value = { MockLdapClient.class, MockRestClient.class, SignalsConfig.class,
        UserEntity.class, UserDbService.class, UserManager.class, UserRestService.class,
        Container.class, ContainerDbService.class, ContainerManager.class, ContainerRestService.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ ContainerType.class.getName()});
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

        UserEntity user = new UserEntity();
        user.setEnabled(true);
        user.setFirstName(TEST_USER_FIRST);
        user.setLastName("TwoLast");
        user.setId(TEST_USER_ID);
        user.setEmail("user.two@someplace.invalid");
        userManager.save(user);
    }


    @Test
    public void containerManagerTest() {

        Container ct = manager.getSnbContainer(TEST_CONTAINER_ID);
        manager.save(ct);
        assertEquals("Container name mismatch", TEST_CONTAINER_NAME, ct.getName());

        ct = manager.getContainer(TEST_CONTAINER_ID);
        assertEquals("Container barcode mismatch", TEST_CONTAINER_BARCODE, ct.getBarcode());
        assertEquals("Created by Id matches", TEST_USER_ID, ct.getCreatedBy().getId());
        assertEquals("Created by first name matches", TEST_USER_FIRST, ((UserEntity) ct.getCreatedBy()).getFirstName());
    }
}
