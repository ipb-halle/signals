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

import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.SignalsConfig;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class LdapClientTest {

    public final static String TEST_ALL_USERS_DN = "CN=All Users, OU=groups, DC=somewhere, DC=invalid";
    public final static String TEST_ALL_USERS_NAME = "All Users";
    public final static String TEST_GOETHE_DN = "CN=Goethe\\, JohannW, OU=poet, DC=somewhere, DC=invalid";

    @Resource
    SignalsConfig signalsConfig;

    @Inject
    LdapClient ldapClient;

    @Module
    @Classes(cdi = true, value = { MockLdapAdapterFactory.class, SignalsConfig.class,
        LdapClient.class, Role.class, RoleEntity.class, RolePriv.class, 
        Group.class, User.class })
    public EjbJar app() {
        return new EjbJar();
    }

/*
    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ UserEntity.class.getName() });
    }
*/

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }


    @Test
    public void getGroupTest() {
        try {
            assertEquals("Name of Group matches", TEST_ALL_USERS_NAME, ldapClient.getGroup(TEST_ALL_USERS_DN).getName());
        } catch (LdapConnectionErrorException ex) {
            fail("LdapConnectionErrorException");
        }
    }

    @Test
    public void getMembersTest() {
        try {
            assertTrue("'All Users' has member 'Goethe'", ldapClient.getMembers(TEST_ALL_USERS_DN, true).contains(TEST_GOETHE_DN));
        } catch (LdapConnectionErrorException ex) {
            fail("LdapConnectionErrorException");
        }
    }

    @Test
    public void getMembershipTest() {
        assertTrue("'Goethe' is member in 'All Users'", ldapClient.getMemberships(TEST_GOETHE_DN, true).contains(TEST_ALL_USERS_DN));
    }
}
