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

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.attachment.AttachmentFile;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsIEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.RestReply;
import de.ipb_halle.signals.storage.StorageService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Manager for signals materials libraries (materials/libraries API endpoint)
 */

@Stateless
public class MaterialsManager {

    @Inject
    private AttachmentDbService attachmentDbService;

    @Inject
    private LibraryDbService libraryDbService;

    @Inject
    private LibraryRestService libraryRestService;

    @Inject
    private MaterialDbService materialDbService;

    @Inject
    private MaterialRestService materialRestService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private StorageService storageService;

    private Logger logger = LoggerFactory.getLogger(MaterialsManager.class);

    public void manageLibraries(RuntimeConfig config) {
        fetchLibraries(config);
    }


    //=============RECEIVE MATERIALS AND FIELDS=============================================================
    public void manageMaterials(RuntimeConfig runtimeConfig, Date[] dateRange) {
        Map<String, Object> cmap = new HashMap<>();
        /**
         * criteria Map contains entityType, start and end date e.g.:
         * CMAP key: "includeTypes" and value: "[asset.EntityType]"
         * CMAP key: "start" and value: "2024-01-01T00:00:00.000+0100"
         * CMAP key: "end" and value: "2024-12-09T09:05:12.479+0100"
         */

        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }

        // ToDo: order of entities (assets, then batches) matters!
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES,
                new EntityType[]{EntityType.valueOf(Material.ENTITY_TYPE_ASSET),
                        EntityType.valueOf(Material.ENTITY_TYPE_BATCH)});

        Map<String, Map<String, Field>> allFields = mapFieldsByLibraryId();

        //List of all asset entities
        List<SignalsIEntityDTO> entityDTOs = signalsEntityDbService.load(cmap);
        int i = 1;
        for (SignalsIEntityDTO dto : entityDTOs) {
            if ((i % 1000) == 0) {
                this.logger.info("Processed material #{}", i);
            }
            fetchMaterial(dto, allFields);
        }
    }

    /**
     * This method creates mapping of fields by libraryId
     * @return all fields mapped by libraryId (outer map) and fieldId (inner map)
     */
    private Map<String, Map<String, Field>> mapFieldsByLibraryId() {
        /**
         * Information about field:
         * Field{id='6329671b759ae07953c8117a',
         * attributeListEid='null',
         * calculated=null,
         * defaultUnit='null',
         * definedBy='SYSTEM_DEFAULT',
         * definingEntityId='assetType:6329671b759ae07953c8117b',
         * hidden=true, key='null', multiSelect=null, readOnly=null, required=false,
         * title='Chemical Compounds Image', userDefined=null, fieldType=ATTACHED_FILE.FieldType(28) ,
         * measures=[], options=[], designation=asset.FieldDesignation}
         */
        List<Library> libraries = libraryDbService.load(new HashMap<String, Object>());
        Set<String> libraryIds = libraries.stream().map(Library::getId).collect(Collectors.toSet());
        List<Field> allFields = receiveAllFieldsOfAllLibraries(libraryIds);

        //creating a map with library id as a key and field title/ field Object hashMap as a value
        Map<String, Map<String, Field>> resultMap = new HashMap<>();
        for (Field field : allFields) {
            //removing prefix assetType-> definingEntityId='assetType:6329671b759ae07953c8117b',
            String libraryId = field.getDefiningEntityId().split(":")[1];
            //putting String libraryID as a key and field result hashMap with field title and field object as a value
            resultMap.putIfAbsent(libraryId, new HashMap<>());
            //putting field Object as a value in value hashMap
            resultMap.get(libraryId).put(field.getId(), field);
        }
        return resultMap;
    }

    /**
     * This method obtains a List of all Fields for a given set of libraries
     *
     * @param libraryIds a set of library Ids -> assetType:1234567890abcdef
     * @return list of Fields
     * how field looks like:
     * Field{  id='66e2c5c9c4f5b568f97b8e84',
     * attributeListEid='attribute:27',
     * calculated=false,
     * defaultUnit='null',
     * definedBy='USER_ADDED',
     * hidden=false,
     * key='null',
     * multiSelect=null,
     * readOnly=null,
     * required=true,
     * title='Materials Access',
     * userDefined=null,
     * fieldType=ATTRIBUTE.FieldType(25) ,
     * measures=[], options=[], designation=asset.FieldDesignation}
     * <p>
     * example fo criteria map:
     * key "definingEntityId" and value [assetType:6215104dab0ad27bf7942a53, assetType:6329671b759ae07953c8117b, assetType:6215104dab0ad27bf7942a45]
     */
    private List<Field> receiveAllFieldsOfAllLibraries(Collection<String> libraryIds) {
        Map<String, Object> cmap = new HashMap<>();
        // receive all fields from all libraries in one shot -> very efficient
        cmap.put(Field.DEFINING_ENTITY_ID, libraryIds.stream().map(id -> Library.LIBRARY_TYPE + ":" + id).collect(Collectors.toList()));
        return fieldDbService.load(cmap);
    }


    /**
     * Fetch a single Material (asset or batch) via REST and store it
     * in the database.
     * @param entityDTO the Signals entity, which should be processed
     * @param
     */
    private void fetchMaterial(SignalsIEntityDTO entityDTO, Map<String, Map<String, Field>> allFields) {
        try {
            Material mat = materialRestService.doGetMaterial(entityDTO.getId());
            Map<String, Field> fieldsByLibraryId = allFields.get(mat.getLibraryId());
            processMaterial(fieldsByLibraryId, mat);
            materialDbService.save(mat);
        } catch (IOException e) {
            logger.warn("fetchMaterial caught IOException for material {}", entityDTO.getId());
        }
    }



    /**
     * @param fieldsByLibraryId a map of field definitions keyed by their library ID
     * @param mat               the material to which the field values are associated
     * @throws IOException if an error occurs during the process, such as when retrieving attachments
     */
    private void processMaterial(Map<String, Field> fieldsByLibraryId, Material mat) throws IOException {

        /*
            example of fieldValue in fieldValues:
                entityId='null', fieldId='6215104dab0ad27bf7942a48', fieldTitle='Molecular Formula',
                value='"C<sub>10</sub>H<sub>20</sub>O<sub>2</sub>"', linkType=UNSPECIFIED', adHocField=null
        */
        List<FieldValue> fieldValues = materialRestService.doGetMaterialProperties(mat.getId(), fieldsByLibraryId);

        for (FieldValue value : fieldValues) {
            Field definition = fieldsByLibraryId.get(value.getFieldId());

            //ToDO NEVER OCCURS
            if (definition == null) {
                logger.info("Definition is null");
                definition = value.getAdHocField();
                fieldDbService.save(definition);
                fieldsByLibraryId.put(definition.getId(), definition);
            }

            value.setEntityId(mat.getId());
            mat.addFieldValue(value);
            switch (definition.getFieldType().getValue()) {
                case FieldType.ATTACHED_FILE:
                    obtainAttachment(mat, definition, value);
                    break;
                case FieldType.CHEMICAL_DRAWING:
                    obtainDrawing(mat, definition, value);
                    break;
                case FieldType.SEQUENCE_FILE:
                    obtainSequence(mat, definition, value);
            }
        }
    }


    private void obtainAttachment(Material mat, Field field, FieldValue value) throws IOException {
        String mimeType = materialRestService.parseAttachmentMimeType(value);
        RestReply tempPath = materialRestService.doGetMaterialAttachment(mat, field, mimeType);
        if (tempPath != null) {
            List<RestReply> replies = new ArrayList<>();
            replies.add(tempPath);
            logger.info("obtainAttachment");
            storeAttachment(mat, field, replies, value);
        }
    }

    private void obtainDrawing(Material mat, Field drawing, FieldValue value) throws IOException {
        List<RestReply> drawings = materialRestService.doGetMaterialDrawing(mat);
        logger.info("obtainDrawing");
        storeAttachment(mat, drawing, drawings, value);
    }

    private void obtainSequence(Material mat, Field sequence, FieldValue value) throws IOException {
        List<RestReply> sequences = materialRestService.doGetMaterialSequence(mat);
        logger.info("obtainSequence");
        storeAttachment(mat, sequence, sequences, value);
    }


    //=============STORE ATTACHMENTS=============================================================

    /**
     * Moves a collection of temporary attachment files into
     * permanent storage and create the appropriate database records.
     * Checks, whether the files actually are new revisions. If
     * files didn't change (as per their fileId), no new
     * AttachmentRevision is created.
     *
     * @param mat
     * @param field
     * @param files
     */
    @Transactional(rollbackOn = IOException.class)
    private void storeAttachment(Material mat, Field field, Collection<RestReply> files, FieldValue value) throws IOException {

        Attachment attachment = getAttachment(mat, field, value);
        AttachmentRevision latestRevision = attachment.getLatestRevision();
        AttachmentRevision newRevision = new AttachmentRevision();
        addRevisionData(newRevision, value);
        attachment.addRevision(newRevision);

        if ((latestRevision == null) || (!latestRevision.getFileId().equals(newRevision.getFileId()))) {
            for (RestReply reply : files) {
                AttachmentFile file = new AttachmentFile();
                file.setDigest(reply.getDigest());
                file.setSize(reply.getFileSize());
                file.setMimeType(reply.getMimeType());
                file.setTempPath(reply.getPath());
                attachment.addFile(file);
            }
            attachmentDbService.save(attachment);

            for (AttachmentFile file : attachment.getFiles(attachment.getLatestRevision().getId())) {
                storageService.storeFile(file);
            }
        }
    }

    private Attachment getAttachment(Material mat, Field field, FieldValue value) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Attachment.ANCESTOR_ID, mat.getId());
        cmap.put(Attachment.FIELD_ID, field.getId());
        cmap.put(Attachment.LATEST_ONLY, Boolean.TRUE);
        List<Attachment> attachments = attachmentDbService.load(cmap);
        switch (attachments.size()) {
            case 0:
                Attachment attachment = new Attachment();
                attachment.setAncestorId(mat.getId());
                attachment.setFieldId(field.getId());
                return attachment;
            case 1:
                return attachments.get(0);
            default:
                throw new RuntimeException("getLatestAttachmentRevision() returned more than 1 attachment");
        }
    }

    private void addRevisionData(AttachmentRevision rev, FieldValue value) {
        materialRestService.parseAttachmentRevisionInfo(rev, value);
    }

    /**
     * Obtains libraries from Signals via REST call and stores them in the
     * database. If the RuntimeConfig flag updateDb is false, the list of
     * libraries is obtained but not stored in the database ('dry run').
     *
     * @param config
     */
    private void fetchLibraries(RuntimeConfig config) {
        List<Library> libraries = libraryRestService.doGetLibraries();
        for (Library lib : libraries) {
            if (config.updateDb) {
                libraryDbService.save(lib);
            }
        }
    }
}


