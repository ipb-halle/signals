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

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContainerManagerUnitTest {

    @InjectMocks
    private ContainerManager manager;

    @Mock
    private ContainerDbService containerDbService;

    @Mock
    private ContainerTypeDbService containerTypeDbService;

    @Mock
    private ContainerRestService containerRestService;

    @Mock
    private SignalsEntityDbService signalsEntityDbService;

    @Mock
    private ContainerProcessorBean containerProcessorBean;

    @Test
    public void importContainer_shouldCallRestServiceIfUpdateSNBTrue() {
        RuntimeConfig config = new RuntimeConfig();
        config.updateSNB = true;

        ContainerEntity containerEntity = new ContainerEntity();
        containerEntity.setId("cont:123");
        containerEntity.setContainerTypeId("ct:789");
        containerEntity.setFieldValues(new HashSet<>());
        containerEntity.setUnit("g");

        ContainerType containerType = new ContainerType();
        containerType.setId("ct:789");

        when(containerDbService.loadContainerById("cont:123")).thenReturn(containerEntity);
        when(containerTypeDbService.loadById("ct:789")).thenReturn(containerType);

        manager.importContainer(config, "cont:123");

        verify(containerRestService).doCreateContainer(eq(containerType), any(Container.class));
    }

    @Test
    public void manageContainers_shouldProcessOnlyNonTypeContainers() {
        Date start = new Date(System.currentTimeMillis() - 100000);
        Date end = new Date();
        Date[] range = new Date[]{start, end};

        Set<String> containerTypeIds = Set.of("ct:typeOnly");
        when(containerTypeDbService.getContainerTypeIds()).thenReturn(containerTypeIds);

        SignalsEntityDTO container1 = new SignalsEntityDTO();
        container1.setId("cont:real1");
        container1.setType(EntityType.valueOf(ContainerEntity.ENTITY_TYPE_CONTAINER));

        SignalsEntityDTO typeDef = new SignalsEntityDTO();
        typeDef.setId("ct:typeOnly");
        typeDef.setType(EntityType.valueOf(ContainerEntity.ENTITY_TYPE_CONTAINER));

        List<SignalsEntityDTO> dtos = List.of(container1, typeDef);
        when(signalsEntityDbService.loadSE(anyMap())).thenReturn(dtos);

        manager.manageContainers(range);

        verify(containerProcessorBean).processSingleContainer("cont:real1");
        verify(containerProcessorBean, never()).processSingleContainer("ct:typeOnly");
    }
}
