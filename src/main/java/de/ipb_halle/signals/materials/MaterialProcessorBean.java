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

package de.ipb_halle.signals.materials;


import de.ipb_halle.signals.attachment.*;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

@Stateless
@LocalBean
public class MaterialProcessorBean {

    @Inject
    private MaterialDbService materialDbService;

    @Inject
    private MaterialRestService materialRestService;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private StorageService storageService;

    @Inject
    private AttachmentDbService attachmentDbService;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private final static Logger logger = LoggerFactory.getLogger(MaterialProcessorBean.class);


    /**
     * This method will be called once per Material in order to process it in one transaction
     *
     * @param signalsEntityDTO DTO of materials (contains e.g. the id of material)
     * @param allFields        Mapping all fields, grouped by Library-ID
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleMaterial(SignalsEntityDTO signalsEntityDTO,
                                      Map<String, Map<String, Field>> allFields) {
        try {
            //The main processing of material takes places in this method
            doProcessMaterial(signalsEntityDTO, allFields);

        } catch (Exception e) {
            //Transaction will be automatically rolled back if exception occurs
            logger.error("MPB:-> Error in ProcessSingleMaterial, material {}: {}",
                    signalsEntityDTO.getId(), e.getMessage(), e);
        }
    }

    private void doProcessMaterial(SignalsEntityDTO signalsEntityDTO, Map<String, Map<String, Field>> allFields) throws IOException {

        //If transaction marked for rollback, then break it
        if (transactionSynchronizationRegistry.getTransactionStatus()
                == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("MPB:-> Transaction is marked for rollback, skipping.");
        }

        //1) Load material vie REST
        Material material = materialRestService.doGetMaterial(signalsEntityDTO.getId());

        //2) Process fields
        Map<String, Field> fieldsByLibraryId = allFields.get(material.getLibraryId());
        if (fieldsByLibraryId == null) {
            logger.error("MPB:-> No fields found for libraryId {}, skipping material {}",
                    material.getLibraryId(), signalsEntityDTO.getId());
            return;
        }

        processMaterialFields(fieldsByLibraryId, material);

        //3) Save materials in database
        materialDbService.save(material);
    }

    private void processMaterialFields(Map<String, Field> fieldLibrariesById, Material material) throws IOException {
        List<FieldValue> fieldValues = materialRestService.doGetMaterialProperties(material.getId(), fieldLibrariesById);

        for (FieldValue fieldValue : fieldValues) {
            Field field = fieldLibrariesById.get(fieldValue.getFieldId());
            if (field == null) {
                //if ad-hoc resp. new field
                logger.trace("MPB:-> Definition of field is null => saving new field");
                field = fieldValue.getField();
                fieldDbService.save(field);
                fieldLibrariesById.put(field.getId(), field);
            }
            fieldValue.setEntityId(material.getId());
            if (field.getFieldType().getValue().equals(FieldType.ATTACHED_FILE)
                    || field.getFieldType().getValue().equals(FieldType.CHEMICAL_DRAWING)
                    || field.getFieldType().getValue().equals(FieldType.SEQUENCE_FILE)) {
                processAttachments(material, field, fieldValue);
            }
            material.addFieldValue(fieldValue);
        }
    }

    /**
     * Process all attachments of a single material. Avoid downloading
     * and storing of unchanged attachments
     *
     * @param material
     * @param field
     * @param fieldValue
     */
    private void processAttachments(Material material, Field field, FieldValue fieldValue) throws IOException {
        Attachment attachment = getAttachment(material, field);
        AttachmentRevision latestRevision = attachment.getLatestRevision();
        AttachmentRevision newRevision = new AttachmentRevision();
        materialRestService.parseAttachmentRevisionInfo(newRevision, fieldValue);

        if ((latestRevision == null)
                || !latestRevision.getFileId().equals(newRevision.getFileId())) {
            logger.info("MaterialProcessorBean:-> processAttachments-> Found new attachment for material Id={}", material.getId());
            List<RestReply> replies = new ArrayList<>();

            switch (field.getFieldType().getValue()) {
                case FieldType.ATTACHED_FILE:
                    replies = obtainAttachment(material, field, fieldValue);
                    break;
                case FieldType.CHEMICAL_DRAWING:
                    replies = obtainDrawing(material);
                    break;
                case FieldType.SEQUENCE_FILE:
                    replies = obtainSequence(material);
            }

            attachment.addRevision(newRevision);
            if (!replies.isEmpty()) {
                storeAttachment(attachment, replies);
            }
        }
    }

    private List<RestReply> obtainAttachment(Material material, Field field, FieldValue fieldValue) throws IOException {
        String mimeType = materialRestService.parseAttachmentMimeType(fieldValue);
        RestReply tempPath = materialRestService.doGetMaterialAttachment(material, field, mimeType);
        return tempPath != null ? List.of(tempPath) : new ArrayList<RestReply>();
    }

    private List<RestReply> obtainDrawing(Material material) throws IOException {
        return materialRestService.doGetMaterialDrawing(material);
    }

    private List<RestReply> obtainSequence(Material material) throws IOException {
        return materialRestService.doGetMaterialSequence(material);
    }


    /**
     * persist the downloaded attachments in the database and move
     * them from their staging location into permanent storage. Make
     * sure, not to store and move if the transaction has been aborted.
     *
     * @param attachment the attachment with a new revision already added
     * @param files
     * @throws IOException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void storeAttachment(Attachment attachment,
                                 Collection<RestReply> files
    ) throws IOException {

        if (transactionSynchronizationRegistry.getTransactionStatus()
                == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.warn("MPB:-> Transaction marked for rollback, skipping attachment storage.");
            return;
        }

        for (RestReply reply : files) {
            AttachmentFile file = new AttachmentFile();
            file.setDigest(reply.getDigest());
            file.setSize(reply.getFileSize());
            file.setMimeType(reply.getMimeType());
            file.setTempPath(reply.getPath());
            attachment.addFile(file);
        }
        logger.trace("MPB:-> Saving attachment: {}", attachment);

        attachmentDbService.save(attachment);

        logger.trace("MPB:-> Persisting files for revision: {}",
                attachment.getLatestRevision().getId());
        for (AttachmentFile file : attachment.getFiles(attachment.getLatestRevision().getId())) {
            storageService.storeFile(file);
        }
    }

    /**
     * Loads an attachment object (if exists) or creates new one
     */
    private Attachment getAttachment(Material material, Field field) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Attachment.ANCESTOR_ID, material.getId());
        cmap.put(Attachment.FIELD_ID, field.getId());
        cmap.put(Attachment.LATEST_ONLY, Boolean.TRUE);
        List<Attachment> attachments = attachmentDbService.load(cmap);

        switch (attachments.size()) {
            case 0:
                Attachment attachment = new Attachment();
                attachment.setAncestorId(material.getId());
                attachment.setFieldId(field.getId());
                return attachment;
            case 1:
                return attachments.get(0);
            default:
                logger.error("MPB:-> getAttachment() found more than 1 attachment for matId = {}, fieldId = {}", material.getId(), field.getId());
                try {
                    throw new RuntimeException();
                } catch (Exception e) {
                    logger.warn("Exception", e);
                }
                return null;
        }
    }
}
