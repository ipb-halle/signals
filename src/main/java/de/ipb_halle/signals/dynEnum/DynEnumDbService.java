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
package de.ipb_halle.signals.dynEnum;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Database Service for DynEnums
 */

@Stateless
public class DynEnumDbService {

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    private final Logger logger = LoggerFactory.getLogger(DynEnumDbService.class);

    /**
     * obtain all DynEnums
     */
    public List<DynEnum> load() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<DynEnum> criteriaQuery = builder.createQuery(DynEnum.class);
        Root<DynEnum> root = criteriaQuery.from(DynEnum.class);
        criteriaQuery.select(root);

        return em.createQuery(criteriaQuery).getResultList();
    }

    /**
     * save a dynEnum
     */
    public DynEnum save(DynEnum dyn) {
        return this.em.merge(dyn);
    }
}
