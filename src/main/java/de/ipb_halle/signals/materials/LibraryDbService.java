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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.field.FieldDefinitionDbService;
import de.ipb_halle.signals.field.FieldDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

/** 
 * DB service for material libraries
 */

@Stateless
public class LibraryDbService {

    public final String ASSET = "A";
    public final String BATCH = "B";
    public final String LIBRARY_ID = "library_id";
    public final String LIBRARY_FIELD_TYPE = "type";

    @Inject 
    private FieldDefinitionDbService fieldService;

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    private Logger logger = LoggerFactory.getLogger(LibraryDbService.class);


    private List<FieldDefinition> loadFieldDefinitions(String id, String type) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<LibraryFieldDefinition> criteriaQuery = builder.createQuery(LibraryFieldDefinition.class);
        Root<LibraryFieldDefinition> root = criteriaQuery.from(LibraryFieldDefinition.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<Predicate> ();
        predicates.add(builder.equal(root.get(LIBRARY_ID), id));
        predicates.add(builder.equal(root.get(LIBRARY_FIELD_TYPE), type));

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));

        List<FieldDefinition> result = new ArrayList<> ();
        for (LibraryFieldDefinition fd : em.createQuery(criteriaQuery).getResultList()) {
            logger.debug("Load library field: lib={}, fd={}, type={}", id, fd.getFieldDefinitionId(), fd.getType());
            result.add(fieldService.loadById(fd.getFieldDefinitionId()));
        }
        return result;
    }

    
    public Library loadById(String id) {
        logger.debug("Load library: id={}", id);
        LibraryEntity entity = em.find(LibraryEntity.class, id);
        return new Library(entity, 
            loadFieldDefinitions(id, ASSET),
            loadFieldDefinitions(id, BATCH));
    }

    public void save(Library lib) {
        logger.debug("Store library: id={}", lib.getId());
        LibraryEntity le = lib.createEntity();
        em.merge(le);
        saveFieldDefinitions(lib.getAssetFieldDefinitions(), le.getId(), ASSET);
        saveFieldDefinitions(lib.getBatchFieldDefinitions(), le.getId(), BATCH);
    }

    private void saveFieldDefinitions(Set<FieldDefinition> fieldDefinitions, String libraryId, String type) {
        for (FieldDefinition fd : fieldDefinitions) {
            fieldService.save(fd);
            LibraryFieldDefinition libFD = new LibraryFieldDefinition()
                .setLibraryId(libraryId)
                .setFieldDefinitionId(fd.getId())
                .setType(type);
            em.merge(libFD);
            logger.debug("Stored library field: lib={} fd={} type={}", libraryId, fd.getId(), type);
        }
    }
}
