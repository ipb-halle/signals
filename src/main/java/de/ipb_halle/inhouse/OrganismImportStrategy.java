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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrganismImportStrategy extends AbstractBatchImportStrategy {

    public static final String TEMPLATE_ID_EXTRACT_SAMPLE = "samples.templateIdExtractSample";
    private final Logger logger = LogManager.getLogger(OrganismImportStrategy.class);

    @Override
    protected String experimentNamePrefix() {
        return "ORG";
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

        List<InhouseCorrelation> correlations =
                inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId);

        if (correlations == null || correlations.isEmpty()) {
            errorLogger.log("No correlations for procId=" + procId);
            return;
        }

        for (InhouseCorrelation c : correlations) {
            Integer orgId = c.getOrganismId();
            if (orgId == null) continue;

            // In table inhouseCorrelations we need ref_org_proc (id), context 'orgproc', to load inhouseExtract
            // for further creation of signals 'extract sample'. It should reference organism, ipb_code if present and storage place
            Integer corrOrgProcId = c.getCorrId();
            if (corrOrgProcId == null) {
                logger.error("No correlation org_proc id in DB for correlation id = {}\n", c.getId());
                continue;
            }
            List<InhouseExtract> extractList = inhouseDB.getInhouseDbService().loadExtractByCorrOrgProcId(corrOrgProcId);
            logger.info("INHOUSE EXTRACT LIST: {}\n", Arrays.toString(extractList.toArray()));
            logger.info("INHOUSE EXTRACT ARRAY LIST SIZE = {}\n", extractList.size());
            InhouseExtract extract;
            if (extractList.isEmpty()) {
                logger.info("No Extracts for this organism ={}\n ", orgId);
            } else {
                extract = extractList.get(0);
                String ipbCodeRaw = extract.getIpbCode();
                String ipbCodeNorm = IpbCodeNormalizer.normalize(ipbCodeRaw).orElse(null);
                String desc = String.format("OrgId: %s, Experiment: %s%s, Journal: %s",
                        orgId, exp.getThreelc(), procId, exp.getJournal());

                sampleCreator.createExtractSample(TEMPLATE_ID_EXTRACT_SAMPLE, eid, desc, ipbCodeNorm);
            }
        }
    }
}
