/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Manager for signals materials libraries (materials/libraries API endpoint).
 * <p>
 * Orchestrates the loading of "materials" (e.g., assets, batches) from the Signals database,
 * invokes the {@link MaterialProcessorBean} for processing each material, and handles library
 * data retrieval (both local and remote).
 */
@Stateless
public class MaterialsManager {

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private LibraryDbService libraryDbService;

    @Inject
    private LibraryRestService libraryRestService;

    @Inject
    private MaterialDbService materialDbService;

    @Inject
    private MaterialProcessorBean materialProcessorBean;

    @Inject
    private MaterialRestService materialRestService;

    private final Logger logger = LoggerFactory.getLogger(MaterialsManager.class);


    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void importMaterial(RuntimeConfig runtimeConfig, String id) {
        Material mat = materialDbService.loadById(id);
        Material batch = null;
        // also need to load batch if material supports batches!
        Library lib = libraryDbService.loadById(mat.getLibraryId());
        if (lib.hasBatches()) {
            Map<String, Object> cmap = new HashMap<>();
            cmap.put(MaterialDbService.MATERIAL_ID, mat.getId());
            List<Material> batches = materialDbService.load(cmap);
            if (batches.size() > 0) {
                batch = batches.get(0);
            } else {
                throw new RuntimeException("Could not obtain required batch for material " + mat.getId());
            }
        }
        if (runtimeConfig.updateSNB) {
            materialRestService.doCreateMaterial(lib, mat, batch);
        }
    }

    /**
     * Fetches materials from the Signals database within a specified date range
     * and processes them sequentially, mapping all fields from relevant libraries.
     * <p>
     * This method:
     * <ul>
     *     <li>Builds query parameters for date-based retrieval.</li>
     *     <li>Retrieves and maps fields from all known libraries.</li>
     *     <li>Loads the materials from the local database via {@link SignalsEntityDbService}.</li>
     *     <li>Processes each material in sequence using
     *     {@link #processMaterialsSequentially(List, Map)}.</li>
     * </ul>
     *
     * @param dateRange     an array with one or two date elements:
     *                      <ul>
     *                          <li><b>[0]</b> start date (required)</li>
     *                          <li><b>[1]</b> optional end date</li>
     *                      </ul>
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void manageMaterials( Date[] dateRange) {
        logger.debug("MM:-> START MANAGE MATERIALS");

        // 1) Query parameters for load
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES,
                new EntityType[]{EntityType.valueOf(Material.ENTITY_TYPE_ASSET),
                        EntityType.valueOf(Material.ENTITY_TYPE_BATCH)});

        // 2) Load all materials from the database
        List<SignalsEntityDTO> entityDTOs = signalsEntityDbService.load(cmap);

        // 3) Fetch all fields of all libraries
        Map<String, Map<String, Field>> allFields = mapFieldsByLibraryId();

        // 4) Parallel processing
        processMaterialsSequentially(entityDTOs, allFields);
    }


    /**
     * Builds a mapping of library IDs to the fields belonging to each library.
     * <p>
     * The resulting structure is:
     * <code>Map&lt;libraryId, Map&lt;fieldId, Field&gt;&gt;</code>.
     *
     * @return a nested map from library ID -> field ID -> {@link Field}
     */
    private Map<String, Map<String, Field>> mapFieldsByLibraryId() {
        // 1) Loads all libraries
        List<Library> libraries = libraryDbService.load(new HashMap<>());
        logger.info("Materials manager:-> loading of material libraries done");
        Set<String> libraryIds = libraries.stream().map(Library::getId).collect(Collectors.toSet());
        logger.info("Materials manager:-> library Ids {}\n", Arrays.toString(libraryIds.toArray()));

        // 2) Load all fields for the retrieved libraries (ids without prefix)
        List<Field> allFields = receiveAllFieldsOfAllLibraries(libraryIds);

        // 3) Build the nested map
        Map<String, Map<String, Field>> fieldMap = new HashMap<>();
        for (Field field : allFields) {
            //removing prefix assetType-> definingEntityId='assetType:6329671b759ae07953c8117b',
            //String libraryId = field.getDefiningEntityId().split(":")[1];
            String libraryId = field.getDefiningEntityId();
            fieldMap.putIfAbsent(libraryId, new HashMap<>());
            fieldMap.get(libraryId).put(field.getId(), field);
        }
        return fieldMap;
    }

    /**
     * Loads all fields for each specified library ID from the database.
     * <p>
     * Since libraries in the database store their fields under a <code>definingEntityId</code>
     * in the form <code>libraryType:libraryId</code>, this method first maps library IDs to
     * the expected search format, then invokes the {@link FieldDbService}.
     *
     * @param libraryIds a collection of library IDs to load fields for
     * @return a list of all fields matching the library IDs
     */
    private List<Field> receiveAllFieldsOfAllLibraries(Collection<String> libraryIds) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Field.DEFINING_ENTITY_ID_LIST, libraryIds.stream()
                .map(id -> Library.LIBRARY_TYPE + ":" + id)
                .collect(Collectors.toList()));
        List<Field> load = fieldDbService.load(cmap);
        System.out.println(Arrays.toString(load.toArray()));
        return load;
    }

    /**
     * Processes the given list of material DTOs in a sequential manner, delegating
     * each to {@link MaterialProcessorBean#processSingleMaterial(SignalsEntityDTO, Map)}.
     * <p>
     * Logs progress for every 500 materials processed.
     *
     * @param materials a list of {@link SignalsEntityDTO} representing materials
     * @param allFields a nested map of library ID -> field ID -> {@link Field}, used
     *                  for contextualizing field data in each material
     */
    private void processMaterialsSequentially(List<SignalsEntityDTO> materials,
                                              Map<String, Map<String, Field>> allFields) {
        int count = 0;
        for (SignalsEntityDTO dto : materials) {
            materialProcessorBean.processSingleMaterial(dto, allFields);
            count++;
            if (count % 500 == 0) {
                System.out.printf("%d materials are proceeded.\n", count);
            }
        }
    }

    /**
     * Main entry point for managing libraries: it calls {@link #fetchLibraries(RuntimeConfig)}
     * to retrieve the library list and optionally updates the local database.
     *
     * @param config runtime configuration determining whether the fetched libraries
     *               are persisted to the database
     */
    public void manageLibraries(RuntimeConfig config) {
        fetchLibraries(config);
    }

    /**
     * Fetches a list of libraries from the Signals system via REST and, if
     * {@code config.updateDb} is {@code true}, saves them to the local database.
     * If {@code config.updateDb} is {@code false}, the method performs a dry run
     * (libraries are fetched but not persisted).
     *
     * @param config a {@link RuntimeConfig} object indicating whether the fetched
     *               libraries should be stored in the database
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


