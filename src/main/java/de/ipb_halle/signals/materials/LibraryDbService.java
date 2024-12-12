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

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.FieldDesignation;

import java.util.Set;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB service for material libraries
 */

@Stateless
public class LibraryDbService {

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private DynEnumManager dynEnumManager;

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    private Logger logger = LoggerFactory.getLogger(LibraryDbService.class);

    public List<Library> load(Map<String, Object> cmap) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<LibraryEntity> criteriaQuery = criteriaBuilder.createQuery(LibraryEntity.class);
        Root<LibraryEntity> root = criteriaQuery.from(LibraryEntity.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (cmap.containsKey(Library.HAS_DRAWING)) {
            predicates.add(criteriaBuilder.equal(root.get(Library.HAS_DRAWING), cmap.get(Library.HAS_DRAWING)));
        }
        if (cmap.containsKey(Library.HAS_IMAGE)) {
            predicates.add(criteriaBuilder.equal(root.get(Library.HAS_IMAGE), cmap.get(Library.HAS_IMAGE)));
        }
        if (cmap.containsKey(Library.HAS_SEQUENCE)) {
            predicates.add(criteriaBuilder.equal(root.get(Library.HAS_SEQUENCE), cmap.get(Library.HAS_SEQUENCE)));
        }
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        List<Library> results = new ArrayList<>();
        for (LibraryEntity entity : em.createQuery(criteriaQuery).getResultList()) {
            Library library = new Library(entity);
            library.addAllAssetFields(loadFieldDefinitions(entity.getId(), 
                    FieldDesignation.valueOf(FieldDesignation.ASSET)));
            library.addAllBatchFields(loadFieldDefinitions(entity.getId(), 
                    FieldDesignation.valueOf(FieldDesignation.BATCH)));
            results.add(library);
        }
        return results;
    }

    public Library loadById(String id) {
        logger.debug("Load library: id={}", id);
        LibraryEntity entity = em.find(LibraryEntity.class, id);
        Library lib = new Library(entity);
        lib.addAllAssetFields(loadFieldDefinitions(entity.getId(), 
                FieldDesignation.valueOf(FieldDesignation.ASSET)));
        lib.addAllBatchFields(loadFieldDefinitions(entity.getId(), 
                FieldDesignation.valueOf(FieldDesignation.BATCH)));
        return lib;
    }

    public List<Field> loadFieldDefinitions(String id, FieldDesignation designation) {
        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(Field.FIELD_ID, id);
        cmap.put(Field.FIELD_DESIGNATION, dynEnumManager.valueOf(designation).getId());
        return fieldDbService.load(cmap);
    }

    public void save(Library lib) {
        logger.debug("Store library: id={}", lib.getId());
        LibraryEntity le = lib.createEntity();
        em.merge(le);
        saveFields(lib.getAssetFields(), le.getId());
        saveFields(lib.getBatchFields(), le.getId());
    }

    private void saveFields(Set<Field> fields, String libraryId) {
        for (Field f : fields) {
            fieldDbService.save(f);
            LibraryField libFD = new LibraryField()
                .setLibraryId(libraryId)
                .setFieldId(f.getId());
            em.merge(libFD);
            logger.debug("Stored library field: lib={}/{} field={}", libraryId, f.getDesignation().getValue(), f.getId());
        }
    }
}
