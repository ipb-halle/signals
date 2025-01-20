/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.attachment;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.entity.*;
import de.ipb_halle.signals.materials.MaterialRestService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AttachmentManager {
    private Logger logger = LoggerFactory.getLogger(AttachmentManager.class);

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private AttachmentRestService attachmentRestService;

    @Inject
    private AttachmentDbService attachmentDbService;

    @Inject
    private MaterialRestService materialRestService;


    public void manageAttachments(RuntimeConfig runtimeConfig, EntityType[] includedTypes, Date[] dateRange) {
        Map<String, Object> hmap = new HashMap<>();

        hmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            hmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        hmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, includedTypes);

        /**
         * result:
         * ATTACHMENT manager key: 'includeTypes' and value: '[experiment.EntityType]' of hmap
         * ATTACHMENT manager key: 'start' and value: '2023-01-01T00:00:00.000+0100' of hmap
         * ATTACHMENT manager key: 'end' and value: '2024-12-09T14:24:54.345+0100' of hmap
         */
        //hmap.forEach((mapkey, mapvalue) -> logger.info("THIS IS ATTACHMENT manager key: '{}' and value: '{}' of hmap", mapkey, mapvalue));

        List<SignalsIEntityDTO> signalsEntityDTOS = signalsEntityDbService.load(hmap);

        //signalsEntityDTOList.forEach((entity) -> logger.info("This is an element of list of entitiesDTO: '{}'\n", entity.dump()));

        //loading of attachment lists to add fields
        List<AttachmentEntity> attachments = new ArrayList<>();

        for (SignalsIEntityDTO entityDTO : signalsEntityDTOS) {
            logger.info("AM:-> Processing {} in order to extract the attachments {}\n", entityDTO.getType(), entityDTO.getId());
            if (attachmentRestService.checkIfEntityHasChildren(entityDTO)) {
                attachments.add(attachmentRestService.doGetAttachment(entityDTO.getId()).createEntity());
            } else {
                logger.info("AM:-> {} {} doesn't have children", entityDTO.getType(), entityDTO.getId());
            }
        }
        //Logger
        // attachments.forEach(attachment -> logger.info(attachment.toString()));

        //attachments.forEach(attachmentDbService::save);
    }


}
