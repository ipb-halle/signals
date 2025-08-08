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

@Stateless
@LocalBean
public class AdoProcessorBean {

    @Inject
    AdoRestService adoRestService;

    @Inject
    AdoDbService adoDbService;

    @Resource
    TransactionSynchronizationRegistry synchronizationRegistry;

    private static final Logger logger = (Logger) LogManager.getLogger(AdoProcessorBean.class);


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleAdo(String eid) {
        doProcessAdo(eid);
    }

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

            for (AdoProperty property : ado.getProperties()) {
                logger.info("ADO PROPERTIES propertyID = {}, propertyName = {}\n", property.getPropertyId(), property.getPropertyName());
            }

            for (AdoPropertyValue pv : ado.getPropertyValues()) {
                logger.info("ADO PROPERTIES VALUES propertyID = {}, propertyValue= {}\n", pv.getPropertyId(), pv.getPropertyValue());
            }

            adoDbService.save(ado);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
