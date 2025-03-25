/*
 *
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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

package de.ipb_halle.inhouse;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

@Stateless
public class InhouseDbService {

    @PersistenceContext
    private EntityManager em;

    public List<InhouseCompound> loadCompounds() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseCompound> criteriaQuery = criteriaBuilder.createQuery(InhouseCompound.class);
        Root<InhouseCompound> root = criteriaQuery.from(InhouseCompound.class);
        criteriaQuery.select(root);

        List<InhouseCompound> result = this.em.createQuery(criteriaQuery).getResultList();
        for(InhouseCompound mat : result) {
            mat.addSynonyms(loadCompoundSynonymsByMolId(mat.getMolId()));
        }
        return result;
    }

    public InhouseCompound loadCompoundByMolId(int molId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseCompound> criteriaQuery = criteriaBuilder.createQuery(InhouseCompound.class);
        Root<InhouseCompound> root = criteriaQuery.from(InhouseCompound.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("molId"),molId));
        List<InhouseCompound> result = this.em.createQuery(criteriaQuery).getResultList();
        if (result.size() != 1) {
            System.out.printf("loadCompoundById(%d) - query returned %d instances\n", molId, result.size());
            return null;
        }
        InhouseCompound compound = result.get(0);
        compound.addSynonyms(loadCompoundSynonymsByMolId(molId));
        return compound;
    }

    public List<InhouseCompoundSynonym> loadCompoundSynonymsByMolId(Integer molId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseCompoundSynonym> criteriaQuery = criteriaBuilder.createQuery(InhouseCompoundSynonym.class);
        Root<InhouseCompoundSynonym> root = criteriaQuery.from(InhouseCompoundSynonym.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("molId"), molId));
        return this.em.createQuery(criteriaQuery).getResultList();
    }

    public void save(InhouseCompound mat) {
        this.em.merge(mat);
    }

    public void save(InhouseCompoundSynonym synonym) {
        this.em.merge(synonym);
    }
}
