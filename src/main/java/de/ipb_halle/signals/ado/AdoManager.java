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

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.experiments.ExperimentEntity;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@LocalBean
public class AdoManager {

    @Inject
    SignalsEntityDbService signalsEntityDbService;

    @Inject
    AdoProcessorBean adoProcessorBean;

    @Inject
    AdoRestService adoRestService;

    @Inject
    AdoDbService adoDbService;

    private final Logger logger = LogManager.getLogger(AdoManager.class);

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Ado> ensureAllIpbAdosExist() {
        int maxIpbCode = adoDbService.findMaxIpbCode();
        logger.info("Max IPB code: {}", maxIpbCode);

        List<Ado> existingAdos = adoDbService.loadAll();
        int missingCount = maxIpbCode - existingAdos.size();

        if (missingCount <= 0) {
            logger.info("All ADOs already present ({} of {}).", existingAdos.size(), maxIpbCode);
            return existingAdos;
        }

        logger.info("Creating {} missing ADOs...", missingCount);
        List<Ado> newAdos = adoRestService.createAllIpbCustomObjects(maxIpbCode);
        adoDbService.saveAll(newAdos);
        return newAdos;
    }


    public Ado findIpbCodeAdoForInhouseExperiment(String ipbCode) {
        List<Ado> ados = adoDbService.loadAll();

        if (ados == null || ados.isEmpty()) {
            ados = ensureAllIpbAdosExist();
        }

        return ados.stream()
                .filter(a -> ipbCode.equalsIgnoreCase(a.getIpbCode()))
                .findFirst()
                .orElseGet(() -> {
                    logger.warn("No ADO found for IPB code '{}'", ipbCode);
                    return null;
                });
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void manageAdos(Date[] dateRange) {
        logger.info("Handling ADO synchronization from {} to {}", dateRange[0], dateRange[1]);

        EntityType entityTypes[] = {EntityType.valueOf(AdoEntity.ENTITY_TYPE_ADO)};
        Map<String, Object> criteriaMap = new HashMap<>();

        criteriaMap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            criteriaMap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        criteriaMap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        List<SignalsEntityDTO> signalsEntityDTOS = signalsEntityDbService.loadSE(criteriaMap);

        for (SignalsEntityDTO dto : signalsEntityDTOS) {
            String eid = dto.getEid();
            processAdo(eid);
        }

    }

    private void processAdo(String eid) {
        adoProcessorBean.processSingleAdo(eid);
    }

    public void importAdo(String id) {
// TODO: implementieren
    }
}
