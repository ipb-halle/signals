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
public class LocationProcessorBean {

    @Inject
    private LocationRestService locationRestService;

    @Inject
    private LocationDbService locationDbService;

    @Inject
    private AttachmentDbService attachmentDbService;

    @Inject
    private StorageService storageService;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public static final Logger logger = LogManager.getLogger(LocationProcessorBean.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleLocation(String locationId) {

        try {
            doProcessLocation(locationId);
        } catch (RuntimeException e) {
            logger.error("LocationProcessorBean:-> processLocation() caught an exception.", e);
        }
    }

    private void doProcessLocation(String locationId) throws RuntimeException {
        //If transaction marked for rollback, then break it
        if (transactionSynchronizationRegistry.getTransactionStatus() == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("LocationProcessorBean:-> Transaction is marked for rollback, skipping.");
            return;
        }

        // 1) Load location via REST
        Location location = locationRestService.doGetLocation(locationId);


        location.setLocationTypeId(LocationType.LOCATION_TYPE_ENTITY_PREFIX + location.getLocationTypeId() + LocationType.LOCATION_TYPE_ENTITY_SUFFIX);
        location.getFields().forEach(field -> field.setId(field.getId() + ":" + location.getLocationTypeId()));
        location.getFieldValues().forEach(fieldValue -> {
            fieldValue.setEntityId(locationId);
            fieldValue.setFieldId(fieldValue.getFieldId() + ":" + location.getLocationTypeId());
        });

        // 2) Process location fields
        try {
            processLocationFields(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        locationDbService.saveLocation(location);
    }

    private void processLocationFields(Location location) throws IOException {
        for (Field field : location.getFields()) {
            if (field.getFieldType().getValue().equals(FieldType.ATTACHMENT_FILE)) {
                for (FieldValue fieldValue : location.getFieldValues()) {
                    if (fieldValue.getFieldId().equals(field.getId())) {
                        obtainAttachment(location, field, fieldValue);
                    }
                }
            }
        }
    }

    /**
     * obtain an attachment for a given attachment field and compare,
     * whether this attachment is already known to the system.
     *
     * @param location
     * @param field
     * @param fieldValue
     * @throws IOException
     */
    private void obtainAttachment(Location location, Field field, FieldValue fieldValue) throws IOException {
        String mimeType = locationRestService.parseAttachmentMimeType(fieldValue);
        RestReply tempPath = locationRestService.doGetLocationAttachment(location, field, mimeType);
        if (tempPath != null) {
            Attachment attachment = getAttachment(location, field);

            if(attachment == null){
                logger.error("LocationProcessorBean:-> Attachment is null for location {}, field {}", location.getId(), field.getId());
                return;
            }

            if (isNewRevision(attachment, fieldValue, tempPath)) {
                locationDbService.saveLocation(location);
                storeAttachment(attachment, tempPath);
            } else {
                storageService.removeFromStaging(tempPath);
            }
        }
    }

    /**
     * Loads an attachment object (if exists) or creates new one
     */
    private Attachment getAttachment(Location location, Field field) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Attachment.ANCESTOR_ID, location.getId());
        cmap.put(Attachment.FIELD_ID, field.getId());
        cmap.put(Attachment.LATEST_ONLY, Boolean.TRUE);
        List<Attachment> attachments = attachmentDbService.load(cmap);

        switch (attachments.size()) {
            case 0:
                Attachment attachment = new Attachment();
                attachment.setAncestorId(location.getId());
                attachment.setFieldId(field.getId());
                return attachment;
            case 1:
                return attachments.get(0);
            default:
                logger.error("LocationProcessorBean:-> getAttachment() found more than 1 attachment for matId = {}, fieldId = {}", location.getId(), field.getId());
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
     boolean isNewRevision(Attachment attachment, FieldValue fieldValue, RestReply reply) {
        AttachmentRevision latestRevision = attachment.getLatestRevision();
        AttachmentRevision newRevision = new AttachmentRevision();
        locationRestService.parseAttachmentRevisionInfo(newRevision, fieldValue);
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
            logger.error("LocationProcessorBean:->Transaction marked for rollback, skipping attachment storage.");
            return;
        }

        AttachmentFile file = new AttachmentFile();
        file.setDigest(reply.getDigest());
        file.setSize(reply.getFileSize());
        file.setMimeType(reply.getMimeType());
        file.setTempPath(reply.getPath());
        attachment.addFile(file);
        logger.trace("LocationProcessorBean:-> Saving attachment: {}", attachment);

        attachmentDbService.save(attachment);

        logger.trace("LocationProcessorBean:-> Persisting files for revision: {}", attachment.getLatestRevision().getId());

        for (AttachmentFile stagedFile : attachment.getFiles(attachment.getLatestRevision().getId())) {
            storageService.storeFile(stagedFile);
        }
    }

    public void setLocationRestService(LocationRestService locationRestService) {
        this.locationRestService = locationRestService;
    }

    public void setLocationDbService(LocationDbService locationDbService) {
        this.locationDbService = locationDbService;
    }

    public void setAttachmentDbService(AttachmentDbService attachmentDbService) {
        this.attachmentDbService = attachmentDbService;
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }
}
