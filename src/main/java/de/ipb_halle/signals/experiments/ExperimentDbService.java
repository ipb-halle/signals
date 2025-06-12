/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.experiments;

import de.ipb_halle.signals.dynEnum.DynEnum;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Stateless
@PersistenceElements(entities = {ExperimentEntity.class, ExperimentPropertyEntity.class, ExperimentPropertyValueEntity.class})
public class ExperimentDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager entityManager;

    private static final Logger logger = LogManager.getLogger(ExperimentDbService.class);

    public ExperimentEntity loadExperimentById(String id) {
        return entityManager.find(ExperimentEntity.class, id);
    }

    public void save(Experiment experiment) {
        ExperimentEntity experimentEntity = experiment.createEntity();

        entityManager.merge(experimentEntity);

        for (ExperimentProperty experimentProperty : experiment.getProperties()) {
            if (experimentProperty.getPropertyId() == null || experimentProperty.getPropertyId().isBlank()) {
                logger.warn("Skipping property with null or blank ID: {}", experimentProperty.getPropertyName());
                continue;
            }
            ExperimentPropertyEntity experimentPropertyEntity = experimentProperty.createEntity(); // no template version
            entityManager.merge(experimentPropertyEntity);
        }

        for (ExperimentPropertyValue experimentPropertyValue : experiment.getPropertyValues()) {
            if (experimentPropertyValue.getPropertyId() == null || experimentPropertyValue.getPropertyId().isBlank()) {
                logger.warn("Skipping SamplePropertyValue with null or blank propertyId: experimentId={}", experimentPropertyValue.getExperimentId());
                continue;
            }
            ExperimentPropertyValueEntity experimentPropertyValueEntity = experimentPropertyValue.createEntity();
            if (experimentPropertyValueEntity != null) {
                entityManager.merge(experimentPropertyValueEntity);
            }
        }
    }

    public void loadExperimentPropertyValuesWithProperties(Experiment experiment) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExperimentPropertyValueEntity> query = cb.createQuery(ExperimentPropertyValueEntity.class);
        Root<ExperimentPropertyValueEntity> root = query.from(ExperimentPropertyValueEntity.class);
        root.fetch("property", JoinType.INNER);

        query.select(root).where(cb.equal(root.get("id").get("experimentId"), experiment.getId()));

        List<ExperimentPropertyValueEntity> results = entityManager.createQuery(query).getResultList();

        logger.info("ExperimentPropertyValueEntity results ={}\n", Arrays.toString(results.toArray()));

        for (ExperimentPropertyValueEntity valueEntity : results) {
            ExperimentPropertyValue value = new ExperimentPropertyValue(valueEntity);
            experiment.addPropertyValue(value);
            experiment.addProperty(new ExperimentProperty(valueEntity.getProperty()));
        }
    }

    // Method to load the Experiment Property Entities by Experiment Template_Id
    public List<ExperimentPropertyEntity> loadExperimentPropertyValues(Map<String, Object> cmap) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExperimentPropertyEntity> criteriaQuery = criteriaBuilder.createQuery(ExperimentPropertyEntity.class);
        Root<ExperimentPropertyEntity> root = criteriaQuery.from(ExperimentPropertyEntity.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(
                root.get(ExperimentProperty.TEMPLATE_ID),
                cmap.get(ExperimentProperty.TEMPLATE_ID)));

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        List<ExperimentPropertyEntity> resultList = entityManager.createQuery(criteriaQuery).getResultList();
       // logger.info("loadExperimentProperties= {}\n", Arrays.toString(resultList.toArray()));
        return resultList;
    }
}
