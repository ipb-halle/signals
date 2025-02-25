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

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.FieldDesignation;
import de.ipb_halle.signals.field.FieldType;
import de.ipb_halle.signals.users.UserManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Manager for signals locations (inventory/location API endpoint)
 */

@Stateless
public class LocationManager {

    @Inject
    private LocationTypeDbService locationTypeDbService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private LocationDbService locationDbService;

    @Inject
    private LocationRestService locationRestService;

    @Inject
    private UserManager userManager;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private FieldDbService fieldDbService;

    private Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    public void manageLocations(Date[] dateRange) {
        // 1) Loads a set of location type ids (checked -> location types are present)
        Set<String> locationTypeIds = locationTypeDbService.getLocationTypIds();

        // 2) Load (and map) all location fields for attachmentFiles
        Map<String, Field> attachmentFields = loadAttachmentFieldsMap();

        // 3) Preparing criteria map for loading signals entities by date range and type
        EntityType entityTypes[] = {EntityType.valueOf(LocationEntity.ENTITY_TYPE_LOCATION)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        // 4) Loading all locations from db (checked everything is working)
        List<SignalsEntityDTO> locations = signalsEntityDbService.load(cmap);

        // 5) Process locations (checked, everything is working)
        for (SignalsEntityDTO dto : locations) {
            if (!locationTypeIds.contains(dto.getId())) {
                processLocation(dto.getId(), attachmentFields);
            }
        }
    }

    private Map<String, Field> loadAttachmentFieldsMap() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Field.FIELD_DESIGNATION, dynEnumManager.valueOf(FieldDesignation.valueOf(FieldDesignation.LOCATION)));
        cmap.put(Field.FIELD_TYPE, dynEnumManager.valueOf(FieldType.valueOf(FieldType.ATTACHMENT_FILE)));
        return fieldDbService.load(cmap)
                .stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));
    }


    /**
     * Fetches a location by its ID from the remote REST service and saves it
     * into the local database.
     *
     * @param id the ID of location to fetch
     */
    public void processLocation(String id, Map<String, Field> attachmentFields) {
        try {
            Location location = locationRestService.doGetLocation(id);
        } catch (Exception e) {

        }
    }

    public void save(LocationEntity loc) {
        locationDbService.save(loc);
    }

    public void save(Location loc) {
        locationDbService.save(loc);
    }

    public LocationEntity loadById(String id, boolean augment) {
        return locationDbService.loadById(id);
    }
}
