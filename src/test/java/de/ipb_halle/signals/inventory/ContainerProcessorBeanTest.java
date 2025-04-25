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

import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.storage.StorageService;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.ipb_halle.signals.field.Field;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class ContainerProcessorBeanTest {

    private ContainerProcessorBean containerProcessorBean;

    private ContainerRestService containerRestService;
    private ContainerDbService containerDbService;
    private AttachmentDbService attachmentDbService;
    private StorageService storageService;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @BeforeEach
    public void setUp() {
        containerProcessorBean = new ContainerProcessorBean();

        containerRestService = mock(ContainerRestService.class);
        containerDbService = mock(ContainerDbService.class);
        attachmentDbService = mock(AttachmentDbService.class);
        storageService = mock(StorageService.class);
        transactionSynchronizationRegistry = mock(TransactionSynchronizationRegistry.class);

        containerProcessorBean.setContainerRestService(containerRestService);
        containerProcessorBean.setContainerDbService(containerDbService);
        containerProcessorBean.setAttachmentDbService(attachmentDbService);
        containerProcessorBean.setStorageService(storageService);
        containerProcessorBean.setTransactionSynchronizationRegistry(transactionSynchronizationRegistry);

    }

    @Test
    public void processSingleContainerTest() {
        Container container = new Container();
        container.setId("container:test");
        container.setContainerTypeId("123");

        container.setFields(List.of());
        container.setFieldValues(List.of());

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(jakarta.transaction.Status.STATUS_ACTIVE);
        when(containerRestService.doGetContainer(container.getId())).thenReturn(container);

        containerProcessorBean.processSingleContainer(container.getId());

        verify(containerRestService).doGetContainer(container.getId());
        verify(containerDbService).saveContainer(container);
    }

    @Test
    void testProcessSingleContainer_transactionMarkedForRollback_shouldLogAndSkip() {
        String containerId = "container:test";

        when(transactionSynchronizationRegistry.getTransactionStatus())
                .thenReturn(jakarta.transaction.Status.STATUS_MARKED_ROLLBACK);

        containerProcessorBean.processSingleContainer(containerId);

        verifyNoInteractions(containerRestService);
        verifyNoInteractions(containerDbService);
    }

    @Test
    void testProcessSingleContainer_shouldCatchIOException() {
        String containerId = "test-container-id";

        when(containerRestService.doGetContainer(containerId))
                .thenThrow(new RuntimeException("Test exception"));

        containerProcessorBean.processSingleContainer(containerId);

        verify(containerDbService, never()).saveContainer(any());

    }
}
