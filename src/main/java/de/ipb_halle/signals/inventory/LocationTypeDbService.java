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

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * Database service for location types
 */

@Stateless
public class LocationTypeDbService {

    private final static String LOCATION_TYPE_ENTITY_PREFIX = "location:";
    private final static String LOCATION_TYPE_ENTITY_SUFFIX = ":ivt";
    private final static Logger logger = LogManager.getLogger(LocationTypeDbService.class);

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
            CriteriaQuery<LocationTypeEntity> criteriaQuery = builder.createQuery(LocationTypeEntity.class);
            Root<LocationTypeEntity> root = criteriaQuery.from(LocationTypeEntity.class);
            criteriaQuery.select(root);

            for (LocationTypeEntity locationType : em.createQuery(criteriaQuery).getResultList()) {
                entityIds.add(LOCATION_TYPE_ENTITY_PREFIX
                        + locationType.getId()
                        + LOCATION_TYPE_ENTITY_SUFFIX);
            }
        } catch (Exception e) {
            logger.error("LocationTypeDbService: -> Error while fetching container type IDs: {}", e.getMessage(), e);
        }

        return entityIds;
    }

    public LocationType loadById(String id) {
        LocationTypeEntity lte = this.em.find(LocationTypeEntity.class, id);
        return new LocationType(lte, null);
    }

    public void save(LocationType lt) {
        LocationTypeEntity lte = lt.createEntity();
        this.em.merge(lte);
    }
}


