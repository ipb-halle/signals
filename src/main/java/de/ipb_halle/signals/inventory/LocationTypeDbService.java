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
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


/**
 * Database service for location types
 */

@Stateless
@PersistenceElements(entities = {LocationTypeEntity.class})
public class LocationTypeDbService {

    private final static String LOCATION_TYPE_ENTITY_PREFIX = "location:";
    private final static String LOCATION_TYPE_ENTITY_SUFFIX = ":ivt";
    private final static Logger logger = LogManager.getLogger(LocationTypeDbService.class);

    @Inject
    private FieldDbService fieldDbService;

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    /**
     * @return a set of entity ids of LocationTypes. The entity ids are
     * prefixed and suffixed to match the form of the entities endpoint
     * (e.g. "b9fab5b8-6c26-47f8-8694-320c7c439879" is converted
     * to "location:b9fab5b8-6c26-47f8-8694-320c7c439879:ivt")
     */
    public Set<String> getLocationTypIds() {
        Set<String> entityIds = new HashSet<>();
        try {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<LocationTypeEntity> query = builder.createQuery(LocationTypeEntity.class);
            Root<LocationTypeEntity> root = query.from(LocationTypeEntity.class);
            query.select(root);

            List<LocationTypeEntity> resultList = em.createQuery(query).getResultList();

            if (resultList == null || resultList.isEmpty()) {
                logger.error("LocationTypeDbService:-> No results found for location types");
                return entityIds;
            }
            for (LocationTypeEntity locationType : resultList) {
                if (locationType.getId() != null) {
                    // entityIds.add(LOCATION_TYPE_ENTITY_PREFIX + locationType.getId() + LOCATION_TYPE_ENTITY_SUFFIX);
                    entityIds.add(locationType.getId());
                } else {
                    logger.error("LocationTypeDbService:-> Null id found for a LocationTypeEntity {}\n", locationType.getId());
                }
            }
        } catch (Exception e) {
            logger.error("LocationTypeDbService: -> Error while fetching container type IDs: {}", e.getMessage(), e);
        }
        return entityIds;
    }

    public LocationType loadById(String id) {
        LocationTypeEntity lte = this.em.find(LocationTypeEntity.class, id);
        return new LocationType(lte, loadFields(lte.getId()));
    }

    private List<Field> loadFields(String id) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Field.DEFINING_ENTITY_ID, id);
        return fieldDbService.loadFields(cmap);
    }

    public void save(LocationType lt) {
        LocationTypeEntity lte = lt.createEntity();
        this.em.merge(lte);
        for (Field fd : lt.getFields()) {
            fd.setDefiningEntityId(lt.getId());
            fieldDbService.save(fd);
        }
    }
}


