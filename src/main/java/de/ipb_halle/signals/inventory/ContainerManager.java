/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.attachment.AttachmentFile;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.rest.RestReply;
import de.ipb_halle.signals.storage.StorageService;
import de.ipb_halle.signals.users.UserManager;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manager for containers. This class orchestrates fetching container
 * data from a remote system (via REST) and saving it locally in a
 * database. It also provides methods to augment container data with
 * relevant user and location information.
 */

@Stateless
public class ContainerManager {

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Inject
    private ContainerDbService containerDbService;

    @Inject
    private ContainerRestService containerRestService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private UserManager userManager;

    @Inject
    private LocationManager locationManager;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private ContainerTypeDbService containerTypeDbService;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private StorageService storageService;

    @Inject
    private AttachmentDbService attachmentDbService;


    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    /**
     * Fetches containers created/updated within a specified date range
     * from the remote system, and saves them locally. Filters out container
     * type definitions if they appear in the fetched data.
     *
     * <ol>
     *     <li>Loads container type IDs from the local database.</li>
     *     <li>Loads matching entities (of container type) from the Signals database.</li>
     *     <li>Processes and saves each container locally, skipping type definitions.</li>
     * </ol>
     *
     * @param dateRange an array of dates; the first element is the start date,
     *                  and the second element (if present) is the end date
     */
    public void manageContainers(Date[] dateRange) {
        // 1) Loads set of container type ids
        Set<String> containerTypeIds = containerTypeDbService.getContainerTypeIds();

        // 2) Load (and map) all container fields for attachmentFiles
        Map<String, Field> attachmentFields = loadAttachmentFieldsMap();

        // 3) Preparing criteria map for loading signals entities by date range and type
        EntityType[] entityTypes = {EntityType.valueOf(ContainerEntity.ENTITY_TYPE_CONTAINER)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        // 4) Loads all containers from db (checked its working)
        List<SignalsEntityDTO> containers = signalsEntityDbService.load(cmap);

        // 5) Processes container sequentially
        for (SignalsEntityDTO dto : containers) {
            // filter out type definitions, if signals DB put them together (checked its working)
            if (!containerTypeIds.contains(dto.getId())) {
                processContainer(dto.getId(), attachmentFields);
            }
        }
    }

    /**
     * create a map of all container fields, mapped by their Id
     *
     * @return map of Field by Id
     */
    private Map<String, Field> loadAttachmentFieldsMap() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Field.FIELD_DESIGNATION, dynEnumManager.valueOf(FieldDesignation.valueOf(FieldDesignation.CONTAINER)));
        cmap.put(Field.FIELD_TYPE, dynEnumManager.valueOf(FieldType.valueOf(FieldType.ATTACHMENT_FILE)));
        return fieldDbService.load(cmap)
                .stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));
    }


    /**
     * Fetches a container by its ID from the remote REST service and saves it
     * into the local database.
     *
     * @param id the ID of the container to fetch
     */
    private void processContainer(String id, Map<String, Field> attachmentFields) {
        try {
            Container container = containerRestService.doGetContainer(id);
            processContainerFields(container, attachmentFields);
            containerDbService.save(container);
        } catch (IOException e) {
            logger.error("processContainer() caught an exception.", (Throwable) e);
        }
    }

    private void processContainerFields(Container ct, Map<String, Field> attachmentFields) throws IOException {
        for (FieldValue value : ct.getFieldValues()) {
            Field field = attachmentFields.get(value.getFieldId());
            if (field != null) {
                obtainAttachment(ct, field, value);
            }
        }
    }

    /**
     * obtain an attachment for a given attachment field and compare,
     * whether this attachment is already known to the system.
     *
     * @param ct
     * @param field
     * @param fieldValue
     * @throws IOException
     */
    private void obtainAttachment(Container ct, Field field, FieldValue fieldValue) throws IOException {
        String mimeType = containerRestService.parseAttachmentMimeType(fieldValue);
        RestReply tempPath = containerRestService.doGetContainerAttachment(ct, field, mimeType);
        if (tempPath != null) {
            Attachment attachment = getAttachment(ct, field);
            if (isNewRevision(attachment, fieldValue, tempPath)) {
                storeAttachment(attachment, tempPath);
            } else {
                storageService.removeFromStaging(tempPath);
            }
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
        if (latestRevision == null) {
            attachment.addRevision(newRevision);
            return true;
        }
        for (AttachmentFile file : attachment.getFiles(latestRevision.getId())) {
            if (reply.getDigest().equals(file.getDigest())) {
                return false;
            }
        }
        return true;
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
    private void storeAttachment(Attachment attachment,
                                 RestReply reply) throws IOException {

        if (transactionSynchronizationRegistry.getTransactionStatus()
                == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("Transaction marked for rollback, skipping attachment storage.");
            return;
        }

        AttachmentFile file = new AttachmentFile();
        file.setDigest(reply.getDigest());
        file.setSize(reply.getFileSize());
        file.setMimeType(reply.getMimeType());
        file.setTempPath(reply.getPath());
        attachment.addFile(file);

        attachmentDbService.save(attachment);

        for (AttachmentFile stagedFile : attachment.getFiles(attachment.getLatestRevision().getId())) {
            storageService.storeFile(stagedFile);
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
                attachment.setAncestorId(Container.CONTAINER_TYPE_ENTITY_PREFIX
                        + container.getId()
                        + Container.CONTAINER_TYPE_ENTITY_SUFFIX);
                attachment.setFieldId(field.getId());
                return attachment;
            case 1:
                return attachments.get(0);
            default:
                logger.error("MPB:-> getAttachment() found more than 1 attachment for matId = {}, fieldId = {}", container.getId(), field.getId());
                return null;
        }
    }


    /**
     * Augments a {@link Container} with user and location data. Replaces
     * the simple references to {@code createdBy}, {@code updatedBy}, and
     * {@code location} with fully loaded objects from the local database.
     *
     * @param ct the container to augment
     */
    public void augmentContainer(Container ct) {
        ct.setCreatedBy(userManager.getUser(ct.getCreatedBy().getId()));
        ct.setUpdatedBy(userManager.getUser(ct.getUpdatedBy().getId()));
        ct.setLocation(locationManager.loadById(ct.getLocation().getId(), true));
    }

    /**
     * Loads a container by its ID from the local database.
     *
     * @param id the ID of the container
     * @return the loaded container, or {@code null} if not found
     */
    public Container loadById(String id) {
        return containerDbService.loadById(id);
    }

    /**
     * Fetches a container by its ID from the remote REST service (without
     * saving it locally).
     *
     * @param id the ID of the container
     * @return the container from the remote system
     */
    public Container getSnbContainer(String id) {
        return containerRestService.doGetContainer(id);
    }


    /**
     * Attempts to load a container by its ID from the local database.
     * If not found locally, it is fetched from the remote REST service.
     * If {@code augmented} is true, the container is augmented with user
     * location data.
     *
     * @param id        the ID of the container
     * @param augment whether the container should be augmented
     * @return the container
     */
    public Container getContainer(String id, boolean augment
    ) {
        Container ct = containerDbService.loadById(id);
        if (ct == null) {
            ct = containerRestService.doGetContainer(id);
        }
        if (augment) {
            augmentContainer(ct);
        }
        return ct;
    }

    /**
     * Saves the given container to the local database. The container is first
     * converted into a {@link ContainerEntity}, then persisted.
     *
     * @param c the container to save
     */
    public void save(Container c) {
        containerDbService.save(c);
    }
}
