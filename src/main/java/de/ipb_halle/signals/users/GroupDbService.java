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
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/** 
 * DB service for groups
 */

@Stateless
public class GroupDbService {


    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    /**
     * @return a list of Groups 
     */
    public List<Group> load() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);
        criteriaQuery.select(root);

        List<Group> result = new ArrayList<> ();
        for (Group group : em.createQuery(criteriaQuery).getResultList()) {
            result.add(group); 
        }
        return result;
    }

    public Group loadById(int id) {
        return this.em.find(Group.class, id);
    }

    public void save(Group g) {
        this.em.merge(g);
    }
}

