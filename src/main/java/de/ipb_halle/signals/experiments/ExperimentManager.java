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

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.sample.SampleEntity;
import de.ipb_halle.signals.sample.SampleManager;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ExperimentManager {
    @Inject
    private ExperimentDbService experimentDbService;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private ExperimentRestService experimentRestService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private ExperimentProcessorBean experimentProcessorBean;

    private final Logger logger = LogManager.getLogger(ExperimentManager.class);

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void importExperiment(RuntimeConfig runtimeConfig, String id) {
        logger.info("ICH BIN IN IMPORT EXPERIMENT");

        // 1) loads experiments from local DB to be imported into Signals
        ExperimentEntity experimentEntity = experimentDbService.loadExperimentById(id);
        Experiment experiment = new Experiment(experimentEntity, dynEnumManager);

        // 2) loads Experiment Properties
        experimentDbService.loadExperimentPropertyValuesWithProperties(experiment);
        logger.info("THE RPOERTIES WERE LOADED");

        // 3) create new experiment
        experimentRestService.createNewExperiment(experiment);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void manageExperiments(Date[] dateRange) {
        logger.debug("Experiment Manager:-> START MANAGE EXPERIMENTS");

        // 1) Query parameters for load
        EntityType entityTypes[] = {EntityType.valueOf(ExperimentEntity.ENTITY_TYPE_EXPERIMENT)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        // 2) Load all experiments from Db
        List<SignalsEntityDTO> experiments = signalsEntityDbService.loadSE(cmap);

        // 3) Process experiments
        for (SignalsEntityDTO dto : experiments) {
            processExperiment(dto.getId());
        }

    }

    private void processExperiment(String experimentId) {
        experimentProcessorBean.processSingleExperiment(experimentId);
    }

}
