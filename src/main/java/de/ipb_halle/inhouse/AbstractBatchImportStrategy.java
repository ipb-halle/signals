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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractBatchImportStrategy implements InhouseImportStrategy {

    private final Logger logger = LogManager.getLogger(getClass());

    protected int chunkSize(int size) {
        return size;
    }

    protected abstract String experimentNamePrefix();

    protected abstract void processItem(InhouseDB inhouseDB,
                                        ExperimentCreator experimentCreator,
                                        SampleCreator sampleCreator,
                                        InhouseExperiment exp,
                                        String eid,
                                        Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCahce,
                                        ErrorLogger errorLogger) throws Exception;

    @Override
    public final void importGroup(InhouseDB inhouseDB,
                                  String threeLc,
                                  List<InhouseExperiment> group,
                                  Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache) throws Exception {

        logger.info("{} started for group: {}", getClass().getSimpleName(), threeLc);
        if (group == null || group.isEmpty()) {
            logger.warn("{}: empty list for threeLc={}", getClass().getSimpleName(), threeLc);
            return;
        }

        ExperimentCreator experimentCreator = new ExperimentCreator(inhouseDB);
        SampleCreator sampleCreator = new SampleCreator(inhouseDB, inhouseDB.getAdoManager() != null
                ? new AdoCreator(inhouseDB.getAdoManager())
                : null);
        ErrorLogger errorLogger = new ErrorLogger("errorLog_" + experimentNamePrefix().toLowerCase() + "_import.txt");

        int experimentCounter = 1;
        for (int i = 0; i < group.size(); i += chunkSize(10)) {
            int toIndex = Math.min(i + chunkSize(10), group.size());
            List<InhouseExperiment> chunk = group.subList(i, toIndex);

            InhouseExperiment main = chunk.get(0);
            String expName = threeLc + "-" + experimentNamePrefix() + "-" + experimentCounter++;
            String eid = experimentCreator.createExperiment(expName, main);
            logger.info("Created experiment {} (eid={})", expName, eid);

            for (InhouseExperiment item : chunk) {
                try {
                    processItem(inhouseDB, experimentCreator, sampleCreator, item, eid, cdxmlCache, errorLogger);
                    item.setEid(eid);
                    item.setImportSuccessful(true);
                    //inhouseDB.getInhouseDbService().markAsSuccessfullyImported(item);
                } catch (Exception ex) {
                    errorLogger.log("Failed to process item: " + item + " ->" + ex.getMessage());
                    logger.error("Failed to process item", ex);
                }
            }
        }
    }
}
