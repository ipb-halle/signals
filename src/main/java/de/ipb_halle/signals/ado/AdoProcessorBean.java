/*
 *
 *  * IPB Signals client
 *  * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.signals.ado.properties.AdoProperty;
import de.ipb_halle.signals.ado.properties.AdoPropertyValue;
import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

/**
 * Stateless EJB for processing single Ado entities.
 * <p>
 * This bean is responsible for retrieving an Ado from the remote Signals REST API,
 * logging its properties and property values, fetching its template fields if available,
 * and persisting it to the database. The processing runs within its own transaction
 * to ensure database operations are committed independently.
 * </p>
 *
 * <p>Main responsibilities:</p>
 * <ul>
 *     <li>Retrieve an Ado object by its EID via {@link AdoRestService}</li>
 *     <li>Fetch additional template fields if a template ID exists</li>
 *     <li>Log Ado properties and property values for debugging purposes</li>
 *     <li>Persist the processed Ado object using {@link AdoDbService}</li>
 * </ul>
 *
 * <p>
 * The class uses {@link TransactionSynchronizationRegistry} to check the transaction status
 * and skip processing if the transaction is marked for rollback.
 * </p>
 *
 * @author swittche
 * @version 1.0
 * @since 2025
 */
@Stateless
@LocalBean
public class AdoProcessorBean {

    private static final Logger logger = (Logger) LogManager.getLogger(AdoProcessorBean.class);
    @Inject
    AdoRestService adoRestService;
    @Inject
    AdoDbService adoDbService;
    @Resource
    TransactionSynchronizationRegistry synchronizationRegistry;

    /**
     * Processes a single Ado object identified by its EID within a new transaction.
     * <p>
     * This method ensures that the retrieval, logging, and persistence of the Ado
     * are executed in an isolated transactional context.
     * </p>
     *
     * @param eid The entity identifier (EID) of the Ado to process.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleAdo(String eid) {
        doProcessAdo(eid);
    }

    /**
     * Internal helper method that performs the actual processing of the Ado.
     * <p>
     * Steps performed:
     * <ol>
     *     <li>Check if the current transaction is marked for rollback; if so, skip processing.</li>
     *     <li>Retrieve the Ado from the REST service using {@link AdoRestService#doGetAdo(String)}.</li>
     *     <li>If the Ado has a template ID, fetch its template fields via {@link AdoRestService#fetchAdoTemplateFields(Ado)}.</li>
     *     <li>Log all properties and property values for diagnostic purposes.</li>
     *     <li>Persist the Ado in the database via {@link AdoDbService#save(Ado)}.</li>
     * </ol>
     * Any exception during processing will be wrapped and rethrown as a {@link RuntimeException}.
     * </p>
     *
     * @param eid The entity identifier (EID) of the Ado to process.
     */
    private void doProcessAdo(String eid) {
        if (synchronizationRegistry.getTransactionStatus() == Status.STATUS_MARKED_ROLLBACK) {
            logger.error("AdoProcessorBean -> Transaction is marked for rollback, skipping");
            return;
        }

        try {
            Ado ado = adoRestService.doGetAdo(eid);
            logger.info("ADO templateId= {}, eid = {}\n", ado.getTemplateId(), ado.getEid());

            if (ado.getTemplateId() != null) {
                adoRestService.fetchAdoTemplateFields(ado);
            }

            // Only for debugging cases
//            for (AdoProperty property : ado.getProperties()) {
//                logger.info("ADO PROPERTIES propertyID = {}, propertyName = {}\n", property.getPropertyId(), property.getPropertyName());
//            }
//
//            for (AdoPropertyValue pv : ado.getPropertyValues()) {
//                logger.info("ADO PROPERTIES VALUES propertyID = {}, propertyValue= {}\n", pv.getPropertyId(), pv.getPropertyValue());
//            }

            adoDbService.save(ado);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
