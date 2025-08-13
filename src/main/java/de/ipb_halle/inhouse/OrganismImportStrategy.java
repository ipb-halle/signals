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

import de.ipb_halle.inhouse.imports.AdoCreator;
import de.ipb_halle.inhouse.imports.ExperimentCreator;
import de.ipb_halle.inhouse.imports.SampleCreator;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Imports group of experiments of type Organism
 * For each procedure (procedureId) an organismId will be searched upon table inhouse_correlation
 * and a new Sample with container will be created in a new experiment
 */
public class OrganismImportStrategy implements InhouseImportStrategy {

    private final Logger logger = LogManager.getLogger(OrganismImportStrategy.class);

    @Override
    public void importGroup(InhouseDB inhouseDB,
                            String threeLc,
                            List<InhouseExperiment> group,
                            Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache) throws Exception {

        logger.info("OrganismImportStrategy started for group: {}", threeLc);
        if (group == null || group.isEmpty()) {
            logger.warn("OrganismImportStrategy: empty list for threeLc={}", threeLc);
            return;
        }

        final int chunkSize = 10;
        int experimentCounter = 1;

        ExperimentCreator experimentCreator = new ExperimentCreator(inhouseDB);
        AdoCreator adoCreator = new AdoCreator(inhouseDB.getAdoManager());
        SampleCreator sampleCreator = new SampleCreator(inhouseDB, adoCreator);
        ErrorLogger errorLogger = new ErrorLogger("errorLog_organism_import.txt");


        for (int i = 0; i < group.size(); i += chunkSize) {
            int toIndex = Math.min(i + chunkSize, group.size());
            List<InhouseExperiment> chunk = group.subList(i, toIndex);

            InhouseExperiment main = chunk.get(0);
            String experimentName = threeLc + "-ORG-" + experimentCounter++;
            String eid = experimentCreator.createExperiment(experimentName, main);
            logger.info("OIS-> created experiment: {} (eid={})", experimentName, eid);

            for (InhouseExperiment exp : chunk) {
                Integer procId = exp.getProcId();
                if (procId == null || procId == 0) {
                    errorLogger.log("Missing procId for: " + exp);
                    continue;
                }
                List<InhouseCorrelation> correlations =
                        inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId);

                if (correlations == null || correlations.isEmpty()) {
                    logger.info("No correlations for procId={}", procId);
                    errorLogger.log("No correlations for procId=" + procId);
                    continue;
                }

                for (InhouseCorrelation correlation : correlations) {
                    Integer orgId = correlation.getOrganismId();
                    if (orgId == null) {
                        logger.debug("Skip correlation without organismId for procId={}", procId);
                        continue;
                    }
                    // setting description field value
                    String desc = String.format(
                            "OrgId: %s, Experiment: %s%s, Journal: %s",
                            orgId, threeLc, procId, exp.getJournal()
                    );
                    // create non-chemical sample with field values in sampleContainer -> sample table -> rest call POST
                    try {
                        // создаём sample без ChemDraw и без IPB-кода
                        sampleCreator.createOrganismSample(eid, desc);
                        logger.info("OIS-> organism sample created: procId={}, orgId={}", procId, orgId);

                        exp.setEid(eid);
                        exp.setImportSuccessful(true);
                        // если нужно — раскомментить для реального флага в Access
                        // inhouseDB.getInhouseDbService().markAsSuccessfullyImported(exp);
                    } catch (Exception ex) {
                        errorLogger.log("Failed to create organism sample for procId=" + procId +
                                ", orgId=" + orgId + " -> " + ex.getMessage());
                        logger.error("Failed to create organism sample", ex);
                    }
                }
            }
        }


    }

    private void createSampleForOrganism(InhouseDB inhouseDB, String ancestorId, String propertyValueDescription) {

        // Create a new Sample and assign the chemical sample template
        Sample sample = new Sample();
        sample.setTemplateId(CHEMICAL_SAMPLE_TEMPLATE_ID);

        SignalsEntity signals = new SignalsEntity();
        signals.setEid(ancestorId);
        sample.addAncestor(signals);
        sample.setAncestorId(ancestorId);

        // Set the propertyValue description
        SamplePropertyValue fvDescription = new SamplePropertyValue();
        fvDescription.setPropertyId(CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION);
        fvDescription.setPropertyValue(propertyValueDescription);

        // Create the sample via REST and store the returned Signals sample ID
        sample.addPropertyValue(fvDescription);
        String sampleId = inhouseDB.getSampleRestService().createNewSample(sample);
        sample.setId(sampleId);
        fvDescription.setSampleId(sampleId);

        // Prepare properties as key-value pairs for PATCH update
        HashMap<String, String> propertyKeyToValue = new HashMap<>();
        for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
            //                      id                                  // value
            propertyKeyToValue.put(samplePropertyValue.getPropertyId(), samplePropertyValue.getPropertyValue());
        }

        // Perform a PATCH request to update the properties of the newly created sample
        inhouseDB.getSampleRestService().updateSamplePropertyValues(propertyKeyToValue, sampleId);

        // no ipb_code for organisms
    }
}
