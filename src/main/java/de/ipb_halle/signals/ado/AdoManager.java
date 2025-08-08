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

import de.ipb_halle.inhouse.util.IpbCodeNormalizer;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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

    public Optional<Ado> findOrCreateAdoByIpbCode(String ipbCodeRaw, String templateId) {
        Optional<String> normOpt = IpbCodeNormalizer.normalize(ipbCodeRaw);
        if (normOpt.isEmpty()) {
            logger.info("ADO lookup skipped: missing/invalid IPB code: {}", ipbCodeRaw);
            return Optional.empty();
        }
        String ipbCode = normOpt.get();

        // 1) load upon templateId all existing Ados
        List<Ado> existing = adoDbService.loadByTemplateIdSorted(templateId);

        // 2) search in List existing if searched ado exists
        Optional<Ado> found = existing.stream()
                .filter(a -> ipbCode.equalsIgnoreCase(a.getIpbCode()))
                .findFirst();
        if (found.isPresent()) return found;

        // 3) if not calculating range for ados creation till needed IPB code number
        int targetNumber = IpbCodeNormalizer.extractNumeric(ipbCode).orElse(0);
        int highestExisting = existing.stream()
                .map(Ado::getIpbCode)
                .filter(Objects::nonNull)
                .map(IpbCodeNormalizer::extractNumeric)
                .flatMap(Optional::stream)
                .mapToInt(Integer::intValue)
                .max().orElse(0);

        if (targetNumber <= highestExisting) {
            logger.warn("ADO '{}' not found, the ipb_codes in db is not consistent", ipbCode);
            return Optional.empty();
        }

        int maxNumber = adoDbService.findMaxIpbCode();
        int last = Math.min(targetNumber, maxNumber);
        int first = highestExisting + 1;
        if(last < first) {
            logger.info("Nothing to generate: first={}, last={}", first, last);
            return Optional.empty();
        }

        // 4) generating ados in set range, names == ipb codes
        List<Ado> newAdos = adoRestService.createAdosRange(first, last, templateId);
        adoDbService.saveAll(newAdos);

        return adoDbService.loadByTemplateIdSorted(templateId).stream()
                .filter(a -> ipbCode.equalsIgnoreCase(a.getIpbCode()))
                .findFirst();
    }

    /**
     * Creates all ADOs up to the specified max IPB code.
     *
     * @return List of all existing + newly created ADOs
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Ado> createAllMissingAdosTillMaxIpbCode(String templateId) {
        int maxIpbCode = adoDbService.findMaxIpbCode();
        logger.info("AdoManager: -> Max IPB code: {}", maxIpbCode);

        List<Ado> existingAdos = adoDbService.loadAll();
        logger.info("LOADED ADOS:-> {}\n", Arrays.toString(existingAdos.toArray()));
        int toCreate = maxIpbCode - existingAdos.size();

        if (toCreate <= 0) {
            logger.info("All ADOs already present ({} of {}).", existingAdos.size(), maxIpbCode);
            return existingAdos;
        }

        logger.info("Creating {} missing ADOs...", toCreate);
        List<Ado> newAdos = adoRestService.createAdosRange(toCreate, maxIpbCode, templateId);
        adoDbService.saveAll(newAdos);
        return adoDbService.loadAll();
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
