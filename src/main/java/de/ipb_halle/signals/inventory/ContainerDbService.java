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

import de.ipb_halle.signals.field.*;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Database service for containers
 */

@Stateless
@PersistenceElements(entities = {ContainerEntity.class})
public class ContainerDbService {

    public static final Logger logger = LogManager.getLogger(ContainerDbService.class);

    @Inject
    private FieldDbService fieldDbService;


    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;


    public ContainerEntity loadContainerById(String id) {
        ContainerEntity containerEntity = this.em.find(ContainerEntity.class, id);
        loadFieldValues(containerEntity, id);
        return containerEntity;
    }

    public List<ContainerEntity> loadAllContainersWithMaterialIdSample() {
        List<ContainerEntity> containerEntities = new ArrayList<>();

        // 1) create a criteria builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        // 2) create a criteria query for container entity
        CriteriaQuery<ContainerEntity> criteriaQuery = builder.createQuery(ContainerEntity.class);

        // 3) define the root table
        Root<ContainerEntity> root = criteriaQuery.from(ContainerEntity.class);

        // 4) condition: WHERE material_id like 'sample:%'
        Predicate materialLike = builder.like(root.get("materialId"), "sample:%");

        // 5) set condition in query
        criteriaQuery.where(materialLike);

        // 6) process the query call
        TypedQuery<ContainerEntity> query = em.createQuery(criteriaQuery);
        return query.getResultList();

    }

    private void loadFieldValues(ContainerEntity containerEntity, String id) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(FieldValue.ENTITY_ID, id);
        List<FieldValue> fieldValues = fieldDbService.loadFieldValues(cmap);
        containerEntity.addAllFieldValues(fieldValues);
    }

    public void saveContainer(Container container) {
        ContainerEntity ce = container.createEntity();
        logger.info("Container Processor Bean:-> typeId={}, typeName={}", ce.getContainerTypeId(), ce.getContainerTypeName());

        this.em.merge(ce);
        for (Field f : container.getFields()) {
            fieldDbService.save(f);
        }

        for (FieldValue fv : container.getFieldValues()) {
            FieldValueEntity fve = fv.createEntity();
            em.merge(fve);
        }
    }

}

