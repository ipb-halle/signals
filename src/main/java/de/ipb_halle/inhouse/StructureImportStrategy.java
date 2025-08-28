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

import de.ipb_halle.inhouse.imports.ExperimentCreator;
import de.ipb_halle.inhouse.imports.SampleCreator;
import de.ipb_halle.inhouse.util.IpbCodeNormalizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StructureImportStrategy extends AbstractBatchImportStrategy {
    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "samples.templateIdChemicalSample";
    private final Logger logger = LogManager.getLogger(StructureImportStrategy.class);

    @Override
    protected String experimentNamePrefix() {
        return "STRUCT";
    }

    @Override
    protected void processItem(InhouseDB inhouseDB,
                               ExperimentCreator experimentCreator,
                               SampleCreator sampleCreator,
                               InhouseExperiment exp,
                               String eid,
                               Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCahce,
                               ErrorLogger errorLogger) throws Exception {

        Integer procId = exp.getProcId();
        if (procId == null || procId == 0) {
            errorLogger.log("Missing procId for: " + exp);
            return;
        }

        List<Optional<Experiments.ChemDrawData>> structures = cdxmlCahce.get(procId);
        if (structures == null || structures.isEmpty()) {
            errorLogger.log("No ChenDraw for procId=" + procId);
            return;
        }

        for (Optional<Experiments.ChemDrawData> cdxmlOpt : structures) {
            if (cdxmlOpt.isEmpty()) {
                logger.info("Empty ChemDrawData for procId={}", procId);
                errorLogger.log("Empty ChemDrawData for procId=" + procId);
                continue;
            }

            Experiments.ChemDrawData data = cdxmlOpt.get();
            String chemDrawId = experimentCreator.createChemDraw(data.molId(), eid);

            if (!data.fieldValueCdxml().isEmpty()) {
                inhouseDB.getExperimentRestService()
                        .addReactionToExperiment(chemDrawId, "products", data.fieldValueCdxml());
            }

            String ipbCodeRaw = experimentCreator.loadIpbCodeByMolId(data.molId());
            String ipbCodeNorm = IpbCodeNormalizer.normalize(ipbCodeRaw).orElse(null);

            String desc = String.format("MolId: %s, Experiment: %s%s, Journal: %s",
                    data.molId(), exp.getThreelc(), procId, exp.getJournal());

            sampleCreator.createChemicalSample(CHEMICAL_SAMPLE_TEMPLATE_ID, chemDrawId, "1", eid, desc, ipbCodeNorm, procId);
        }
    }
}
