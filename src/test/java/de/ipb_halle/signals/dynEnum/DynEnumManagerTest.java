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

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.users.UserManagerTest;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(PostgresqlContainerExtension.class)
public abstract class DynEnumManagerTest {

    @Inject
    @DeploymentElement
    public DynEnumManager dynEnumMgr;

    @Test
    public void dynEnumManagerTest() {

        final DynEnum first = EntityType.valueOf("invalidValueForTesting");
        Assertions.assertThrows(RuntimeException.class, () -> dynEnumMgr.valueOf(first));

        dynEnumMgr.allowEnumDiscovery();
        DynEnum saved = dynEnumMgr.valueOf(first);
        Assertions.assertTrue(saved.getId() != null, "DynEnum id is not null");
    }
}
