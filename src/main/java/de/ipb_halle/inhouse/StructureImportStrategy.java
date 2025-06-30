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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StructureImportStrategy implements InhouseImportStrategy {

    @Override
    public void importGroup(
            InhouseDB inhouseDB,
            String threeLc,
            List<InhouseExperiment> group,
            Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache) throws Exception {

        //Set an error logger for writing an errors in the file
        ErrorLogger errorLogger = new ErrorLogger("errorLog_for_structureImportStartegy.txt");

        // set the experiment for 10 samples
        Experiments helper = new Experiments(inhouseDB);
        int chunkSize = 10;
        int experimentCounter = 1;

        for (int i = 0; i < group.size(); i += chunkSize) {
            int toIndex = Math.min(i + chunkSize, group.size());
            List<InhouseExperiment> chunk = group.subList(i, toIndex);

            InhouseExperiment main = chunk.get(0);
            String experimentName = threeLc + "-" + experimentCounter++;
            String eid = helper.createExperimentUpon3LC(experimentName, main);

            for (InhouseExperiment exp : chunk) {
                Integer procId = exp.getProcId();
                if (procId == 0) {
                    helper.getLogger().error("No procedure ID for experiment: {}", exp);
                    continue;
                }

                List<Optional<Experiments.ChemDrawData>> optionals = cdxmlCache.get(procId);
                if (optionals == null || optionals.isEmpty()) {
                    errorLogger.log("Empty ChemDraw list for procId=" + procId);
                    continue;
                }

                for (Optional<Experiments.ChemDrawData> optional : optionals) {
                    if (optional.isEmpty()) {
                        errorLogger.log("Missing ChemDrawData (should not happen) for procedureId =" + procId);
                        continue;
                    }

                    Experiments.ChemDrawData data = optional.get();
                    String chemDrawId = helper.createChemDrawForInhouseExperiment(data.molId(), eid);

                    if (!data.fieldValueCdxml().isEmpty()) {
                        inhouseDB.getExperimentRestService().addReactionToExperiment(
                                // POSITIONS-> = reactants|products|reagents|grid
                                chemDrawId, "products", data.fieldValueCdxml());
                    }

                    String desc = String.format("MolId: %s, Experiment: %s%s, Journal: %s",
                            data.molId(), threeLc, procId, exp.getJournal());
                    helper.createSampleForChemicalDrawing(chemDrawId, "1", eid, desc);

                    exp.setEid(eid);
                    exp.setImportSuccessful(true);
                    inhouseDB.getInhouseDbService().markAsSuccessfullyImported(exp);
                }
            }
        }
    }

}
