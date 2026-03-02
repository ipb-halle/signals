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

package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.rest.RestResultIterator;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class SEProcessorBean {

    @Inject
    private SignalsEntityDbService dbService;

    @Inject
    private SignalsEntityRestService restService;

    private static final Logger logger = LoggerFactory.getLogger(SEProcessorBean.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processEntity(RuntimeConfig config, SignalsEntityDTO parentEntity) {
        try {
            if (config.followChildren) {
                RestResultIterator<SignalsEntityDTO> iterator = restService.doGetChildren(parentEntity);
                while (iterator.hasNext()) {
                    parentEntity.addChild(iterator.next());
                }
            }
            if (config.updateDb) {
                dbService.save(parentEntity);
            }
            logger.trace(parentEntity.dump());
        } catch (Exception e) {
            //Transaction will be automatically rolled back if exception occurs
            logger.error("Error in ProcessSingleMaterial, material {}: {}",
                    parentEntity.getId(), e.getMessage(), e);
        }
    }
}
