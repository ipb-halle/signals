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

import de.ipb_halle.inhouse.Experiments;
import de.ipb_halle.inhouse.InhouseExperiment;
import de.ipb_halle.inhouse.InhouseExperimentDTO;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AdoManager {

    @Inject
    AdoRestService adoRestService;

    @Inject
    AdoDbService adoDbService;

    private final Logger logger = LogManager.getLogger(AdoManager.class);

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Ado> createAllIpbAdoObjects(List<InhouseExperiment> experimentsWithIpbCode, int maxIpbCode) {
        logger.info("creating all ADOs for ipb codes amount = {}\n",experimentsWithIpbCode.size());
        List<Ado> ipbAdoObjects = adoRestService.createAllIpbCustomObjects();
        adoDbService.saveAll(ipbAdoObjects);
        return ipbAdoObjects;
    }


    public Ado findIpbCodeAdoForInhouseExperiment(String ancestorId,String descriptionOfAncestorAsNameForAdo, String ipbCode) {
        List<Ado> ados = adoDbService.loadAll();

        Optional<Ado> optionalAdo = ados.stream()
                .filter(a -> (a.getIpbCode() != null && a.getIpbCode().equalsIgnoreCase(ipbCode)))
                .findFirst();

        if (optionalAdo.isEmpty()) {
            logger.warn("No ADO found for IPB code '{}'\n", ipbCode);
            return null;
        }

        Ado ado = optionalAdo.get();
        return ado;
    }

    public void manageAdos(Date[] dateRange) {
        logger.info("Handlich ADO sychronization from {} to {}", dateRange[0], dateRange[1]);
    }

    public void importAdo(String id) {

    }
}
