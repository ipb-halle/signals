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
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private Logger logger = LoggerFactory.getLogger(MaterialsManager.class);

    public void manageLibraries(RuntimeConfig config) {
        fetchLibraries(config);
    }

    public void manageMaterials(RuntimeConfig config, Date[] dateRange) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES,
                new EntityType[]{EntityType.valueOf(Material.ENTITY_TYPE_ASSET)});
        List<SignalsEntityDTO> entityDTOs = signalsEntityDbService.load(cmap);

        //loading of material lists to add filed
        List<Material> materials = entityDTOs.stream().
                map(dto -> {
                    logger.info("Processing material {}", dto.getId());
                    Material material = materialRestService.doGetMaterial(dto.getId());
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

        //load all fields for library after library id, where field map has a key field title and field object
        Map<String, Map<String, Field>> fieldsByLibrary = loadFieldsByLibraries(libraryIds);

        for (Material mat : materials) {
            updateFieldValues(fieldsByLibrary.get(mat.getLibraryId()), mat);
        }
    }

    private Map<String, Map<String, Field>> loadFieldsByLibraries(Set<String> libraryIds) {
        Map<String, Object> cmap = new HashMap<>();
        // receive all fields from all libraries in one shot -> very efficient
        cmap.put(Field.DEFINING_ENTITY_ID, libraryIds.stream()
                .map(id -> Library.LIBRARY_TYPE + ":" + id)
                .collect(Collectors.toList())
        );

        List<Field> allFields = fieldDbService.load(cmap);

        //creates a map through groupingBy
        Map<String, Map<String, Field>> resultMap = new HashMap<>();
        for (Field field : allFields) {
            String libraryId = field.getDefiningEntityId().split(":")[1];
            resultMap.putIfAbsent(libraryId, new HashMap<>());
            resultMap.get(libraryId).put(field.getTitle(), field);
        }
        return resultMap;
    }

    private void updateFieldValues(Map<String, Field> fieldDefinitions, Material mat) {
        Iterator<FieldValue> iterator = mat.getFieldValues().iterator();
        while (iterator.hasNext()) {
            FieldValue fieldValue = iterator.next();

            // check in fieldCache
            Field field = fieldDefinitions.get(fieldValue.getFieldTitle());
            if (field != null) {
                fieldValue.setFieldId(field.getId());
            } else {
                // generate a new field
                Field newField = new Field();
                newField.setId(UUID.randomUUID().toString());
                newField.setTitle(fieldValue.getFieldTitle());
                newField.setUserDefined(true);
                newField.setFieldType(FieldType.valueOf("TEXT"));
                newField.setDesignation(FieldDesignation.valueOf(FieldDesignation.ASSET));
                newField.setDefiningEntityId("assetType:" + mat.getLibraryId());


                // save in db and cache
                fieldDbService.save(newField);
                fieldDefinitions.put(fieldValue.getFieldTitle(), newField);

                //set Feld-ID in to FieldValue
                fieldValue.setFieldId(newField.getId());
                fieldValue.setValue("EMPTY");
            }
        }
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


