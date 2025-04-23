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

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.inventory.ContainerDbService;
import de.ipb_halle.signals.inventory.ContainerEntity;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static jakarta.transaction.Status.STATUS_ACTIVE;
import static org.mockito.Mockito.*;

public class SampleProcessorBeanTest {

    private SampleProcessorBean sampleProcessorBean;
    private SampleRestService sampleRestService;
    private SampleDbService sampleDbService;
    private ContainerDbService containerDbService;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @BeforeEach
    public void setUp() {
        sampleProcessorBean = new SampleProcessorBean();

        sampleRestService = mock(SampleRestService.class);
        sampleDbService = mock(SampleDbService.class);
        containerDbService = mock(ContainerDbService.class);
        transactionSynchronizationRegistry = mock(TransactionSynchronizationRegistry.class);

        injectField(sampleProcessorBean, "sampleRestService", sampleRestService);
        injectField(sampleProcessorBean, "sampleDbService", sampleDbService);
        injectField(sampleProcessorBean, "containerDbService", containerDbService);
        injectField(sampleProcessorBean, "transactionSynchronizationRegistry", transactionSynchronizationRegistry);
    }

    @Test
    public void testProcessSingleSample_shouldProcessAndSaveSample() throws Exception {
        //given
        String sampleId = "sample:123";
        Sample sample = new Sample();
        sample.setId(sampleId);

        ContainerEntity containerEntity = new ContainerEntity();
        containerEntity.setId("container:abc");
        containerEntity.setMaterialId(sampleId);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(STATUS_ACTIVE);
        when(sampleRestService.doGetSample(sampleId)).thenReturn(sample);
        when(containerDbService.loadAllContainersWithMaterialIdSample()).thenReturn(List.of(containerEntity));

        // when
        sampleProcessorBean.processSingleSample(sampleId);

        // then
        verify(sampleRestService).doGetSample(sampleId);
        verify(sampleRestService).doGetSampleProperties(sample);
        verify(sampleDbService).save(sample);
        assert sample.getParentContainerId().equals("container:abc");
    }

    @Test
    public void testProcessSingleSample_transactionMarkedForRollback_shouldSkipProcessing() throws Exception {
        // given
        String sampleId = "sample:456";

        // simulate rollback state
        when(transactionSynchronizationRegistry.getTransactionStatus())
                .thenReturn(jakarta.transaction.Status.STATUS_MARKED_ROLLBACK);

        // when
        sampleProcessorBean.processSingleSample(sampleId);

        // then
        // ни один из методов не должен быть вызван
        verify(sampleRestService, never()).doGetSample(anyString());
        verify(sampleDbService, never()).save(any());
        verify(sampleRestService, never()).doGetSampleProperties(any());
        verify(containerDbService, never()).loadAllContainersWithMaterialIdSample();
    }

    //Helper method for field injection
    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = SampleProcessorBean.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
}
