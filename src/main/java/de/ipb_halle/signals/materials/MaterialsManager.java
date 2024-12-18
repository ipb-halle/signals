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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.attachment.AttachmentFile;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.config.Feature;
import de.ipb_halle.signals.config.LocalConfig;
import de.ipb_halle.signals.config.LocalConfigDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.*;
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
    private LocalConfigDbService localConfigDbService;

    @Inject
    private MaterialDbService materialDbService;

    @Inject
    private MaterialRestService materialRestService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private StorageService storageService;

    private Logger logger = LoggerFactory.getLogger(MaterialsManager.class);

    public void manageLibraries(RuntimeConfig config) {
        fetchLibraries(config);
    }

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
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES,
                new EntityType[]{EntityType.valueOf(Material.ENTITY_TYPE_ASSET)});


        List<SignalsEntityDTO> assetsEntities = signalsEntityDbService.load(cmap);

        //loading of material lists to add field
        List<Material> materials = assetsEntities.stream().
                map(asset -> {
                    logger.info("Processing material {}", asset.getId());
                    return materialRestService.doGetMaterial(asset.getId());
                })
                .toList();

        processMaterials(materials);
    }

    /**
     * Process all materials. Specifically, obtain all the
     * field values including their proper field definitions and
     * all attachments including any chemical drawings,
     * images and sequences.
     * Material name, description and library type are stored
     * directly with the material, although they appear as field values
     * in some REST endpoints.
     * Finally store the material and all dependent entities and files.
     *
     * @param materials a list of materials
     */
    private void processMaterials(List<Material> materials) {
        //collects all library ids
        Set<String> libraryIds = materials.stream()
                .map(Material::getLibraryId)
                .collect(Collectors.toSet());


        //load all fields for library by library id, where field map has a key field title and field object
        Map<String, Map<String, Field>> fieldsByLibrary = mapFieldsByLibraryId(libraryIds);

        for (Material mat : materials) {
            //field from certain library (contains title, id and type) ; mat contains asset eid and library id as well as name of asset
            Map<String, Field> libraryFields = fieldsByLibrary.get(mat.getLibraryId());
            try {
                processMaterial(libraryFields, mat);
                materialDbService.save(mat);
            } catch (IOException e) {
                logger.warn("processMaterials caught IOException for material {}", mat.getId());
            }
        }
    }

    /**
     * Create a mapping of fields by libraryId
     *
     * @param libraryIds
     * @return
     */
    private Map<String, Map<String, Field>> mapFieldsByLibraryId(Set<String> libraryIds) {
        List<Field> allFields = getLibraryFields(libraryIds);

        //creating a map with library id as a key and field title/ field Object hashMap as a value
        Map<String, Map<String, Field>> resultMap = new HashMap<>();
        for (Field field : allFields) {
            String libraryId = field.getDefiningEntityId().split(":")[1];
            //putting String libraryID as a key and field result hashMap with field title and field object as a value
            resultMap.putIfAbsent(libraryId, new HashMap<>());
            //putting field Object as a value in value hashMap
            resultMap.get(libraryId).put(field.getId(), field);
        }
        return resultMap;
    }

    /**
     * Obtain a List of all Fields for a given set of libraries
     *
     * @param libraryIds a set of library Ids
     * @return list of Fields
     */
    private List<Field> getLibraryFields(Set<String> libraryIds) {
        Map<String, Object> cmap = new HashMap<>();
        // receive all fields from all libraries in one shot -> very efficient
        cmap.put(Field.DEFINING_ENTITY_ID, libraryIds.stream()
                .map(id -> Library.LIBRARY_TYPE + ":" + id)
                .collect(Collectors.toList())
        );

        return fieldDbService.load(cmap);
    }

    /**
     * Process the field definitions and attachments for a single material
     *
     * @param fieldsById a map of field definitions by fieldId for the
     *                   library of the requested material.
     * @param mat        the material
     */
    private void processMaterial(Map<String, Field> fieldsById, Material mat) throws IOException {
        List<FieldValue> fieldValues = materialRestService.doGetMaterialProperties(mat.getId(), fieldsById);

        for (FieldValue value : fieldValues) {
            Field definition = fieldsById.get(value.getFieldId());
            if (definition == null) {
                definition = value.getAdHocField();
                fieldDbService.save(definition);
                fieldsById.put(definition.getId(), definition);
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
                case FieldType.SEQUENCE:
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
            logger.info("obtainAttachment, field ->{}\n temp path: -> {}\n replies: -> {}\n", field.toString(), tempPath, Arrays.toString(replies.toArray()));
            storeAttachment(mat, field, replies, value);
        }
    }

    private void obtainDrawing(Material mat, Field drawing, FieldValue value) throws IOException {
        List<RestReply> drawings = materialRestService.doGetMaterialDrawing(mat);
        storeAttachment(mat, drawing, drawings, value);
    }

    private void obtainSequence(Material mat, Field sequence, FieldValue value) throws IOException {
        List<RestReply> sequences = materialRestService.doGetMaterialSequence(mat);
        storeAttachment(mat, sequence, sequences, value);
    }

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

        if ((latestRevision == null) || (! latestRevision.getFileId().equals(newRevision.getFileId()))) {
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


