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

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Stateless
@PersistenceElements(entities = {SampleEntity.class})
public class SampleDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private FieldDbService fieldDbService;

    private static final Logger logger = LogManager.getLogger(SamplesManager.class);


    public void save(Sample sample) {
        SampleEntity se = sample.createEntity();
        for (SampleProperty sp : sample.getProperties()) {
            SamplePropertyEntity spe = sp.createEntity();
            this.em.merge(spe);
        }
        for (SamplePropertyValue spv : sample.getPropertyValues()) {
            SamplePropertyValueEntity spve = spv.createEntity();
            this.em.merge(spve);
        }
        SampleTemplateEntity sampleTemplateEntity = new SampleTemplateEntity()
                .setTemplateId(sample.getTemplateId())
                .setTemplateName(sample.getTemplateName());
        this.em.merge(sampleTemplateEntity);
        this.em.merge(se);
    }

    public SampleEntity loadSampleEntityById(String id) {
        return em.find(SampleEntity.class, id);
    }

    public void loadSampleProperties(Sample sample) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SamplePropertyEntity> query = cb.createQuery(SamplePropertyEntity.class);
        Root<SamplePropertyEntity> root = query.from(SamplePropertyEntity.class);

        // WHERE sp.template.templateId = :templateId
        query.select(root)
                .where(cb.equal(root.get("template").get("templateId"), sample.getTemplateId()));

        List<SamplePropertyEntity> results = em.createQuery(query).getResultList();

        for (SamplePropertyEntity propertyEntity : results) {
            SampleProperty sampleProperty = new SampleProperty(propertyEntity);
            sample.addProperty(sampleProperty);
        }


    }

    public void loadSamplePropertyValues(Sample sample) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SamplePropertyValueEntity> query = cb.createQuery(SamplePropertyValueEntity.class);
        Root<SamplePropertyValueEntity> root = query.from(SamplePropertyValueEntity.class);

        // WHERE id.sampleId = :sampleId
        query.select(root)
                .where(cb.equal(root.get("id").get("sampleId"), sample.getId()));

        List<SamplePropertyValueEntity> results = em.createQuery(query).getResultList();

        for (SamplePropertyValueEntity valueEntity : results) {
            SamplePropertyValue value = new SamplePropertyValue(valueEntity);
            sample.addPropertyValue(value);
        }
    }

    public void loadTemplate (Sample  sample){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SampleTemplateEntity> query = cb.createQuery(SampleTemplateEntity.class);
        Root<SampleTemplateEntity> root = query.from(SampleTemplateEntity.class);

        Subquery<String> usedTemplateIds = query.subquery(String.class);
        Root<SampleEntity> sampleRoot = usedTemplateIds.from(SampleEntity.class);
        usedTemplateIds.select(sampleRoot.get("template").get("id"));

        query.select(root).where(root.get("id").in(usedTemplateIds));

        List<SampleTemplateEntity> templatesUsed = em.createQuery(query).getResultList();
        logger.info(Arrays.toString(templatesUsed.toArray()));
    }
}
