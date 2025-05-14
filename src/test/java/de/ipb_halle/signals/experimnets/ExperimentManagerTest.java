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

package de.ipb_halle.signals.experimnets;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.experiments.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExperimentManagerTest {

    @InjectMocks
    private ExperimentManager experimentManager;

    @Mock
    private ExperimentDbService experimentDbService;

    @Mock
    private DynEnumManager dynEnumManager;

    @Mock
    private ExperimentRestService experimentRestService;

    @Mock
    private SignalsEntityDbService signalsEntityDbService;

    @Mock
    private ExperimentProcessorBean experimentProcessorBean;

    @Test
    public void testImportExperiment_shouldLoadAndCreate() {
        String experimentId = "exp:001";
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        EntityType mockEntityType = mock(EntityType.class);

        ExperimentEntity experimentEntity = new ExperimentEntity()
                .setId(experimentId)
                .setName("MyExperiment")
                .setType(2);

        when(experimentDbService.loadExperimentById(experimentId)).thenReturn(experimentEntity);
        when(dynEnumManager.valueOf(2)).thenReturn(mockEntityType);

        experimentManager.importExperiment(runtimeConfig, experimentId);

        verify(experimentDbService).loadExperimentById(experimentId);
        verify(experimentDbService).loadExperimentPropertyValuesWithProperties(any(Experiment.class));
        verify(experimentRestService).createNewExperiment(any(Experiment.class));
    }

    @Test
    public void testManageExperiments_shouldProcessEachExperiment() {
        Date[] dateRange = new Date[]{
                new Date(System.currentTimeMillis() - 100_000),
                new Date()
        };
        SignalsEntityDTO dto1 = new SignalsEntityDTO();
        dto1.setId("exp:001");
        SignalsEntityDTO dto2 = new SignalsEntityDTO();
        dto2.setId("exp:002");

        List<SignalsEntityDTO> dtos = List.of(dto1, dto2);

        when(signalsEntityDbService.loadSE(anyMap())).thenReturn(dtos);

        experimentManager.manageExperiments(dateRange);

        verify(signalsEntityDbService).loadSE(anyMap());
        verify(experimentProcessorBean).processSingleExperiment("exp:001");
        verify(experimentProcessorBean).processSingleExperiment("exp:002");
    }
}
