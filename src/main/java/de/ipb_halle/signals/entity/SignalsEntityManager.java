/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ipb_halle.signals.UpdateConfig;
import de.ipb_halle.signals.rest.RestResultIterator;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Manager for signals entities (entities API endpoint)
 */

@Stateless
public class SignalsEntityManager {

    @Inject
    private SignalsEntityDbService dbService;

    @Inject
    private SignalsEntityRestService restService;

    private Logger logger = LogManager.getLogger(SignalsEntityManager.class);

    public SignalsEntityDTO getDbEntity(String id) {
        return dbService.loadById(id);
    }

    /*
     * produce a database dump of SignalsEntities
     * @param dateRange start and end datum of the database dump
     */
    public void listEntities(Date[] dateRange) {
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
        List<SignalsEntityDTO> results = dbService.load(cmap);
        for (SignalsEntityDTO dto : results) {
            System.out.println(dto.dump());
        }
    }

    /*
     * fetch entities via REST from Signals Notebook
     * @param dateRange array with start and end points of data to be fetched
     * @param includeTypes comma separated list of entity types (experiment, notebook, asset, etc.) to be fetched
     */
    public void fetchSnbEntities(Date[] dateRange, String includeTypes, UpdateConfig config) {
        Map<String, Object> cmap = new HashMap<>();
        if ((includeTypes != null) && (! includeTypes.isEmpty())) {
            cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, includeTypes);
        }
        if (dateRange != null) {
            cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        RestResultIterator<SignalsEntityDTO> iter =  restService.doGetEntities(cmap);
        while (iter.hasNext()) {
            SignalsEntityDTO dto = iter.next();
            logger.debug(dto.dump());
            if (config.updateDb) {
                dbService.save(dto);
            }
        }
    }
}

