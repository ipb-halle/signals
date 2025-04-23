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

import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.field.FieldValueEntity;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


/**
 * Database service for locations
 */

@Stateless
@PersistenceElements(entities = {LocationEntity.class})
public class LocationDbService {

    public static final Logger logger = LogManager.getLogger(ContainerDbService.class);

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private FieldDbService fieldDbService;

    public LocationEntity loadLocationById(String id) {
        LocationEntity locationEntity = this.em.find(LocationEntity.class, id);
        loadFieldValues(locationEntity, id);
        return locationEntity;
    }

    private void loadFieldValues(LocationEntity locationEntity, String id) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(FieldValue.ENTITY_ID, id);
        List<FieldValue> fieldValues = fieldDbService.loadFieldValues(cmap);
        locationEntity.addAllFieldValues(fieldValues);
    }


    public void saveLocation(Location loc) {
        LocationEntity le = loc.createEntity();
        this.em.merge(le);
        for (Field f : loc.getFields()) {
            fieldDbService.save(f);
        }

        for (FieldValue fv : loc.getFieldValues()) {
            FieldValueEntity fve = fv.createEntity();
            em.merge(fve);
        }

    }

}

