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

import java.util.HashSet;
import java.util.Set;


/** 
 * Database service for location types
 */

@Stateless
public class LocationTypeDbService {

    private final static String LOCATION_TYPE_ENTITY_PREFIX = "location:";
    private final static String LOCATION_TYPE_ENTITY_SUFFIX = ":ivt";

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    /**
     *
     * @return a set of entity ids of LocationTypes. The entity ids are
     * prefixed and suffixed to match the form of the entities endpoint
     * (e.g. "b9fab5b8-6c26-47f8-8694-320c7c439879" is converted
     * to "location:b9fab5b8-6c26-47f8-8694-320c7c439879:ivt")
     */
    public Set<String> getLocationTypIds() {
        Set<String> entityIds = new HashSet<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<LocationType> criteriaQuery = builder.createQuery(LocationType.class);
        Root<LocationType> root = criteriaQuery.from(LocationType.class);
        criteriaQuery.select(root);

        for (LocationType locationType: em.createQuery(criteriaQuery).getResultList()) {
            entityIds.add(LOCATION_TYPE_ENTITY_PREFIX
                    + locationType.getId()
                    + LOCATION_TYPE_ENTITY_SUFFIX);
        }
        return entityIds;
    }

    public LocationType loadById(String id) {
        return this.em.find(LocationType.class, id);
    }

    public void save(LocationType lt) {
        this.em.merge(lt);
    }
}


