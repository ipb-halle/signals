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
package de.ipb_halle.signals.config;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DB service for local entity configuration
 */

@Stateless
public class LocalConfigDbService {


    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    /**
     * @param cmap criteria map for the query
     * @return list of configuration items
     */
    public List<LocalConfig> load(Map<String, Object> cmap) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<LocalConfig> criteriaQuery = criteriaBuilder.createQuery(LocalConfig.class);
        Root<LocalConfig> root = criteriaQuery.from(LocalConfig.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (cmap.containsKey(LocalConfig.CRITERIA_FEATURE)) {
            predicates.add(criteriaBuilder.equal(root.get(LocalConfig.CRITERIA_FEATURE),
                    cmap.get(LocalConfig.CRITERIA_FEATURE)));
        }
        if (cmap.containsKey(LocalConfig.CRITERIA_ENTITY_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(LocalConfig.CRITERIA_ENTITY_ID),
                    cmap.get(LocalConfig.CRITERIA_ENTITY_ID)));
        }
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(criteriaQuery).getResultList();
    }

    public LocalConfig loadById(Integer id) {
        return this.em.find(LocalConfig.class, id);
    }

    public void save(LocalConfig c) {
        this.em.merge(c);
    }
}
