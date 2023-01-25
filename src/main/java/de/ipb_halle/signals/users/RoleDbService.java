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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RoleDbService {


    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

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
        CriteriaQuery<Role> criteriaQuery = builder.createQuery(Role.class);
        Root<Role> root = criteriaQuery.from(Role.class);
        criteriaQuery.select(root);

        if (cmap.get(Role.ROLE_NAME) != null) {
            criteriaQuery.where(builder.equal(root.get(Role.ROLE_NAME), cmap.get(Role.ROLE_NAME)));
        }

        List<Role> result = new ArrayList<> ();
        for (Role role: em.createQuery(criteriaQuery).getResultList()) {
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

    public Role loadById(String id) {
        return this.em.find(Role.class, id);
    }

    public void save(Role r) {
        this.em.merge(r);
    }
}

