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
import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@LocalBean
public class ContainerProcessorBean {

    @Inject
    ContainerRestService containerRestService;

    @Inject
    ContainerDbService containerDbService;

    @Inject
    AttachmentDbService attachmentDbService;

    @Inject
    StorageService storageService;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public static final Logger logger = LogManager.getLogger(ContainerProcessorBean.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleContainer(String containerId) {
        try {
            //The main processing of container takes places in this method
            doProcessContainer(containerId);
        } catch (IOException e) {
            logger.error("ContainerProcessorBean:-> processContainer() caught an exception.", e);
        }
    }

    private void doProcessContainer(String containerId) throws IOException {

        //If transaction marked for rollback, then break it
        if (transactionSynchronizationRegistry.getTransactionStatus() == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("ContainerProcessorBean:-> Transaction is marked for rollback, skipping.");
        }

        // 1) Load container via REST
        Container container = containerRestService.doGetContainer(containerId);


        container.setContainerTypeId(ContainerType.CONTAINER_TYPE_ENTITY_PREFIX + container.getContainerTypeId() + ContainerType.CONTAINER_TYPE_ENTITY_SUFFIX);
        container.getFields().forEach(field -> field.setId(field.getId() + ":" + container.getContainerTypeId()));
        container.getFieldValues().forEach(fieldValue -> {
            fieldValue.setEntityId(containerId);
            fieldValue.setFieldId(fieldValue.getFieldId() + ":" + container.getContainerTypeId());
        });


        // 2) Process container fields
        processContainerFields(container);
        logger.info(container.getId());
        containerDbService.saveContainer(container);
    }

    private void processContainerFields(Container container) throws IOException {
        for (Field field : container.getFields()) {
            if (field.getType().getValue().equals(FieldType.ATTACHMENT_FILE)) {
                for (FieldValue fieldValue : container.getFieldValues()) {
                    if (fieldValue.getFieldId().equals(field.getId())) {
                        obtainAttachment(container, field, fieldValue);
                    }
                }
            }
        }
    }

    /**
     * obtain an attachment for a given attachment field and compare,
     * whether this attachment is already known to the system.
     *
     * @param container
     * @param field
     * @param fieldValue
     * @throws IOException
     */
    private void obtainAttachment(Container container, Field field, FieldValue fieldValue) throws IOException {
        String mimeType = containerRestService.parseAttachmentMimeType(fieldValue);
        RestReply tempPath = containerRestService.doGetContainerAttachment(container, field, mimeType);
        if (tempPath != null) {
            Attachment attachment = getAttachment(container, field);
            if (isNewRevision(attachment, fieldValue, tempPath)) {
                containerDbService.saveContainer(container);
                storeAttachment(attachment, tempPath);
            } else {
                storageService.removeFromStaging(tempPath);
            }
        }
    }

    /**
     * Loads an attachment object (if exists) or creates new one
     */
    private Attachment getAttachment(Container container, Field field) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Attachment.ANCESTOR_ID, container.getId());
        cmap.put(Attachment.FIELD_ID, field.getId());
        cmap.put(Attachment.LATEST_ONLY, Boolean.TRUE);
        List<Attachment> attachments = attachmentDbService.load(cmap);

        switch (attachments.size()) {
            case 0:
                Attachment attachment = new Attachment();
                attachment.setAncestorId(container.getId());
                attachment.setFieldId(field.getId());
                return attachment;
            case 1:
                return attachments.get(0);
            default:
                logger.error("ContainerProcessorBean:-> getAttachment() found more than 1 attachment for matId = {}, fieldId = {}", container.getId(), field.getId());
                return null;
        }
    }

    /**
     * check whether downloaded file is a new revision and needs to be moved
     * to permanent storage.
     *
     * @param attachment
     * @param fieldValue
     * @param reply
     * @return
     */
    private boolean isNewRevision(Attachment attachment, FieldValue fieldValue, RestReply reply) {
        AttachmentRevision latestRevision = attachment.getLatestRevision();
        AttachmentRevision newRevision = new AttachmentRevision();
        containerRestService.parseAttachmentRevisionInfo(newRevision, fieldValue);
        //newRevision.setFileId(attachment.getAncestorId().split(":")[1] + ":" + attachment.getFieldId().split(":")[0]);
        newRevision.setFileId(reply.getDigest());

        if ((latestRevision == null) ||
                !latestRevision.getFileId().equals(newRevision.getFileId())) {
            attachment.addRevision(newRevision);
            return true;
        }
        for (AttachmentFile file : attachment.getFiles(latestRevision.getId())) {
            if (reply.getDigest().equals(file.getDigest())) {
                return false;
            }
        }
        return false;
    }

    /**
     * persist the downloaded attachments in the database and move
     * them from their staging location into permanent storage. Make
     * sure, not to store and move if the transaction has been aborted.
     *
     * @param attachment with a new revision already added
     * @param reply
     * @throws IOException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void storeAttachment(Attachment attachment, RestReply reply) throws IOException {

        if (transactionSynchronizationRegistry.getTransactionStatus()
                == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("ContainerProcessorBean:->Transaction marked for rollback, skipping attachment storage.");
            return;
        }

        AttachmentFile file = new AttachmentFile();
        file.setDigest(reply.getDigest());
        file.setSize(reply.getFileSize());
        file.setMimeType(reply.getMimeType());
        file.setTempPath(reply.getPath());
        attachment.addFile(file);
        logger.trace("ContainerProcessorBean:-> Saving attachment: {}", attachment);


        attachmentDbService.save(attachment);

        logger.trace("ContainerProcessorBean:-> Persisting files for revision: {}", attachment.getLatestRevision().getId());

        for (AttachmentFile stagedFile : attachment.getFiles(attachment.getLatestRevision().getId())) {
            storageService.storeFile(stagedFile);
        }
    }
}
