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

package de.ipb_halle.signals.experiments;

import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Stateless
@LocalBean
public class ExperimentProcessorBean {

    @Inject
    private ExperimentRestService experimentRestService;

    @Inject
    private ExperimentDbService experimentDbService;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private static final Logger logger = LogManager.getLogger(ExperimentProcessorBean.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleExperiment(String experimentId) {
        doProcessExperiment(experimentId);
    }

    private void doProcessExperiment(String experimentId) {
        //If transaction marked for rollback, then break it
        if (transactionSynchronizationRegistry.getTransactionStatus() == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("ExperimentProcessorBean:-> Transaction is marked for rollback, skipping.");
        }

        try {
            // 1) Load experiment via REST
            Experiment experiment = experimentRestService.doGetExperiment(experimentId);

            // 3) receive property keys for each sample
            if (experiment.getTemplateId() != null) {
                experimentRestService.doGetExperimentProperties(experiment);
            }

            // 3) recieve experiment property values upon template id
            experimentRestService.doGetExperimentPropertyValues(experiment);

            // 4) fetch each property explicitly for given sample in order to process attachments using received property keys
            //sampleRestService.doGetEachPropertyExplicitly(sample);


            experimentDbService.save(experiment);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
