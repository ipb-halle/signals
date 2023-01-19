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
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * DB service for groups
 */

@Stateless
public class GroupDbService {


    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    private Logger logger = LoggerFactory.getLogger(GroupDbService.class);

    /**
     * @return a list of Groups 
     */
    public List<Group> load() {
        return loadBy(new HashMap<String, Object> ());
    }

    public Group loadByName(String name) {
        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(Group.GROUP_NAME, name);
        List<Group> result = loadBy(cmap);
        if (result.size() == 1) {
            return result.get(0);
        }
        logger.debug("Result list for name '{}' contains {} Group records", name, result.size());
        return null;
    }

    public List<Group> loadBy(Map<String, Object> cmap) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);
        criteriaQuery.select(root);

        if (cmap.get(Group.GROUP_NAME) != null) {
            criteriaQuery.where(builder.equal(root.get(Group.GROUP_NAME), cmap.get(Group.GROUP_NAME)));
        }

        List<Group> result = new ArrayList<> ();
        for (Group group : em.createQuery(criteriaQuery).getResultList()) {
            result.add(group); 
        }
        return result;
    }

    public Map<String, Group> loadMappedById(Map<String, Object> cmap) {
        Map<String, Group> resultMap = new HashMap<> ();
        for (Group group : loadBy(cmap)) {
            resultMap.put(group.getId(), group);
        }
        return resultMap;
    }

    public Group loadById(String id) {
        return this.em.find(Group.class, id);
    }

    public void save(Group g) {
        this.em.merge(g);
    }
}

