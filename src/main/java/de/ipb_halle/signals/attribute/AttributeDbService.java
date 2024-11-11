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
package de.ipb_halle.signals.attribute;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Database service for signals attributes
 */

@Stateless
public class AttributeDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;
    
    private Logger logger = LoggerFactory.getLogger(AttributeDbService.class);

    private List<AttributeValue> loadValues(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<AttributeValue> criteriaQuery = builder.createQuery(AttributeValue.class);
        Root<AttributeValue> root = criteriaQuery.from(AttributeValue.class);
        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get("id").get("id"), id));
        return em.createQuery(criteriaQuery).getResultList();
    }

    public Attribute loadById(String id) {
        AttributeDefinition def = this.em.find(AttributeDefinition.class, id);
        if (def != null) {

            if (AttributeType.valueOf(AttributeType.CHOICE).equals(dynEnumManager.valueOf(def.getType()))) {
                List<AttributeValue> values = loadValues(id);
                for (AttributeValue v : values) {
                    System.out.printf("LOADED AttributeValue(%s, %s)\n", v.getId(), v.getValue());
                }
                return new Attribute(def, dynEnumManager, loadValues(id));
            } else {
                return new Attribute(def, dynEnumManager, null);
            }
        }
        return null;
    }

    public void save(Attribute attr) {
        AttributeDefinition entity = attr.createEntity();
        this.em.merge(entity);
        if (AttributeType.valueOf(AttributeType.CHOICE).equals(attr.getType()))
            for (AttributeValue value : attr.getOptions()) {
                logger.trace(String.format("AttributeValue(id=%s): %s\n", value.getId(), value.getValue()));
                this.em.merge(value);
            }
    }

}


