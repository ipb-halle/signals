/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.dynEnum;

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.users.UserManagerTest;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Imports no longer needed, when working with the
 * @DeploymentElement annotation.
 *
import de.ipb_halle.signals.TestBase;
import java.util.Properties;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
 */

// @RunWithApplicationComposer
public abstract class DynEnumManagerTest {

    @Inject
    @DeploymentElement
    public DynEnumManager dynEnumMgr;

    /*
     * Will be provided by de.ipb_halle.tda.DeploymentProcessor
     *
    @Module
    @Classes(cdi = true, value = { DynEnumDbService.class, 
        DynEnumManager.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ DynEnum.class.getName(), EntityType.class.getName()});
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }
     */
        
    @Test
    public void dynEnumManagerTest() {

        final DynEnum first = EntityType.valueOf("invalidValueForTesting");
        Assertions.assertThrows(RuntimeException.class, () -> dynEnumMgr.valueOf(first));

        dynEnumMgr.allowEnumDiscovery();
        DynEnum saved = dynEnumMgr.valueOf(first);
        Assertions.assertTrue(saved.getId() != null, "DynEnum id is not null");
    }
}
