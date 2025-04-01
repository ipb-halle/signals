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

import de.ipb_halle.signals.sample.*;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

@Stateless
@PersistenceElements(entities = {ExperimentEntity.class})
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
            logger.info("ExperimentDbService:-> SAVING PROPERTY VALUES()");
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

        for (ExperimentPropertyValueEntity valueEntity : results) {
            ExperimentPropertyValue value = new ExperimentPropertyValue(valueEntity);
            experiment.addPropertyValue(value);
            experiment.addProperty(new ExperimentProperty(valueEntity.getProperty()));
        }
        logger.info("EXPERIMENT DB SERVICE, EXPERIMENT PROPERTIES = {}\n, EXEPRIMENT PROPERTY VALUES = {}\n",
                Arrays.toString(experiment.getProperties().toArray()),
                Arrays.toString(experiment.getPropertyValues().toArray()));
    }
}
