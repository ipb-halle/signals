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

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class ContainerProcessorBeanTest {

    private ContainerProcessorBean containerProcessorBean;

    private ContainerRestService containerRestService;
    private ContainerDbService containerDbService;
    private AttachmentDbService attachmentDbService;
    private StorageService storageService;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private Container container;
    private Field field;
    private FieldValue fieldValue;
    private final String containerId = "container:test";
    private final String containerTypeId = "123";
    private final String fieldId = "field1";
    private final String mimeType = "application/pdf";
    private final Path dummyPath = Path.of("/tmp/fake.pdf");

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
        prepareContainerWithAttachment();

        String fullContainerTypeId = ContainerType.CONTAINER_TYPE_ENTITY_PREFIX + containerTypeId + ContainerType.CONTAINER_TYPE_ENTITY_SUFFIX;
        String fieldIdTransformed = fieldId + ":" + fullContainerTypeId;


        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(containerRestService.doGetContainer(containerId)).thenReturn(container);
        when(containerRestService.parseAttachmentMimeType(any())).thenReturn(mimeType);
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
        doNothing().when(attachmentDbService).save(any());
        doNothing().when(containerDbService).saveContainer(any());

        containerProcessorBean.processSingleContainer(containerId);

        verify(containerRestService).doGetContainer(containerId);
        verify(containerDbService, times(2)).saveContainer(container);
        verify(containerRestService).doGetContainerAttachment(any(), any(), any());
        verify(attachmentDbService).save(any());
        verify(storageService).storeFile(any());

    }

    @Test
    public void processSingleContainer_noExistingAttachment_shouldCreateNew() throws IOException {
        prepareContainerWithAttachment();

        String fullContainerTypeId = ContainerType.CONTAINER_TYPE_ENTITY_PREFIX + containerTypeId + ContainerType.CONTAINER_TYPE_ENTITY_SUFFIX;
        String fieldIdTransformed = fieldId + ":" + fullContainerTypeId;

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(containerRestService.doGetContainer(containerId)).thenReturn(container);
        when(containerRestService.parseAttachmentMimeType(any())).thenReturn(mimeType);
        when(containerRestService.doGetContainerAttachment(any(), any(), any())).thenReturn(reply);

        doAnswer(invocation -> {
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(containerRestService).parseAttachmentRevisionInfo(any(), any());

        when(attachmentDbService.load(any())).thenReturn(List.of());

        doNothing().when(attachmentDbService).save(any());
        doNothing().when(containerDbService).saveContainer(any());

        containerProcessorBean.processSingleContainer(containerId);

        verify(containerRestService).doGetContainer(containerId);
        verify(containerDbService, times(2)).saveContainer(container);
        verify(containerRestService).doGetContainerAttachment(any(),any(),any());
        verify(attachmentDbService).save(any());
        verify(storageService).storeFile(any());
    }

    @Test
    public  void processSingleContainer_multipleAttachment_shouldLogErrorAndSkipAttachment() throws IOException {
        prepareContainerWithAttachment();

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(containerRestService.doGetContainer(containerId)).thenReturn(container);
        when(containerRestService.parseAttachmentMimeType(any())).thenReturn(mimeType);
        when(containerRestService.doGetContainerAttachment(any(),any(),any())).thenReturn(reply);

        doAnswer(invocation->{
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;

        }).when(containerRestService).parseAttachmentRevisionInfo(any(),any());

        Attachment attachment1 = new Attachment();
        Attachment attachment2 = new Attachment();
        when(attachmentDbService.load(any())).thenReturn(List.of(attachment1,attachment2));

        doNothing().when(containerDbService).saveContainer(any());

        containerProcessorBean.processSingleContainer(containerId);

        verify(containerRestService).doGetContainer(containerId);
        verify(containerDbService, times(1)).saveContainer(container);
        verify(containerRestService).doGetContainerAttachment(any(), any(), any());

        verify(attachmentDbService, never()).save(any());
        verify(storageService, never()).storeFile(any());

    }

    @Test
    public void isNewRevision_shouldReturnFalse_whenDigestMatchesExitingFile(){
        prepareContainerWithAttachment();

        Attachment attachment = new Attachment();
        attachment.setAncestorId(containerId);
        attachment.setFieldId(fieldId);

        AttachmentRevision latestRevision = new AttachmentRevision();
        latestRevision.setId(1111);
        latestRevision.setFileId("digest123");
        attachment.addRevision(latestRevision);

        AttachmentFile existingFile = new AttachmentFile();
        existingFile.setDigest("digest123");
        existingFile.setRevisionId(latestRevision.getId());
        attachment.addFile(existingFile);

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        doAnswer(invocation->{
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(containerRestService).parseAttachmentRevisionInfo(any(),any());

        boolean result = containerProcessorBean.isNewRevision(attachment, fieldValue, reply);

        assertFalse(result, "Should return false because digest matches existing file");
    }

    private void prepareContainerWithAttachment() {
        container = new Container();
        container.setId(containerId);
        container.setContainerTypeId(containerTypeId);

        field = new Field();
        field.setId(fieldId);
        field.setFieldType(FieldType.valueOf(FieldType.ATTACHMENT_FILE));

        fieldValue = new FieldValue();
        fieldValue.setFieldId(fieldId);

        container.setFields(List.of(field));
        container.setFieldValues(List.of(fieldValue));
    }



}
