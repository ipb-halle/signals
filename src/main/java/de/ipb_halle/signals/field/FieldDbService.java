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
package de.ipb_halle.signals.field;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;


/**
 * DB service for field definitions
 */

@Stateless
public class FieldDbService {


    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    /**
     * Load a FieldDefinition entity by its ID.
     *
     * @param id the ID of the FieldDefinition entity
     * @return the FieldDefinition entity
     */
    public Field loadById(String id) {
        FieldDefinition fieldDefinition = this.em.find(FieldDefinition.class, id);
        FieldType fieldType = (FieldType) dynEnumManager.valueOf(fieldDefinition.getFieldType());
        FieldDesignation designation = (FieldDesignation) dynEnumManager.valueOf(fieldDefinition.getFieldDesignation());
        List<FieldOption> options = loadFieldOptions(id);
        List<FieldMeasure> measures = loadFieldMeasures(id);
        Field field = new Field(fieldDefinition, fieldType, designation)
                .addAllOptions(options)
                .addAllMeasures(measures);
        return field;
    }

    private List<FieldMeasure> loadFieldMeasures(String id) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<FieldMeasure> criteriaQuery = criteriaBuilder.createQuery(FieldMeasure.class);
        Root<FieldMeasure> root = criteriaQuery.from(FieldMeasure.class);
        criteriaQuery.select(root);

        criteriaQuery.where(criteriaBuilder.equal(root.get("id").get("field_id"), id));
        return em.createQuery(criteriaQuery).getResultList();
    }

    private List<FieldOption> loadFieldOptions(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<FieldOption> criteriaQuery = builder.createQuery(FieldOption.class);
        Root<FieldOption> root = criteriaQuery.from(FieldOption.class);
        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get("id").get("field_id"), id));
        return em.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Save a FieldDefinition entity to the database.
     *
     * @param field the FieldDefinition entity to save
     */
    public void save(Field field) {
        field.setDesignation((FieldDesignation) dynEnumManager.valueOf(field.getDesignation()));
        this.em.merge(field.createEntity());
        saveOptions(field);
        saveMeasures(field);
    }

    private void saveMeasures(Field field) {
        for (FieldMeasure measure : field.getMeasures()) {
            this.em.merge(measure);
        }
    }

    private void saveOptions(Field field) {
        for (FieldOption option : field.getOptions()) {
            this.em.merge(option);
        }
    }


}

