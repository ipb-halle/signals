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

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.inventory.ContainerDbService;
import de.ipb_halle.signals.inventory.ContainerEntity;
import de.ipb_halle.signals.inventory.LocationProcessorBean;
import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


@Stateless
@LocalBean
public class SampleProcessorBean {

    @Inject
    private SampleRestService sampleRestService;

    @Inject
    private SampleDbService sampleDbService;

    @Inject
    private ContainerDbService containerDbService;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private static final Logger logger = LogManager.getLogger(LocationProcessorBean.class);


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processSingleSample(String sampleId) {
        doProcessSample(sampleId);
    }

    private void doProcessSample(String sampleId) {
        //If transaction marked for rollback, then break it
        if (transactionSynchronizationRegistry.getTransactionStatus() == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            logger.error("SampleProcessorBean:-> Transaction is marked for rollback, skipping.");
            return;
        }

        try {
            // 1) Load sample via REST
            Sample sample = sampleRestService.doGetSample(sampleId);

            // 2) set parent container id
            sample.setParentContainerId(loadContainerIdForSample(sampleId));

            // 3) receive property keys for each sample
            sampleRestService.doGetSampleProperties(sample);

            // 4) fetch each property explicitly for given sample in order to process attachments using received property keys
            //sampleRestService.doGetEachPropertyExplicitly(sample);


            sampleDbService.save(sample);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private String loadContainerIdForSample(String sampleId) {
        List<ContainerEntity> containerEntities = containerDbService.loadAllContainersWithMaterialIdSample();
        for (ContainerEntity containerEntity : containerEntities) {
            if (containerEntity.getMaterialId().equals(sampleId)) {
                return containerEntity.getId();
            }
        }
        logger.warn("Warning! Where is no container for this sample = {}!!", sampleId);
        return "null";
    }


}
