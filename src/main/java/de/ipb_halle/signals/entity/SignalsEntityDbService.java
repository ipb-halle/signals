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
import de.ipb_halle.signals.users.Group;
import de.ipb_halle.signals.users.UserEntity;
import de.ipb_halle.signals.util.EmbeddedKeyValue;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Database service for signals entities
 */

@Stateless
@PersistenceElements(entities={SignalsEntity.class, SignalsEntityChild.class,
        Share.class, EffectiveShare.class, GroupShare.class, UserShare.class})
public class SignalsEntityDbService {

    public record SEloadInfo(
            boolean children,
            boolean userGroupShares,
            boolean effectiveShares) {}

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    private static final Logger logger = LoggerFactory.getLogger(SignalsEntityDbService.class);

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
        List<SignalsEntity> resultList = em.createQuery(criteriaQuery).getResultList();
        for (SignalsEntity entity : resultList) {
            results.add(new SignalsEntityDTO(entity, dynEnumManager));
        }
        return results;
    }

    public SignalsEntityDTO loadById(String id) {
        return loadById(id, new SEloadInfo(true, false, false));
    }

    public SignalsEntityDTO loadById(String id, SEloadInfo what) {
        SignalsEntity entity = this.em.find(SignalsEntity.class, id);
        if (entity != null) {
            SignalsEntityDTO dto = new SignalsEntityDTO(entity, dynEnumManager);
            if (what.children) {
                dto.addChildren(loadChildren(id));
            }
            if (what.userGroupShares) {
                Map<String, Object> cmap = new HashMap<>();
                cmap.put(Share.ENTITY_ID, id);
                dto.addShares(loadUserShares(cmap));
                dto.addShares(loadGroupShares(cmap));
            }
            if (what.effectiveShares) {
                Map<String, Object> cmap = new HashMap<>();
                cmap.put(Share.ENTITY_ID, id);
                dto.addShares(loadEffectiveShares(cmap));
            }
            return dto;
        }
        logger.error("loadById({}) returned null", id);
        return null;
    }

    private List<ISignalsEntity> loadChildren(String id) {
        List<ISignalsEntity> results = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SignalsEntityChild> criteriaQuery = builder.createQuery(SignalsEntityChild.class);
        Root<SignalsEntityChild> root = criteriaQuery.from(SignalsEntityChild.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(EmbeddedKeyValue.ID).get("id"), id));
        for (SignalsEntityChild child : em.createQuery(criteriaQuery).getResultList()) {
            results.add(loadById(child.getChildId()));
        }
        return results;
    }

    /**
     * Load effective shares of entities (i.e. user privileges
     * @param cmap
     * @return
     */
    public List<Share> loadEffectiveShares(Map<String, Object> cmap) {
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<EffectiveShare> criteriaQuery = builder.createQuery(EffectiveShare.class);
        Root<EffectiveShare> root = criteriaQuery.from(EffectiveShare.class);
        criteriaQuery.select(root);

        if (cmap.containsKey(Share.ENTITY_ID)) {
            predicates.add(builder.equal(root.get("id").get(EmbeddedKeyValue.ID),
                    cmap.get(Share.ENTITY_ID)));
        }
        if (cmap.containsKey(Share.USER_ID)) {
            predicates.add(builder.equal(root.get("id").get(EmbeddedKeyValue.VALUE),
                    cmap.get(Share.USER_ID)));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        List<Share> results = new ArrayList<>();
        for (Share share : em.createQuery(criteriaQuery).getResultList()) {
            results.add(share);
        }
        return results;
    }

    /**
     * Load effective shares of entities (i.e. user privileges
     * @param cmap
     * @return
     */
    public List<Share> loadGroupShares(Map<String, Object> cmap) {
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GroupShare> criteriaQuery = builder.createQuery(GroupShare.class);
        Root<GroupShare> root = criteriaQuery.from(GroupShare.class);
        criteriaQuery.select(root);

        if (cmap.containsKey(Share.ENTITY_ID)) {
            predicates.add(builder.equal(root.get("id").get(EmbeddedKeyValue.ID),
                    cmap.get(Share.ENTITY_ID)));
        }
        if (cmap.containsKey(Share.GROUP_ID)) {
            predicates.add(builder.equal(root.get("id").get(EmbeddedKeyValue.VALUE),
                    cmap.get(Share.GROUP_ID)));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        List<Share> results = new ArrayList<>();
        for (Share share : em.createQuery(criteriaQuery).getResultList()) {
            results.add(share);
        }
        return results;
    }

    /**
     * Load effective shares of entities (i.e. user privileges
     * @param cmap
     * @return
     */
    public List<Share> loadUserShares(Map<String, Object> cmap) {
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserShare> criteriaQuery = builder.createQuery(UserShare.class);
        Root<UserShare> root = criteriaQuery.from(UserShare.class);
        criteriaQuery.select(root);

        if (cmap.containsKey(Share.ENTITY_ID)) {
            predicates.add(builder.equal(root.get("id").get(EmbeddedKeyValue.ID),
                    cmap.get(Share.ENTITY_ID)));
        }
        if (cmap.containsKey(Share.USER_ID)) {
            predicates.add(builder.equal(root.get("id").get(EmbeddedKeyValue.VALUE),
                    cmap.get(Share.USER_ID)));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        List<Share> results = new ArrayList<>();
        for (Share share : em.createQuery(criteriaQuery).getResultList()) {
            results.add(share);
        }
        return results;
    }

    public void save(SignalsEntityDTO dto) {
        SignalsEntity entity = dto.createEntity();
        this.em.merge(entity);
        saveChildren(dto);
        saveShares(dto);
    }

    private void saveChildren(SignalsEntityDTO dto)  {
        for (ISignalsEntity child : dto.getChildren()) {
            em.merge(new SignalsEntityChild(dto.getId(), child.getId()));
        }
    }

    public void remove(Share share) {
        switch (share.getType()) {
            case GROUP:
                this.em.remove((GroupShare) share);
                break;
            case USER:
                this.em.remove((UserShare) share);
                break;
            default:
                logger.warn("Illegal call to remove() for {}", share.getClass().getName());
        }
    }

    public void saveShares(SignalsEntityDTO dto) {
        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(Share.ENTITY_ID, dto.getId());
        List<Share> dbShares = loadUserShares(cmap);
        dbShares.addAll(loadGroupShares(cmap));
        saveOrRemoveShares(dto, dbShares);
    }

    /**
     * Persist share records not present in the database and
     * remove database records, which are not present in the DTO
     * @param dto
     * @param dbShares
     */
    private void saveOrRemoveShares(SignalsEntityDTO dto, List<Share> dbShares) {
        Set<Share> dtoShares = new HashSet<> (dto.getShares());    // set will be modified, need to make a copy
        removeDeletedSharesAndTrim(dtoShares, dbShares);
        for (Share share : dtoShares) {
            save(share);
        };
    }

    private void removeDeletedSharesAndTrim(Set<Share> dtoShares, List<Share> dbShares) {
        for (Share dbShare : dbShares) {
            if (dtoShares.contains(dbShare)) {
                // dbShare exists in DB and DTO
                // ==> trim DTO set
                dtoShares.remove(dbShare);
            } else {
                // dbShare has been deleted from DTO
                // ==> remove from DB
                remove(dbShare);
            }
        }
    }

    /**
     * Save a share information. Only UserShares or GroupShares will be
     * persisted - EffectiveShares will be ignored. <code>save</code> will
     * safeguard against non-existing users and groups, as it is possible
     * to delete a group, while it is still referenced by GroupShares.
     * @param share
     */
    public void save(Share share) {
        switch(share.getType()) {
            case GROUP:
                saveGroupShare((GroupShare) share);
                break;
            case USER:
                saveUserShare((UserShare) share);
                break;
            default:
                logger.debug("Save(share) ignored for {}", share.getClass().getName());
        }
    }

    private void saveGroupShare(GroupShare share) {
        if (this.em.find(Group.class, share.getGroupId()) != null) {
            this.em.merge(share);
        }
    }

    private void saveUserShare(UserShare share) {
        if (this.em.find(UserEntity.class, share.getUserId()) != null) {
            this.em.merge(share);
        }
    }
}
