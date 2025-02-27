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
package de.ipb_halle.signals.entity;

import java.util.*;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.rest.RestResultIterator;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manager for signals entities (entities API endpoint)
 */

@Stateless
public class SignalsEntityManager {

    @Inject
    private SignalsEntityDbService dbService;

    @Inject
    private SignalsEntityRestService restService;

    @Inject
    private SEProcessorBean signalsEntitiesProcessorBean;

    private Logger logger = LoggerFactory.getLogger(SignalsEntityManager.class);

    /**
     * Lists entities from the database that fall within a certain date range
     * and match the specified included entity types. The results are printed
     * to the console (stdout).
     *
     * @param dateRange     an array with two {@code Date} objects: [0] for start, [1] for end
     * @param includedTypes the array of entity types to be included in the database query
     */
    public void listEntities(Date[] dateRange, EntityType[] includedTypes) {
        System.out.print("""
                ********************************************
                *
                * Signals Entities
                *
                ********************************************
                """);

        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, includedTypes);
        List<SignalsEntityDTO> results = dbService.load(cmap);
        for (SignalsEntityDTO dto : results) {
            System.out.println(dto.dump());
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void manageSignalsEntities(Date[] dateRange, EntityType[] includeTypes, RuntimeConfig config) {
        logger.info("SEM:-> Starting manageSignalsEntities");

        // 1) Build query parameters for local database load
        Map<String, Object> cmap = buildQueryParameters(dateRange, includeTypes);

        // 2) Fetch snbEntities including children from remote
        fetchSnbEntities(cmap, config);
    }

    private static Map<String, Object> buildQueryParameters(Date[] dateRange, EntityType[] includeTypes) {
        Map<String, Object> cmap = new HashMap<>();
        if (dateRange != null && dateRange.length > 0) {
            cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
            if (dateRange.length > 1) {
                cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
            }
        }
        if (includeTypes != null && includeTypes.length > 0) {
            cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, includeTypes);
        }
        return cmap;
    }

    public void fetchSnbEntities(Map<String, Object> cmap, RuntimeConfig config) {
        RestResultIterator<SignalsEntityDTO> iter = restService.doGetEntities(cmap);
        while (iter.hasNext()) {
            // switch bean context to obtain a transaction boundary
            signalsEntitiesProcessorBean.processEntity(config, iter.next());
        }
    }
}
