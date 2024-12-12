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
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * DB service for field definitions
 */

@Stateless
public class FieldDbService {


    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    private Logger logger = LoggerFactory.getLogger(FieldDbService.class);

    /**
     * Load a FieldDefinition entity by its ID.
     *
     * @param id the ID of the FieldDefinition entity
     * @return the FieldDefinition entity
     */
    public Field loadById(String id) {
        FieldDefinition fieldDefinition = this.em.find(FieldDefinition.class, id);
        if (fieldDefinition != null) {
            FieldType fieldType = (FieldType) dynEnumManager.valueOf(fieldDefinition.getFieldType());
            FieldDesignation designation = (FieldDesignation) dynEnumManager.valueOf(fieldDefinition.getFieldDesignation());
            List<FieldOption> options = loadFieldOptions(id);
            List<FieldMeasure> measures = loadFieldMeasures(id);
            Field field = new Field(fieldDefinition, fieldType, designation)
                    .addAllOptions(options)
                    .addAllMeasures(measures);
            return field;
        }
        return null;
    }

    public List<Field> load(Map<String, Object> cmap) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<FieldDefinition> criteriaQuery = criteriaBuilder.createQuery(FieldDefinition.class);
        Root<FieldDefinition> root = criteriaQuery.from(FieldDefinition.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (cmap.containsKey(Field.FIELD_ID)) {
            predicates.add(criteriaBuilder.equal(root.get("id"), cmap.get(Field.FIELD_ID)));
        }
        if (cmap.containsKey(Field.FIELD_DESIGNATION)) {
            predicates.add(criteriaBuilder.equal(root.get(Field.FIELD_DESIGNATION), cmap.get(Field.FIELD_DESIGNATION)));
        }
        if (cmap.containsKey(Field.DEFINING_ENTITY_ID)) {
            List<String> definingEntityIds = (List<String>) cmap.get(Field.DEFINING_ENTITY_ID);
            /**
             * "defining_entity_id" is a "library id" or other name is "AssetType id" from field
             * takes all field from DB for all libraries
             * translation in sql -> select * (Object Field) from field_definitions where defining_entity_id in ('definingEntityIds');
             * ToDo: eventually complication with a big number of defining entitles (e.g. experiments) by querying with IN clause -> rework later
             */
            predicates.add(root.get(Field.DEFINING_ENTITY_ID).in(definingEntityIds));
            //predicates.add(criteriaBuilder.equal(root.get(Field.DEFINING_ENTITY_ID), cmap.get(Field.DEFINING_ENTITY_ID)));
        }
        if (cmap.containsKey(Field.FIELD_TITLE)) {
            predicates.add(criteriaBuilder.equal(root.get(Field.FIELD_TITLE), cmap.get(Field.FIELD_TITLE)));
        }
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        List<Field> results = new ArrayList<>();
        for (FieldDefinition entity : em.createQuery(criteriaQuery).getResultList()) {
            FieldType type = (FieldType) dynEnumManager.valueOf(entity.getFieldType());
            FieldDesignation designation = (FieldDesignation) dynEnumManager.valueOf(entity.getFieldDesignation());
            Field value = new Field(entity, type, designation);
            /*ToDO:
             * load options
             * load measures
             */
            logger.info("this is class FieldDBService, method load. You have to implement load options and load measures to field");
            results.add(value);
        }
        return results;
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

        criteriaQuery.where(builder.equal(root.get("id").get("id"), id));
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

    public void save(Collection<FieldValue> fieldValues) {
        for (FieldValue value : fieldValues) {
            FieldValueEntity entity = value.createEntity();
            this.em.merge(entity);
        }
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


    private Field getGloballyDefinedAttachmentField(String id, String title) {
        Field field = loadById(id);
        if (field == null) {
            field = new Field();
            field.setId(id);
            field.setTitle(title);
            field.setCalculated(Boolean.FALSE);
            field.setHidden(Boolean.FALSE);
            field.setMultiSelect(Boolean.FALSE);
            field.setRequired(Boolean.FALSE);
            field.setFieldType(FieldType.valueOf(FieldType.ATTACHED_FILE));
            field.setDesignation(FieldDesignation.valueOf(FieldDesignation.DEFAULT));
            save(field);
            return loadById(id);
        }
        return field;
    }

    public Field getImageField() {
        return getGloballyDefinedAttachmentField(Field.FIELD_ID_IMAGE, "Image");
    }

    public Field getDrawingField() {
        return getGloballyDefinedAttachmentField(Field.FIELD_ID_CHEMICAL_DRAWING, "Chemical Drawing");
    }

    public Field getSequenceField() {
        return getGloballyDefinedAttachmentField(Field.FIELD_ID_SEQUENCE, "Sequence");
    }
}

