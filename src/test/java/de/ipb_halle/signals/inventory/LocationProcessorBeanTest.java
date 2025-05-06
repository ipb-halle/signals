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
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.RestReply;
import de.ipb_halle.signals.storage.StorageService;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import de.ipb_halle.signals.field.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class LocationProcessorBeanTest {

    private LocationProcessorBean locationProcessorBean;

    private LocationRestService locationRestService;
    private LocationDbService locationDbService;
    private AttachmentDbService attachmentDbService;
    private StorageService storageService;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private Location location;
    private Field field;
    private FieldValue fieldValue;
    private final String locationId = "location:test";
    private final String locationTypeId = "456";
    private final String fieldId = "field1";
    private final String mimeType = "application/pdf";
    private final Path dummyPath = Path.of("/tmp/fake.pdf");

    @BeforeEach
    public void setUp() {
        locationProcessorBean = new LocationProcessorBean();

        locationRestService = mock(LocationRestService.class);
        locationDbService = mock(LocationDbService.class);
        attachmentDbService = mock(AttachmentDbService.class);
        storageService = mock(StorageService.class);
        transactionSynchronizationRegistry = mock(TransactionSynchronizationRegistry.class);

        locationProcessorBean.setLocationRestService(locationRestService);
        locationProcessorBean.setLocationDbService(locationDbService);
        locationProcessorBean.setAttachmentDbService(attachmentDbService);
        locationProcessorBean.setStorageService(storageService);
        locationProcessorBean.setTransactionSynchronizationRegistry(transactionSynchronizationRegistry);
    }

    @Test
    public void processSingleLocationTest() {
        prepareLocationWithAttachment();

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(locationRestService.doGetLocation(locationId)).thenReturn(location);

        locationProcessorBean.processSingleLocation(locationId);

        verify(locationRestService).doGetLocation(locationId);
        verify(locationDbService).saveLocation(location);

    }

    @Test
    public void testProcessSingleLocation_transactionMarkedForRollback_shouldLogAndSkip() {
        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_MARKED_ROLLBACK);

        locationProcessorBean.processSingleLocation(locationId);

        verifyNoInteractions(locationRestService);
        verifyNoInteractions(locationDbService);
    }

    @Test
    public void testProcessSingleLocation_shouldCatchIOException() {
        when(locationRestService.doGetLocation(locationId)).thenThrow(new RuntimeException("Test exception"));

        locationProcessorBean.processSingleLocation(locationId);

        verify(locationDbService, never()).saveLocation(any());
    }

    @Test
    public void processSingleLocation_withAttachmentField_shouldProcessAttachment() throws IOException {
        prepareLocationWithAttachment();

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(locationRestService.doGetLocation(locationId)).thenReturn(location);
        when(locationRestService.parseAttachmentMimeType(any())).thenReturn(mimeType);
        when(locationRestService.doGetLocationAttachment(any(), any(), any())).thenReturn(reply);

        doAnswer(invocation -> {
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(locationRestService).parseAttachmentRevisionInfo(any(), any());

        Attachment existingAttachment = new Attachment();
        existingAttachment.setAncestorId(locationId);
        existingAttachment.setFieldId(fieldId + ":" + LocationType.LOCATION_TYPE_ENTITY_PREFIX + locationTypeId + LocationType.LOCATION_TYPE_ENTITY_SUFFIX);
        existingAttachment.addRevisions( new HashSet<>());

        when(attachmentDbService.load(any())).thenReturn(List.of(existingAttachment));

        locationProcessorBean.processSingleLocation(locationId);

        verify(locationRestService).doGetLocation(locationId);
        verify(locationDbService, times(2)).saveLocation(location);
        verify(locationRestService).doGetLocationAttachment(any(),any(),any());
        verify(attachmentDbService).save(any());
        verify(storageService).storeFile(any());
    }

    @Test
    public void processSingleLocation_noExistingAttachment_shouldCreateNew() throws IOException {
        prepareLocationWithAttachment();

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(locationRestService.doGetLocation(locationId)).thenReturn(location);
        when(locationRestService.parseAttachmentMimeType(any())).thenReturn(mimeType);
        when(locationRestService.doGetLocationAttachment(any(),any(),any())).thenReturn(reply);

        doAnswer(invocation -> {
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(locationRestService).parseAttachmentRevisionInfo(any(), any());

        when(attachmentDbService.load(any())).thenReturn(List.of());

        locationProcessorBean.processSingleLocation(locationId);

        verify(locationRestService).doGetLocation(locationId);
        verify(locationDbService, times(2)).saveLocation(location);
        verify(locationRestService).doGetLocationAttachment(any(), any(), any());
        verify(attachmentDbService).save(any());
        verify(storageService).storeFile(any());

    }

    @Test
    public void processSingleLocation_multipleAttachments_shouldLogErrorAndSkipAttachment() throws IOException {
        prepareLocationWithAttachment();

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        when(transactionSynchronizationRegistry.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(locationRestService.doGetLocation(locationId)).thenReturn(location);
        when(locationRestService.parseAttachmentMimeType(any())).thenReturn(mimeType);
        when(locationRestService.doGetLocationAttachment(any(), any(), any())).thenReturn(reply);

        doAnswer(invocation -> {
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(locationRestService).parseAttachmentRevisionInfo(any(), any());

        Attachment attachment1 = new Attachment();
        Attachment attachment2 = new Attachment();
        when(attachmentDbService.load(any())).thenReturn(List.of(attachment1, attachment2));

        locationProcessorBean.processSingleLocation(locationId);

        verify(locationRestService).doGetLocation(locationId);
        verify(locationDbService, times(1)).saveLocation(location);
        verify(locationRestService).doGetLocationAttachment(any(), any(), any());

        verify(attachmentDbService, never()).save(any());
        verify(storageService, never()).storeFile(any());
    }

    @Test
    public void isNewRevision_shouldReturnFalse_whenDigestMatchesExistingFile() {
        prepareLocationWithAttachment();

        Attachment attachment = new Attachment();
        attachment.setAncestorId(locationId);
        attachment.setFieldId(fieldId);

        AttachmentRevision latestRevision = new AttachmentRevision();
        latestRevision.setId(1111);
        latestRevision.setFileId("digest123");
        attachment.addRevision(latestRevision);

        AttachmentFile existingFile = new AttachmentFile();
        existingFile.setDigest("digest123");
        existingFile.setRevisionId(latestRevision.getId()); // важно!
        attachment.addFile(existingFile);

        RestReply reply = new RestReply(dummyPath, "digest123", mimeType);
        reply.setFileSize(100L);

        doAnswer(invocation -> {
            AttachmentRevision revision = invocation.getArgument(0);
            revision.setFileId("digest123");
            return null;
        }).when(locationRestService).parseAttachmentRevisionInfo(any(), any());

        boolean result = locationProcessorBean.isNewRevision(attachment, fieldValue, reply);

        assertFalse(result, "Should return false because digest matches existing file");
    }

    private void prepareLocationWithAttachment() {
        location = new Location();
        location.setId(locationId);
        location.setLocationTypeId(locationTypeId);

        field = new Field();
        field.setId(fieldId);
        field.setFieldType(FieldType.valueOf(FieldType.ATTACHMENT_FILE));

        fieldValue = new FieldValue();
        fieldValue.setFieldId(fieldId);

        location.addFields(List.of(field));
        location.addFieldValues(List.of(fieldValue));
    }
}
