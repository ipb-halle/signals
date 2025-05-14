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

import de.ipb_halle.signals.experiments.Experiment;
import de.ipb_halle.signals.experiments.ExperimentDbService;
import de.ipb_halle.signals.experiments.ExperimentProcessorBean;
import de.ipb_halle.signals.experiments.ExperimentRestService;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExperimentProcessorBeanTest {

    @InjectMocks
    private ExperimentProcessorBean experimentProcessorBean;

    @Mock
    private ExperimentRestService experimentRestService;

    @Mock
    private ExperimentDbService experimentDbService;

    @Mock
    private TransactionSynchronizationRegistry registry;

    @Test
    public void testProcessSingleExperiment_withTemplate() throws Exception {
        String experimentId = "exp:123";

        when(registry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);

        Experiment mockExperiment = new Experiment();
        mockExperiment.setTemplateId("template:001");

        when(experimentRestService.doGetExperiment(experimentId)).thenReturn(mockExperiment);

        experimentProcessorBean.processSingleExperiment(experimentId);

        verify(experimentRestService).doGetExperiment(experimentId);
        verify(experimentRestService).doGetExperimentProperties(mockExperiment);
        verify(experimentRestService).doGetExperimentPropertyValues(mockExperiment);
        verify(experimentDbService).save(mockExperiment);
    }

    @Test
    public void testProcessSingleExperiment_transactionMarkedForRollback() {
        when(registry.getTransactionStatus()).thenReturn(Status.STATUS_MARKED_ROLLBACK);

        experimentProcessorBean.processSingleExperiment("exp:rollback");

        verifyNoInteractions(experimentRestService);
        verifyNoInteractions(experimentDbService);
    }

    @Test
    public void testProcessSingleExperiment_withoutTemplate() throws Exception {
        String experimentId = "exp:456";

        when(registry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);

        Experiment mockExperiment = new Experiment();
        mockExperiment.setTemplateId(null);

        when(experimentRestService.doGetExperiment(experimentId)).thenReturn(mockExperiment);

        experimentProcessorBean.processSingleExperiment(experimentId);

        verify(experimentRestService).doGetExperiment(experimentId);
        verify(experimentRestService, never()).doGetExperimentProperties(mockExperiment);
        verify(experimentRestService).doGetExperimentPropertyValues(mockExperiment);
        verify(experimentDbService).save(mockExperiment);
    }
}
