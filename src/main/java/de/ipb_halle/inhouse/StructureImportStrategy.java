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

public class StructureImportStrategy implements InhouseImportStrategy {

    public static final String SIGNALS_ENTITY_CUSTOM_OBJECT_IPB_CODE = "ado-1:4b98a234-e223-4c67-b6b1-f9df67958460";
    private final Logger logger = LogManager.getLogger(StructureImportStrategy.class);

    @Override
    public void importGroup(InhouseDB inhouseDB, String threeLc, List<InhouseExperiment> group, Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache) throws Exception {
        logger.info("StructureImportStrategy started for group: {}", threeLc);
        if (group.isEmpty()) {
            logger.warn("StructureImportStrategy:-> Empty experiment list grouped by threeLc {}\n", threeLc);
            return;
        }

        // set the experiment for 10 samples
        ExperimentCreator experimentCreator = new ExperimentCreator(inhouseDB);
        AdoCreator adoCreator = new AdoCreator(inhouseDB.getAdoManager());
        SampleCreator sampleCreator = new SampleCreator(inhouseDB, adoCreator);
        ErrorLogger errorLogger = new ErrorLogger("errorLog_structure_import.txt");

        int chunkSize = 10;
        int experimentCounter = 1;

        for (int i = 0; i < group.size(); i += chunkSize) {
            int toIndex = Math.min(i + chunkSize, group.size());
            List<InhouseExperiment> chunk = group.subList(i, toIndex);

            InhouseExperiment main = chunk.get(0);
            String expName = threeLc + "-" + experimentCounter++ + System.currentTimeMillis();
            String eid = experimentCreator.createExperiment(expName, main);

            for (InhouseExperiment exp : chunk) {
                Integer procId = exp.getProcId();
                if (procId == 0) {
                    errorLogger.log("Missing procId for: " + exp);
                    continue;
                }

                List<Optional<Experiments.ChemDrawData>> structures = cdxmlCache.get(procId);
                if (structures == null || structures.isEmpty()) {
                    errorLogger.log("No ChemDraw for procId=" + procId);
                    continue;
                }

                for (Optional<Experiments.ChemDrawData> cdxmlOpt : structures) {
                    if (cdxmlOpt.isEmpty()) {
                        logger.info("Empty ChemDrawData for procId= {}", procId);
                        errorLogger.log("Empty ChemDrawData for procId=" + procId);
                        continue;
                    }
                    // Create ChemDraw parent entity -> empty
                    Experiments.ChemDrawData data = cdxmlOpt.get();
                    logger.info("SIS-> eid of created experiment Eid = {}, MolId =  {}\n", eid, data.molId());
                    String chemDrawId = experimentCreator.createChemDraw(data.molId(), eid);

                    logger.info("SIS-> CHEMDRAW Eid = {}\n", chemDrawId);

                    // Add a cdxml to empty chemDrawing entity as a product
                    if (!data.fieldValueCdxml().isEmpty()) {
                        inhouseDB.getExperimentRestService().addReactionToExperiment(
                                // POSITIONS-> = reactants|products|reagents|grid
                                chemDrawId, "products", data.fieldValueCdxml());
                        logger.info("SIS-> Reaction added to chemDraw");
                    }

                    // setting description field value
                    String desc = String.format("MolId: %s, Experiment: %s%s, Journal: %s", data.molId(), threeLc, procId, exp.getJournal());

                    // Load IpbCode
                    String ipbCode = experimentCreator.loadIpbCodeByMolId(data.molId());
                    if (ipbCode == null || ipbCode.trim().isEmpty()) {
                        ipbCode = "IPB code not assigned";
                    }
                    logger.info("SIS-> starting creating sample");
                    sampleCreator.createSample(chemDrawId, "1", eid, desc, ipbCode);

                    exp.setEid(eid);
                    exp.setImportSuccessful(true);
                    inhouseDB.getInhouseDbService().markAsSuccessfullyImported(exp);
                }
            }
        }
    }
}