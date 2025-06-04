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
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.tda.PersistenceElements;
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

@Stateless
@PersistenceElements(entities = {MaterialEntity.class})
public class MaterialDbService {

    public final static String ENTITY_ID = "id";
    public final static String ENTITY_TYPE = "entityType";
    public final static String LIBRARY_ID = "libraryId";
    public final static String MATERIAL_ID = "materialId";

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private FieldDbService fieldDbService;

    private Logger logger = LoggerFactory.getLogger(LibraryDbService.class);

    /**
     * Obtain a list of materials matching given criteria
     */
    public List<Material> loadMaterials(Map<String, Object> cmap) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<MaterialEntity> criteriaQuery = criteriaBuilder.createQuery(MaterialEntity.class);
        Root<MaterialEntity> root = criteriaQuery.from(MaterialEntity.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (cmap.containsKey(MATERIAL_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(MATERIAL_ID), cmap.get(MATERIAL_ID)));
        }
        if (cmap.containsKey(LIBRARY_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(LIBRARY_ID), cmap.get(LIBRARY_ID)));
        }
        if (cmap.containsKey(ENTITY_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(ENTITY_ID), cmap.get(ENTITY_ID)));
        }
        if (cmap.containsKey(ENTITY_TYPE)) {
            predicates.add(criteriaBuilder.equal(root.get(ENTITY_TYPE), cmap.get(ENTITY_TYPE)));
        }
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        List<Material> results = new ArrayList<>();
        for (MaterialEntity entity : em.createQuery(criteriaQuery).getResultList()) {
            results.add(loadById(entity.getId()));
        }
        return results;
    }

    public Material loadById(String id) {
        MaterialEntity entity = this.em.find(MaterialEntity.class, id);
        Material mat = new Material(entity, (EntityType) dynEnumManager.valueOf(entity.getEntityType()));
        loadFieldValues(mat, id);
        if (mat.getEntityType().equals(EntityType.valueOf(Material.ENTITY_TYPE_ASSET))) {
            loadSynonyms(mat, id);
        }
        if (entity.getMaterialId() != null) {
            Material material = loadById(entity.getMaterialId());
            mat.setParentMaterial(material);
        }
        return mat;
    }

    private void loadFieldValues(Material mat, String id) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(FieldValue.ENTITY_ID, id);
        mat.addAllFieldValues(fieldDbService.loadFieldValues(cmap));
    }

    private void loadSynonyms(Material mat, String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Synonym> criteriaQuery = builder.createQuery(Synonym.class);
        Root<Synonym> root = criteriaQuery.from(Synonym.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(Synonym.ENTITY_ID).get(Synonym.ENTITY_ID), id));
        mat.addAllSynonyms(em.createQuery(criteriaQuery).getResultList());
    }

    public void save(Material mat) {
        String materialId = mat.getId();
        String parentId = (mat.getParentMaterial() != null ? mat.getParentMaterial().getId() : null);

        if (materialId.startsWith(Material.MATERIAL_ASSET_PREFIX)) {
            if (parentId != null) {
                throw new IllegalArgumentException("Asset '" + materialId + "' must not have parent");
            }
        } else if (materialId.startsWith("batch: ")) {
            if (parentId == null) {
                throw new IllegalArgumentException("Batch '" + materialId + "' must have a parent");
            }
            if (em.find(MaterialEntity.class, parentId) == null) {
                throw new IllegalArgumentException("Parent asset '" + parentId + "' not found");
            }
        }

        MaterialEntity entity = mat.createEntity();
        this.em.merge(entity);
        saveSynonyms(mat.getSynonyms());
        fieldDbService.save(mat.getFieldValues());
    }

    private void saveSynonyms(Collection<Synonym> synonyms) {
        for (Synonym synonym : synonyms) {
            this.em.merge(synonym);
        }
    }
}
