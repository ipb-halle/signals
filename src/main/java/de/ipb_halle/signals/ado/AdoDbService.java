/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.ado;

import de.ipb_halle.inhouse.InhouseCompound;
import de.ipb_halle.signals.ado.properties.AdoProperty;
import de.ipb_halle.signals.ado.properties.AdoPropertyEntity;
import de.ipb_halle.signals.ado.properties.AdoPropertyValue;
import de.ipb_halle.signals.ado.properties.AdoPropertyValueEntity;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.ArrayList;
import java.util.List;

@Stateless
@LocalBean
@PersistenceElements(entities = {InhouseCompound.class, AdoEntity.class, AdoPropertyEntity.class, AdoPropertyValueEntity.class, DynEnumManager.class})
public class AdoDbService {

    private static final Logger logger = (Logger) LogManager.getLogger(AdoDbService.class);

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    public void save(Ado ado) {
        AdoEntity entity = ado.createEntity();
        em.merge(entity);

        for (AdoProperty adoProperty : ado.getProperties()) {
            if (adoProperty.getPropertyId() == null) {
                logger.warn("AdoDbService:-> Skipping ado with null id: {}\n", adoProperty.getPropertyName());
                continue;
            }
            AdoPropertyEntity adoPropertyEntity = adoProperty.createEntity();
            em.merge(adoPropertyEntity);
        }

        for (AdoPropertyValue adoPropertyValue : ado.getPropertyValues()) {
            if (adoPropertyValue.getPropertyId() == null) {
                logger.warn("Skipping AdoPropertyValue with null propertyId: adoId = {} \n", ado.getId());
                continue;
            }
            AdoPropertyValueEntity adoPropertyValueEntity = (AdoPropertyValueEntity) adoPropertyValue.createEntity();
            em.merge(adoPropertyValueEntity);
        }
    }

    public void saveAll(List<Ado> ipbAdoObjects) {
        for (Ado ado : ipbAdoObjects) {
            this.save(ado);
        }
    }

    public List<Ado> loadAll() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AdoEntity> criteriaQuery = criteriaBuilder.createQuery(AdoEntity.class);

        Root<AdoEntity> root = criteriaQuery.from(AdoEntity.class);
        criteriaQuery.select(root);

        List<AdoEntity> entities = em.createQuery(criteriaQuery).getResultList();
        List<Ado> adoList = new ArrayList<>();
        for (AdoEntity entity : entities) {
            adoList.add(new Ado(entity, dynEnumManager));
        }
        return adoList;
    }

    public int findMaxIpbCode() {
        String sql = """
                SELECT MAX(CAST(REGEXP_REPLACE(ipb_code, '\\D', '', 'g') AS INTEGER))
                FROM inhouse_compounds
                WHERE ipb_code ~ '^IPB\\d+$'
                """;

        Integer result = (Integer) em.createNativeQuery(sql).getSingleResult();
        return result != null ? result : 0;
    }
}
