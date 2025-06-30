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

package de.ipb_halle.inhouse;

import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Imports group of experiments of type Organism
 * For each procedure (procedureId) an organismId will be searched upon table inhouse_correlation
 * and a new Sample with container will be created in a new experiment
 */
public class OrganismImportStrategy implements InhouseImportStrategy {
    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "sample:0174e78c-0b95-49f9-8a57-39061bbc0050";
    public static final String CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION = "2";


    private final Logger logger = LogManager.getLogger(OrganismImportStrategy.class);

    @Override
    public void importGroup(InhouseDB inhouseDB, String threeLc, List<InhouseExperiment> experimentsBy3lc, Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache) throws Exception {
        importGroup(inhouseDB, threeLc, experimentsBy3lc);
    }

    @Override
    public void importGroup(
            InhouseDB inhouseDB,
            String threeLc,
            List<InhouseExperiment> experimentsBy3lc) throws Exception {
        if (experimentsBy3lc.isEmpty()) {
            logger.warn("Empty experiment list grouped by threeLc {}\n", threeLc);
            return;
        }
        Experiments helper = new Experiments(inhouseDB);

        int chunkSize = 10;
        int experimentCounter = 1;

        for (int i = 0; i < experimentsBy3lc.size(); i += chunkSize) {
            int toIndex = Math.min(i + chunkSize, experimentsBy3lc.size());
            List<InhouseExperiment> chunk = experimentsBy3lc.subList(i, toIndex);

            InhouseExperiment main = chunk.get(0);
            String experimentName = threeLc + "-ORG-" + experimentCounter++;
            String eid = helper.createExperimentUpon3LC(experimentName, main);

            for (InhouseExperiment exp : chunk) {
                int procId = exp.getProcId();
                List<InhouseCorrelation> correlations = inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId);

                for (InhouseCorrelation correlation : correlations) {
                    Integer orgId = correlation.getOrganismId();
                    if (orgId == null) continue;

                    String desc = String.format("OrgId: %s, Experiment: %s%s, Journal: %s",
                            orgId, threeLc, procId, exp.getJournal());

                    createSampleForOrganism(inhouseDB, eid, orgId, desc);

                    exp.setEid(eid);
                    exp.setImportSuccessful(true);
                    inhouseDB.getInhouseDbService().markAsSuccessfullyImported(exp);
                }
            }
        }


    }

    private void createSampleForOrganism(InhouseDB inhouseDB, String experimentId, Integer organismId, String description) {

        Sample sample = new Sample();
        sample.setTemplateId(CHEMICAL_SAMPLE_TEMPLATE_ID);

        SignalsEntity signals = new SignalsEntity();
        signals.setEid(experimentId);
        sample.addAncestor(signals);
        sample.setAncestorId(experimentId);

        SamplePropertyValue propertyDesc = new SamplePropertyValue();
        propertyDesc.setPropertyId(CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION);
        propertyDesc.setPropertyValue(description);

        sample.addPropertyValue(propertyDesc);

    }


}
