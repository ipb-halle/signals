/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.List;

@ExtendWith(PostgresqlContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ContainerDbServiceTest {

    @Inject
    @DeploymentElement
    private ContainerDbService containerDbService;

    @Inject
    @DeploymentElement
    private FieldDbService fieldDbService;

    @BeforeEach
    public void setUp() {
        Container testConatiner = new Container();
        testConatiner.setId("container:sample123");
        testConatiner.setMaterial(new MaterialReference().setId("sample:abc123"));
        testConatiner.setContainerTypeId("type:sample");
        testConatiner.setCreatedAt(new Date());
        IUser user = new UserReference("testUser");
        testConatiner.setCreatedBy(user);
        testConatiner.setLocation(new LocationReference().setId("testLocation"));
        testConatiner.setUnit(Unit.getUnit("g"));
        testConatiner.setUpdatedBy(user);

        containerDbService.saveContainer(testConatiner);
    }

    @Test
    public void testLoadAllContainersWithMaterialIdSample() {
        List<ContainerEntity> results = containerDbService.loadAllContainersWithMaterialIdSample();

        Assertions.assertFalse(results.isEmpty(), "Expected to find at least one container with materialId like 'sample:%'");
        Assertions.assertTrue(results.get(0).getMaterialId().startsWith("sample:"), "MaterialId must start with 'sample:'");
    }
}
