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

package de.ipb_halle.signals.sample;

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

import java.util.List;

@Stateless
@PersistenceElements(entities = {SampleEntity.class})
public class SampleDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    private static final Logger logger = LogManager.getLogger(SampleDbService.class);

    public void save(Sample sample) {
        SampleEntity sampleEntity = sample.createEntity();

        em.merge(sampleEntity);

        for (SampleProperty sampleProperty : sample.getProperties()) {
            if (sampleProperty.getPropertyId() == null || sampleProperty.getPropertyId().isBlank()) {
                logger.warn("Skipping property with null or blank ID: {}", sampleProperty.getPropertyName());
                continue;
            }
            SamplePropertyEntity samplePropertyEntity = sampleProperty.createEntity(); // no template version
            em.merge(samplePropertyEntity);
        }

        for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
            if (samplePropertyValue.getPropertyId() == null || samplePropertyValue.getPropertyId().isBlank()) {
                logger.warn("Skipping SamplePropertyValue with null or blank propertyId: sampleId={}", samplePropertyValue.getSampleId());
                continue;
            }
            SamplePropertyValueEntity samplePropertyValueEntity = samplePropertyValue.createEntity();
            if (samplePropertyValueEntity != null) {
                em.merge(samplePropertyValueEntity);
            }
        }
    }


    public SampleEntity loadSampleEntityById(String id) {
        return em.find(SampleEntity.class, id);
    }

    public void loadSamplePropertyValuesWithProperties(Sample sample) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SamplePropertyValueEntity> query = cb.createQuery(SamplePropertyValueEntity.class);
        Root<SamplePropertyValueEntity> root = query.from(SamplePropertyValueEntity.class);
        root.fetch("property", JoinType.INNER);

        // WHERE id.sampleId = :sampleId
        query.select(root).where(cb.equal(root.get("id").get("sampleId"), sample.getId()));

        List<SamplePropertyValueEntity> results = em.createQuery(query).getResultList();

        for (SamplePropertyValueEntity valueEntity : results) {
            SamplePropertyValue value = new SamplePropertyValue(valueEntity);
            sample.addPropertyValue(value);
            sample.addProperty(new SampleProperty(valueEntity.getProperty()));
        }
    }


}
