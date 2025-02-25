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
package de.ipb_halle.signals.users;

import de.ipb_halle.tda.PersistenceElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * DB service for roles
 */

@Stateless
@PersistenceElements(entities = { RoleEntity.class, RolePriv.class, RolePrivDef.class })
public class RoleDbService {

    private static Set<String> rolePrivileges;

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;


    protected synchronized String getRolePrivilege(String privilege) {
        if (rolePrivileges == null) {
            loadRolePrivileges();
        }
        if (! rolePrivileges.contains(privilege)) {
            saveRolePrivilege(privilege);
            rolePrivileges.add(privilege);
        }
        return privilege;
    }

    private void loadRolePrivileges() {
        rolePrivileges = new HashSet<> ();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<RolePrivDef> criteriaQuery = builder.createQuery(RolePrivDef.class);
        Root<RolePrivDef> root = criteriaQuery.from(RolePrivDef.class);
        criteriaQuery.select(root);

        for (RolePrivDef entity: em.createQuery(criteriaQuery).getResultList()) {
            rolePrivileges.add(entity.getId());
        }
    }

    private void saveRolePrivilege(String privilege) {
        this.em.merge(new RolePrivDef(privilege));
    }

    /**
     * @return a list of Roles
     */
    public List<Role> load() {
        return loadBy(new HashMap<String, Object> ());
    }

    public Role loadByName(String name) {
        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(Role.ROLE_NAME, name);
        List<Role> result = loadBy(cmap);
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    public List<Role> loadBy(Map<String, Object> cmap) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<RoleEntity> criteriaQuery = builder.createQuery(RoleEntity.class);
        Root<RoleEntity> root = criteriaQuery.from(RoleEntity.class);
        criteriaQuery.select(root);

        if (cmap.get(Role.ROLE_NAME) != null) {
            criteriaQuery.where(builder.equal(root.get(Role.ROLE_NAME), cmap.get(Role.ROLE_NAME)));
        }

        List<Role> result = new ArrayList<> ();
        for (RoleEntity entity: em.createQuery(criteriaQuery).getResultList()) {
            Role role = new Role(entity);
            role.setPrivileges(loadRolePrivileges(entity));
            result.add(role);
        }
        return result;
    }

    public Map<String, Role> loadMappedById(Map<String, Object> cmap) {
        Map<String, Role> resultMap = new HashMap<> ();
        for (Role role : loadBy(cmap)) {
            resultMap.put(role.getId(), role);
        }
        return resultMap;
    }


    public Set<String> loadRolePrivileges(RoleEntity entity) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<RolePriv> criteriaQuery = builder.createQuery(RolePriv.class);
        Root<RolePriv> root = criteriaQuery.from(RolePriv.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root
                .get(RolePriv.ROLE_PRIV_ID)
                .get(RolePrivId.ROLE_ID),
                entity.getId()));

        Set<String> privileges = new HashSet<> ();
        for (RolePriv rp : em.createQuery(criteriaQuery).getResultList()) {
            privileges.add(rp.getRolePrivilege());
        }
        return privileges;
    }

    public Role loadById(String id) {
        RoleEntity entity = this.em.find(RoleEntity.class, id);
        if (entity != null) {
            Role role = new Role(entity);
            role.setPrivileges(loadRolePrivileges(entity));
            return role;
        }
        return null;
    }

    public Role save(Role r) {
        this.em.merge(r.createEntity());
        savePrivileges(r);
        return loadById(r.getId());
    }

    public void savePrivileges(Role r) {
        for (String privilege : r.getPrivileges()) {
            this.em.merge(new RolePriv(r.getId(), getRolePrivilege(privilege)));
        }
    }
}

