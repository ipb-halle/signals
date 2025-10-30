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

/**
 * Service class for handling persistence operations for Ado objects in the Signals database.
 * <p>
 * This stateless EJB provides methods to save, load, and query {@link Ado} instances,
 * their properties, and property values. It uses JPA for database access and
 * {@link DynEnumManager} for handling dynamic enumerations related to Ado entities.
 * </p>
 *
 * <p>Supported operations:</p>
 * <ul>
 *     <li>Saving single or multiple Ado objects along with their properties and property values</li>
 *     <li>Loading Ado objects by template ID or retrieving all</li>
 *     <li>Retrieving sorted results by IPB code</li>
 *     <li>Finding the maximum numeric IPB code value in the database</li>
 * </ul>
 *
 * @author swittche
 * @version 1.0
 * @since 2025
 */
@Stateless
@LocalBean
@PersistenceElements(entities = {InhouseCompound.class, AdoEntity.class, AdoPropertyEntity.class, AdoPropertyValueEntity.class, DynEnumManager.class})
public class AdoDbService {

    private static final Logger logger = (Logger) LogManager.getLogger(AdoDbService.class);

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    /**
     * Saves a single {@link Ado} object to the database, including its properties and property values.
     * <p>
     * If any {@link AdoProperty} or {@link AdoPropertyValue} has a {@code null} propertyId,
     * it will be skipped and a warning will be logged.
     * </p>
     *
     * @param ado The Ado object to be persisted.
     */
    public void save(Ado ado) {
        AdoEntity entity = ado.createEntity();
        // Save Ado
        em.merge(entity);

        // Save Properties
        for (AdoProperty adoProperty : ado.getProperties()) {
            if (adoProperty.getPropertyId() == null) {
                logger.warn("AdoDbService:-> Skipping ado with null id: {}\n", adoProperty.getPropertyName());
                continue;
            }
            AdoPropertyEntity adoPropertyEntity = adoProperty.createEntity();
            logger.debug("Saving AdoProperty: propertyId = {}", adoProperty.getPropertyId());

            em.merge(adoPropertyEntity);
        }

        //todo-> this part of code should be disabled by first synchronization or by generating new ado types like ado-3 where new id of field can appear
        for (AdoPropertyValue adoPropertyValue : ado.getPropertyValues()) {
            if (adoPropertyValue.getPropertyId() == null) {
                logger.warn("Skipping AdoPropertyValue with null propertyId: adoId = {} \n", ado.getId());
                continue;
            }
            AdoPropertyValueEntity adoPropertyValueEntity = (AdoPropertyValueEntity) adoPropertyValue.createEntity();
            logger.debug("Trying to save AdoPropertyValue: propertyId = {}, adoId = {}",
                    adoPropertyValue.getPropertyId(), ado.getId());

            em.merge(adoPropertyValueEntity);
        }
    }

    /**
     * Saves a list of {@link Ado} objects to the database by calling {@link #save(Ado)} for each.
     *
     * @param ipbAdoObjects List of Ado objects to persist.
     */
    public void saveAll(List<Ado> ipbAdoObjects) {
        for (Ado ado : ipbAdoObjects) {
            this.save(ado);
        }
    }

    /**
     * Loads all {@link Ado} objects from the database.
     * <p>
     * This method retrieves all {@link AdoEntity} records and converts them into {@link Ado} instances.
     * </p>
     *
     * @return A list of all Ado objects found in the database.
     */
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

    /**
     * Loads all {@link Ado} objects that match the given template ID.
     * <p>
     * This method retrieves all {@link AdoEntity} records where {@code templateId} matches
     * the given value and converts them into {@link Ado} instances.
     * </p>
     *
     * @param templateId The template ID used to filter Ado objects.
     * @return A list of matching Ado objects.
     */
    public List<Ado> loadByTemplateId(String templateId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AdoEntity> criteriaQuery = criteriaBuilder.createQuery(AdoEntity.class);

        Root<AdoEntity> root = criteriaQuery.from(AdoEntity.class);
        criteriaQuery.select(root)
                .where(criteriaBuilder.equal(root.get("templateId"), templateId));

        List<AdoEntity> entities = em.createQuery(criteriaQuery).getResultList();
        List<Ado> adoList = new ArrayList<>();
        for (AdoEntity entity : entities) {
            adoList.add(new Ado(entity, dynEnumManager));
        }
        return adoList;
    }

    /**
     * Loads all {@link Ado} objects with the given template ID, sorted by their IPB code in ascending order.
     *
     * @param templateId The template ID used to filter Ado objects.
     * @return A sorted list of matching Ado objects.
     */
    public List<Ado> loadByTemplateIdSorted(String templateId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AdoEntity> cq = cb.createQuery(AdoEntity.class);
        Root<AdoEntity> root = cq.from(AdoEntity.class);

        cq.select(root)
                .where(cb.equal(root.get("templateId"), templateId))
                .orderBy(cb.asc(root.get("ipbCode")));

        List<AdoEntity> entities = em.createQuery(cq).getResultList();
        List<Ado> adoList = new ArrayList<>();
        for (AdoEntity entity : entities) {
            adoList.add(new Ado(entity, dynEnumManager));
        }
        return adoList;
    }

    /**
     * Finds the maximum numeric value of the IPB code in the {@code inhouse_compounds} table.
     * <p>
     * The IPB code is expected to follow the pattern {@code IPB<number>}.
     * Non-digit characters are stripped before parsing the number.
     * </p>
     *
     * @return The maximum numeric IPB code found, or {@code 0} if none exists.
     */
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
