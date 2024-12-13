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
import de.ipb_halle.signals.config.Feature;
import de.ipb_halle.signals.config.LocalConfig;
import de.ipb_halle.signals.config.LocalConfigDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.rest.RestClient;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Manager for signals materials libraries (materials/libraries API endpoint)
 */

@Stateless
public class MaterialsManager {

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
                    Material material = materialRestService.doGetMaterial(asset.getId());
                    return material;
                })
                .toList();

        processMaterials(materials);
    }

    /**
     * Process all materials. Specifically, 
     * * provide all the field definitions for the fields obtained from 
     *   the materials REST endpoint. The endpoint provides only field
     *   title and field value. It specifically does not provide any
     *   field Id. 
     * * Furthermore, the REST endpoint provides values for
     *   fields, which have no definition. Known examples are the fields
     *   "title", "description" and ___. 
     * * Additionally, this method downloads all attachments and any 
     *   chemical drawings, images and sequences.
     * * Finally store the material and all dependent entities and files.
     * @param materials a list of materials
     */
    private void processMaterials(List<Material> materials) {
        //collects all library ids
        Set<String> libraryIds = materials.stream()
                .map(Material::getLibraryId)
                .collect(Collectors.toSet());

        Set<String> drawingLibraryIds = getLibrariesWith(Feature.HAS_DRAWING);
        Set<String> imageLibraryIds = getLibrariesWith(Feature.HAS_IMAGE);
        Set<String> sequenceLibraryIds = getLibrariesWith(Feature.HAS_SEQUENCE);

        //load all fields for library by library id, where field map has a key field title and field object
        Map<String, Map<String, Field>> fieldsByLibrary = mapFieldsByLibraryId(libraryIds);

        for (Material mat : materials) {
            Map<String, Field> libraryFields = fieldsByLibrary.get(mat.getLibraryId());
            processMaterial(libraryFields, mat);

            if (imageLibraryIds.contains(mat.getLibraryId())) {
                obtainImage(mat, libraryFields);
            }
            if (drawingLibraryIds.contains(mat.getLibraryId())) {
                obtainDrawing(mat, libraryFields);
            }
            if (sequenceLibraryIds.contains(mat.getLibraryId())) {
                obtainSequence(mat, libraryFields);
            }
            materialDbService.save(mat);
        }
    }

    /**
     * Provide a set of library Ids, which match certain criteria, e.g.
     * all Ids of libraries, which may contain chemical drawings, images or
     * sequences.
     * @param feature the requested feature (see criteria map keys in Library)
     * @return A set of library Ids
     */
    private Set<String> getLibrariesWith(Feature feature) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(LocalConfig.CRITERIA_FEATURE, feature);
        return localConfigDbService.load(cmap)
                .stream()
                .map(cfg -> cfg.getEntityId())
                .collect(Collectors.toSet());
    }

    /**
     * Create a mapping of fields by libraryId
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
            resultMap.get(libraryId).put(field.getTitle(), field);
        }
        return resultMap;
    }

    /**
     * Obtain a List of all Fields for a given set of libraries
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

        List<Field> allFields = fieldDbService.load(cmap);
        return allFields;
    }

    /**
     * Process the field definitions and attachments for a single material
     * @param fieldDefinitions a map of field definitions by field title for the
     * library of the requested material.
     * @param mat the material
     */
    private void processMaterial(Map<String, Field> fieldDefinitions, Material mat) {
        FieldType attachment = FieldType.valueOf(FieldType.ATTACHED_FILE);
        assignSimpleFieldValues(fieldDefinitions, mat);
        fieldDefinitions.values().stream()
                .filter(f -> {
                    return f.equals(attachment);
                })
                .forEach(f -> obtainAttachment(f, mat));
    }

    /**
     * Assign all field definitions for simple field values (e.g. text, numbers,
     * etc. but not attachments, images, etc.)
     * @param libraryFields map of field definitions (by title)
     * @param mat the material
     */
    private void assignSimpleFieldValues(Map<String, Field> libraryFields, Material mat) {
        Iterator<FieldValue> iterator = mat.getFieldValues().iterator();
        while (iterator.hasNext()) {
            FieldValue fieldValue = iterator.next();

            // check in fieldCache
            final Field field = obtainAssetField(libraryFields,
                    mat.getLibraryId(),
                    fieldValue.getFieldTitle(),
                    FieldType.valueOf(FieldType.TEXT));
            fieldValue.setFieldId(field.getId());
        }
    }

    /**
     * Get a asset field from the field by title map. Create a new field if necessary.
     * @param libraryFields the map of fields by field title
     * @param libraryId the library id
     * @param title the title of the field
     * @param type the type of the field
     * @return a persisted field
     */
    private Field obtainAssetField(Map<String, Field> libraryFields, String libraryId, String title, FieldType type) {
        Field field = libraryFields.get(title);
        if (field == null) {
            field = createNewAssetField(libraryId, title, type);
            libraryFields.put(title, field);
        }
        return field;
    }
    /**
     * Provide a field definition for ASSET ad-hoc fields (e.g. name, description, etc.)
     * @param libraryId the Id of the library
     * @param title the title of the field
     * @param type the type of the field (TEXT or ATTACHED_FILE)
     * @return a persisted Field
     */
    private Field createNewAssetField(String libraryId, String title, FieldType type) {
        // generate a new field
        Field newField = new Field();
        newField.setId(UUID.randomUUID().toString());
        newField.setTitle(title);
        newField.setUserDefined(true);
        newField.setFieldType(type);
        newField.setDesignation(FieldDesignation.valueOf(FieldDesignation.ASSET));
        newField.setDefiningEntityId("assetType:" + libraryId);

        // save in db and cache
        fieldDbService.save(newField);
        return newField;
    }

    /**
     * Provide a field definition for BATCH ad-hoc fields (e.g. name, description, etc.)
     * @param libraryId the Id of the library
     * @param title the title of the field
     * @return a persisted Field
     */
    private Field createNewBatchField(String libraryId, String title) {
        // generate a new field
        Field newField = new Field();
        newField.setId(UUID.randomUUID().toString());
        newField.setTitle(title);
        newField.setUserDefined(true);
        newField.setFieldType(FieldType.valueOf(FieldType.TEXT));
        newField.setDesignation(FieldDesignation.valueOf(FieldDesignation.BATCH));
        newField.setDefiningEntityId("assetType:" + libraryId);

        // save in db and cache
        fieldDbService.save(newField);
        return newField;
    }

    private void obtainAttachment(Field field, Material mat) {
        Path tempPath = materialRestService.doGetMaterialAttachment(mat, field);
        // ToDo: obtain single attachment for given Field
    }

    private void obtainImage(Material mat, Map<String, Field> libraryFields) {
        final Field image = obtainAssetField(libraryFields,
                mat.getLibraryId(),
                Field.FIELD_ID_IMAGE,
                FieldType.valueOf(FieldType.ATTACHED_FILE));
        Path tempPath = materialRestService.doGetMaterialImage(mat);
        storeAttachment(image, tempPath, RestClient.IMAGE_UNKNOWN);
    }

    private void obtainDrawing(Material mat, Map<String, Field> libraryFields) {
        final Field drawing = obtainAssetField(libraryFields,
                mat.getLibraryId(),
                Field.FIELD_ID_CHEMICAL_DRAWING,
                FieldType.valueOf(FieldType.ATTACHED_FILE));
        Map<String, Path> drawings = materialRestService.doGetMaterialDrawing(mat);
        drawings.forEach((key, value) -> storeAttachment(drawing, value, key));
    }

    private void obtainSequence(Material mat, Map<String, Field> libraryFields) {
        final Field sequence = obtainAssetField(libraryFields,
                mat.getLibraryId(),
                Field.FIELD_ID_SEQUENCE,
                FieldType.valueOf(FieldType.ATTACHED_FILE));
        Map<String, Path> sequences = materialRestService.doGetMaterialSequence(mat);
        sequences.forEach((key, value) -> storeAttachment(sequence, value, key));
    }

    /**
     * Move the temporary attachment file into permanent storage and create the
     * appropriate database records.
     * @param field
     * @param p
     * @param mimeType
     */
    private void storeAttachment(Field field, Path p, String mimeType) {
        logger.info("Request to store path {} of type {} in field {}", p.toString(), mimeType, field.getId());
        throw new RuntimeException("NOT IMPLEMENTED.");
    }

    /**
     * Obtain libraries from Signals via REST call and store them in the
     * database. If the RuntimeConfig flag updateDb is false, the list of
     * libraries is obtained but not stored in the database ('dry run').
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


