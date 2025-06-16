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
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class InhouseDbService {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<InhouseCompound> loadCompounds() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseCompound> criteriaQuery = criteriaBuilder.createQuery(InhouseCompound.class);
        Root<InhouseCompound> root = criteriaQuery.from(InhouseCompound.class);
        criteriaQuery.select(root);

        List<InhouseCompound> result = this.em.createQuery(criteriaQuery).getResultList();
        for (InhouseCompound mat : result) {
            mat.addSynonyms(loadSynonymsById(InhouseSynonym.SYNONYM_COMPOUND, mat.getMolId()));
        }
        return result;
    }

    public InhouseCompound loadCompoundByMolId(int molId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseCompound> criteriaQuery = criteriaBuilder.createQuery(InhouseCompound.class);
        Root<InhouseCompound> root = criteriaQuery.from(InhouseCompound.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("molId"), molId));
        List<InhouseCompound> result = this.em.createQuery(criteriaQuery).getResultList();
        if (result.size() != 1) {
            System.out.printf("loadCompoundByMolId(%d) - query returned %d instances\n", molId, result.size());
            return null;
        }
        InhouseCompound compound = result.get(0);
        compound.addSynonyms(loadSynonymsById(InhouseSynonym.SYNONYM_COMPOUND, molId));
        return compound;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<InhouseExperiment> loadExperiments() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseExperiment> criteriaQuery = criteriaBuilder.createQuery(InhouseExperiment.class);
        Root<InhouseExperiment> root = criteriaQuery.from(InhouseExperiment.class);
        criteriaQuery.select(root);

        return em.createQuery(criteriaQuery).getResultList();

    }

    public List<InhouseTaxon> loadAllTaxa() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseTaxon> criteriaQuery = criteriaBuilder.createQuery(InhouseTaxon.class);
        Root<InhouseTaxon> root = criteriaQuery.from(InhouseTaxon.class);
        criteriaQuery.select(root);
        // ascending order --> parent before child
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("id")));
        List<InhouseTaxon> result = this.em.createQuery(criteriaQuery).getResultList();
        for (InhouseTaxon taxon : result) {
            if (taxon.getLevel().equals(InhouseTaxon.TAXONOMY_SPECIES)) {
                taxon.addSynonyms(loadSynonymsById(InhouseSynonym.SYNONYM_ORGANISM, taxon.getInhouseId()));
            }
        }
        return result;
    }

    public InhouseTaxon loadTaxonByInhouseId(String level, int inhouseId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseTaxon> criteriaQuery = criteriaBuilder.createQuery(InhouseTaxon.class);
        Root<InhouseTaxon> root = criteriaQuery.from(InhouseTaxon.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("inhouseId"), inhouseId));
        predicates.add(criteriaBuilder.equal(root.get("level"), level));
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        List<InhouseTaxon> result = this.em.createQuery(criteriaQuery).getResultList();
        if (result.size() != 1) {
            System.out.printf("loadTaxonByInhouseId(%d) - query returned %d instances\n", inhouseId, result.size());
            return null;
        }
        InhouseTaxon taxon = result.get(0);
        if (level.equals(InhouseTaxon.TAXONOMY_SPECIES)) {
            taxon.addSynonyms(loadSynonymsById(InhouseSynonym.SYNONYM_ORGANISM, inhouseId));
        }
        return taxon;
    }

    public InhouseTaxon loadTaxonByOrganismId(int organismId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseTaxon> criteriaQuery = criteriaBuilder.createQuery(InhouseTaxon.class);
        Root<InhouseTaxon> root = criteriaQuery.from(InhouseTaxon.class);
        criteriaQuery.select(root);

        criteriaQuery.where(criteriaBuilder.equal(root.get("organismId"), organismId));

        List<InhouseTaxon> result = this.em.createQuery(criteriaQuery).getResultList();
        if (result.size() != 1) {
            System.out.printf("loadTaxonByOrganismId(%d) - query returned %d instances\n", organismId, result.size());
            return null;
        }
        InhouseTaxon taxon = result.get(0);
        if (taxon.getLevel().equals(InhouseTaxon.TAXONOMY_SPECIES)) {
            taxon.addSynonyms(loadSynonymsById(InhouseSynonym.SYNONYM_ORGANISM, taxon.getInhouseId()));
        }
        return taxon;
    }

    public List<InhouseSynonym> loadSynonymsById(String type, Integer inhouseId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseSynonym> criteriaQuery = criteriaBuilder.createQuery(InhouseSynonym.class);
        Root<InhouseSynonym> root = criteriaQuery.from(InhouseSynonym.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("inhouseId"), inhouseId));
        predicates.add(criteriaBuilder.equal(root.get("type"), type));
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        return this.em.createQuery(criteriaQuery).getResultList();
    }

    public InhouseCorrelation loadCorrelationByProcedureId(int procId) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<InhouseCorrelation> query = builder.createQuery(InhouseCorrelation.class);
        Root<InhouseCorrelation> root = query.from(InhouseCorrelation.class);
        query.select(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("procedureId"), procId));
        query.where(builder.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(query).getSingleResult();
    }


    public void save(InhouseCompound mat) {
        this.em.merge(mat);
    }

    public void save(InhouseContainer container) {
        this.em.merge(container);
    }

    public void save(InhouseCorrelation corr) {
        this.em.merge(corr);
    }

    public void save(InhouseExperiment experiment) {
        this.em.merge(experiment);
    }

    public InhouseLocation save(InhouseLocation location) {
        return this.em.merge(location);
    }

    public void save(InhouseSynonym synonym) {
        this.em.merge(synonym);
    }

    public void save(InhouseTaxon taxon) {
        this.em.merge(taxon);
    }
}
