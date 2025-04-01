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

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.FieldDbService;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleManager {

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    private FieldDbService fieldDbService;

    @Inject
    private SampleDbService sampleDbService;

    @Inject
    private SampleProcessorBean sampleProcessorBean;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private SampleRestService sampleRestService;


    private final Logger logger = LogManager.getLogger(SampleManager.class);

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void importSample(RuntimeConfig runtimeConfig, String id) {
        //loads samples from local DB to be imported into Signals
        SampleEntity sampleEntity = sampleDbService.loadSampleEntityById(id);
        Sample sample = new Sample(sampleEntity, dynEnumManager);
        sampleDbService.loadSamplePropertyValues(sample);
        sampleDbService.loadSampleProperties(sample);

        sampleRestService.createNewSample(sample);
    }


    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void manageSamples(Date[] dateRange) {
        logger.debug("Sample Manager:-> START MANAGE SAMPLES");

        // 1) Query parameters for load
        EntityType entityTypes[] = {EntityType.valueOf(SampleEntity.ENTITY_TYPE_SAMPLE)};
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, entityTypes);

        // 2) Load all samples from Db
        List<SignalsEntityDTO> samples = signalsEntityDbService.loadSE(cmap);

        // 3) Process samples
        for (SignalsEntityDTO dto : samples) {
            processSample(dto.getId());
        }

    }

    private void processSample(String sampleId) {
        sampleProcessorBean.processSingleSample(sampleId);
    }
}
