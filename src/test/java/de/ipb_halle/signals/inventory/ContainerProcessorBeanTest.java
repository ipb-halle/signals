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

import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.attachment.AttachmentFile;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.RestReply;
import de.ipb_halle.signals.storage.StorageService;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

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

    @Test
    public void processSingleContainer_withAttachmentField_shouldProcessAttachment() throws IOException {
        String containerId = "container:test";
        String conatinerTypeId = "123";
        String fullContainerTypeId = ContainerType.CONTAINER_TYPE_ENTITY_PREFIX + conatinerTypeId + ContainerType.CONTAINER_TYPE_ENTITY_SUFFIX;
        String fieldId = "field1";
        String fieldIdTransformed = fieldId + ":" + fullContainerTypeId;

        Container container = new Container();
        container.setId(containerId);
        container.setContainerTypeId(conatinerTypeId);

        Field field = new Field();
        field.setId(fieldId);
        field.setFieldType(FieldType.valueOf(FieldType.ATTACHMENT_FILE));

        FieldValue fieldValue = new FieldValue();
        fieldValue.setFieldId(fieldId);

        container.setFields(List.of(field));
        container.setFieldValues(List.of(fieldValue));

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(containerRestService.doGetContainer(containerId)).thenReturn(container);
        when(containerRestService.parseAttachmentMimeType(any())).thenReturn("application/pdf");

        String mimeType = "application/pdf";
        Path dummyPath = Path.of("/tmp/fake.pdf");
        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(containerRestService.doGetContainerAttachment(any(), any(), any())).thenReturn(reply);

        doAnswer(invocation -> {
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(containerRestService).parseAttachmentRevisionInfo(any(), any());

        Attachment existingAttachment = new Attachment();
        existingAttachment.setAncestorId(containerId);
        existingAttachment.setFieldId(fieldIdTransformed);
        existingAttachment.addRevisions(new HashSet<>());

        when(attachmentDbService.load(any())).thenReturn(List.of(existingAttachment));
        doNothing().when(attachmentDbService).save(any(Attachment.class));

        containerProcessorBean.processSingleContainer(containerId);

        verify(containerRestService).doGetContainer(containerId);
        verify(containerDbService, times(2)).saveContainer(container);
        verify(containerRestService).doGetContainerAttachment(any(), any(), any());
        verify(attachmentDbService).save(any(Attachment.class));
        verify(storageService).storeFile(any(AttachmentFile.class));

    }
}
