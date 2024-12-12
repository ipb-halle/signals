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


        //cmap.forEach((key, value) -> logger.info("This is HMAP key: '{}' and value: '{}'", key, value));


        /**
         * here we receive the entities asset with relatively ids
         */
        List<SignalsEntityDTO> assetsEntities = signalsEntityDbService.load(cmap);

        //entityDTOs.forEach((entity) -> logger.info("This is an element of list of entitiesDTO: '{}'\n", entity.dump()));

        //loading of material lists to add field
        List<Material> materials = assetsEntities.stream().
                map(asset -> {
                    logger.info("Processing material {}", asset.getId());
                    Material material = materialRestService.doGetMaterial(asset.getId());
                    return material;
                })
                .toList();

        augmentFieldDefinitions(materials);

        materials.forEach(materialDbService::save);
    }


    //extending of field definitions
    private void augmentFieldDefinitions(List<Material> materials) {
        //collects all library ids
        Set<String> libraryIds = materials.stream()
                .map(Material::getLibraryId)
                .collect(Collectors.toSet());
        //hold all fields from library ids
        List<Field> allFields = getFieldsFromLibrariesIds(libraryIds);
        Field imageField = fieldDbService.getImageField();
        Field drawingField = fieldDbService.getDrawingField();
        Field sequenceField = fieldDbService.getSequenceField();
        Set<String> imageLibraryIds = getLibrariesWithImages();
        Set<String> drawingLibraryIds = getLibrariesWithDrawings();
        Set<String> sequenceLibraryIds = getLibrariesWithSequences();

        //load all fields for library by library id, where field map has a key field title and field object
        Map<String, Map<String, Field>> fieldsByLibrary = generateResultMapWithLibIdAndFieldMap(libraryIds, allFields);

        for (Material mat : materials) {
            updateFieldValues(fieldsByLibrary.get(mat.getLibraryId()), mat);
            if (imageLibraryIds.contains(mat.getLibraryId())) {
                obtainImage(mat, imageField);
            }
            if (drawingLibraryIds.contains(mat.getLibraryId())) {
                obtainDrawing(mat);
            }
            if (sequenceLibraryIds.contains(mat.getLibraryId())) {
                obtainSequence(mat);
            }
        }
    }

    private void getFieldsAttachments(List<Material> materials, List<Field> allFields) {

        List<Field> fieldsWithTypeAttachedFile = getFieldsWithTypeAttachedFile(allFields);

        //map with key material id and value list of field values which contains id of fields with type attached files
        Map<String, List<FieldValue>> materialsMapWithFieldValuesAttachedFile = getMaterialFieldValuesWithAttachedFile(materials, fieldsWithTypeAttachedFile);

        for (String materialId : materialsMapWithFieldValuesAttachedFile.keySet()) {
            for (FieldValue value : materialsMapWithFieldValuesAttachedFile.get(materialId)) {
                logger.info("This is field value with id {}\n", value.getFieldId());
            }
        }

        //materialRestService.doGetFieldAttachments(materials, )
    }

    private Map<String, List<FieldValue>> getMaterialFieldValuesWithAttachedFile(List<Material> materials, List<Field> fieldsWithTypeAttachedFile) {
        Map<String, List<FieldValue>> fieldValuesAndMaterialIds = new HashMap<>();
        for (Material material : materials) {
            List<FieldValue> fieldValues = new ArrayList<>();
            for (FieldValue fieldValue : material.getFieldValues()) {
                for (Field field : fieldsWithTypeAttachedFile) {
                    logger.info("Material id {} field id {}", material.getId(), field.getId());
                    /*
                    String result = materialRestService.doGetFieldAttachments(material, field.getId());
                    if (result != null) {
                        logger.warn("THIS IS REST CALL RESULT {}", result);
                        fieldValues.add(fieldValue.setFieldId(field.getId()));
                    }

                     */
                }
            }
            logger.info(Arrays.toString(fieldValues.toArray()));
            fieldValuesAndMaterialIds.put(material.getId(), fieldValues);
        }
        return fieldValuesAndMaterialIds;
    }

    private List<Field> getFieldsWithTypeAttachedFile(List<Field> allFields) {
        List<Field> fieldsWithTypeAttachedFile = new ArrayList<>();
        for (Field field : allFields) {
            if (field.getFieldType().equals(FieldType.valueOf(FieldType.ATTACHED_FILE))) {
                String fieldId = field.getId();
                fieldsWithTypeAttachedFile.add(field);
                //logger.info("field IDS {}\n and field types {}\n field title {} \n and field ancestor {}\n", fieldId, field.getFieldType(), field.getTitle(), field.getDefiningEntityId());
            }
        }
        return fieldsWithTypeAttachedFile;
    }

    private Map<String, Map<String, Field>> generateResultMapWithLibIdAndFieldMap(Set<String> libraryIds, List<Field> allFields) {
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

    private List<Field> getFieldsFromLibrariesIds(Set<String> libraryIds) {
        Map<String, Object> cmap = new HashMap<>();
        // receive all fields from all libraries in one shot -> very efficient
        cmap.put(Field.DEFINING_ENTITY_ID, libraryIds.stream()
                .map(id -> Library.LIBRARY_TYPE + ":" + id)
                .collect(Collectors.toList())
        );

        List<Field> allFields = fieldDbService.load(cmap);
        return allFields;
    }

    private void updateFieldValues(Map<String, Field> fieldDefinitions, Material mat) {
        FieldType attachment = FieldType.valueOf(FieldType.ATTACHED_FILE);
        assignSimpleFieldValues(fieldDefinitions, mat);
        fieldDefinitions.values().stream()
                .filter(f -> {
                    return f.equals(attachment);
                })
                .forEach(f -> obtainAttachment(f, mat));

        // obtainImage(mat);
    }

    private void assignSimpleFieldValues(Map<String, Field> fieldDefinitions, Material mat) {
        Iterator<FieldValue> iterator = mat.getFieldValues().iterator();
        while (iterator.hasNext()) {
            FieldValue fieldValue = iterator.next();

            // check in fieldCache
            Field field = fieldDefinitions.get(fieldValue.getFieldTitle());
            if (field != null) {
                fieldValue.setFieldId(field.getId());
            } else {
                field = createNewAssetField(mat.getLibraryId(), fieldValue.getFieldTitle());
                fieldDefinitions.put(field.getTitle(), field);

                //set Feld-ID in to FieldValue
                fieldValue.setFieldId(field.getId());
            }
        }
    }

    private Field createNewAssetField(String libraryId, String title) {
        // generate a new field
        Field newField = new Field();
        newField.setId(UUID.randomUUID().toString());
        newField.setTitle(title);
        newField.setUserDefined(true);
        newField.setFieldType(FieldType.valueOf("TEXT"));
        newField.setDesignation(FieldDesignation.valueOf(FieldDesignation.ASSET));
        newField.setDefiningEntityId("assetType:" + libraryId);

        // save in db and cache
        fieldDbService.save(newField);
        return newField;
    }

    private void obtainAttachment(Field field, Material mat) {
        Path tempPath = materialRestService.doGetMaterialAttachment(mat, field);
        // ToDo: obtain single attachment for given Field
    }

    private void obtainImage(Material mat) {
        Path tempPath = materialRestService.doGetMaterialImage(mat);
        storeAttachment(tempPath, RestClient.IMAGE_UNKNOWN);
    }

    private void obtainDrawing(Material mat) {
        Map<String, Path> drawings = materialRestService.doGetMaterialDrawing(mat);
        drawings.forEach((key, value) -> storeAttachment(value, key));
    }

    private void obtainSequence(Material mat) {
        Map<String, Path> sequences = materialRestService.doGetMaterialSequence(mat);
        sequences.forEach((key, value) -> storeAttachment(value, key));
    }


    private void storeAttachment(Path p, String mimeType) {
        logger.info("Request to store path {} of type {}", p.toString(), mimeType);
        throw new RuntimeException("NOT IMPLEMENTED.");
    }

    private void fetchLibraries(RuntimeConfig config) {
        List<Library> libraries = libraryRestService.doGetLibraries();
        for (Library lib : libraries) {
            if (config.updateDb) {
                libraryDbService.save(lib);
            }
        }
    }
}


