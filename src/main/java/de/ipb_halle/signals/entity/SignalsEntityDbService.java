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
package de.ipb_halle.signals.entity;

import java.util.*;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;


/**
 * Database service for signals entities
 */

@Stateless
public class SignalsEntityDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    public List<SignalsEntityDTO> load(Map<String, Object> cmap) {
        List<SignalsEntityDTO> results = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SignalsEntity> criteriaQuery = builder.createQuery(SignalsEntity.class);
        Root<SignalsEntity> root = criteriaQuery.from(SignalsEntity.class);
        criteriaQuery.select(root);

        if (cmap.containsKey(SignalsEntityRestService.PARAMETER_START)) {
            predicates.add(builder.greaterThan(root.get("editedAt"),
                    (Date) cmap.get(SignalsEntityRestService.PARAMETER_START)));
        }
        if (cmap.containsKey(SignalsEntityRestService.PARAMETER_END)) {
            predicates.add(builder.lessThan(root.get("editedAt"),
                    (Date) cmap.get(SignalsEntityRestService.PARAMETER_END)));
        }
        if (cmap.containsKey(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES)) {
            List<Integer> enumIds = dynEnumManager.getDynEnumIds(
                    (EntityType[]) cmap.get(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES));
            predicates.add(root.get("type").in(enumIds));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        for (SignalsEntity entity : em.createQuery(criteriaQuery).getResultList()) {
            results.add(new SignalsEntityDTO(entity, dynEnumManager));
        }
        return results;
    }

    public SignalsEntityDTO loadById(String id) {
        SignalsEntity entity = this.em.find(SignalsEntity.class, id);
        if (entity != null) {
            SignalsEntityDTO dto = new SignalsEntityDTO(entity, dynEnumManager);
            dto.addChildren(loadChildren(id));
        }
        return null;
    }

    private List<ISignalsEntity> loadChildren(String id) {
        List<ISignalsEntity> results = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SignalsEntityChild> criteriaQuery = builder.createQuery(SignalsEntityChild.class);
        Root<SignalsEntityChild> root = criteriaQuery.from(SignalsEntityChild.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("id"), id));
        for (SignalsEntityChild child : em.createQuery(criteriaQuery).getResultList()) {
            results.add(loadById(child.getChildId()));
        }
        return results;
    }


    public void save(SignalsEntityDTO dto) {
        SignalsEntity entity = dto.createEntity();
        this.em.merge(entity);
        saveChildren(dto);
    }

    public void saveChildren(SignalsEntityDTO dto)  {
        for (ISignalsEntity child : dto.getChildren()) {
            em.merge(new SignalsEntityChild(dto.getId(), child.getId()));
        }
    }
}


