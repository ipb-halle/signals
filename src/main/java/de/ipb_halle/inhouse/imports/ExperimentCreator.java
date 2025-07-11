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

package de.ipb_halle.inhouse.imports;

import de.ipb_halle.inhouse.InhouseCompound;
import de.ipb_halle.inhouse.InhouseDB;
import de.ipb_halle.inhouse.InhouseExperiment;
import de.ipb_halle.inhouse.InhouseExperimentDTO;
import de.ipb_halle.signals.experiments.Experiment;

public class ExperimentCreator {

    private final InhouseDB inhouseDB;

    public ExperimentCreator(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    public String createExperiment(String experimentName, InhouseExperiment experiment) {
        // 1) Create Inhouse Experiment DTO
        InhouseExperimentDTO dto = new InhouseExperimentDTO(experiment);
        dto.setInhouseDB(inhouseDB);

        // 2) Create a Signals Experiment JavaObject
        Experiment signalsExp = dto.createExperiment(experimentName);

        // 3) Make a Rest Call to signals API in order to create an experiment entity in signals Notebook
        // with field values for an Experiment Template InhouseExperiment (Template ID = experiment:834e6aee-0d59-4732-89d7-925edca09844)
        signalsExp = inhouseDB.getExperimentRestService().createNewExperiment(signalsExp);

        // 4) Set Id to Inhose Experiment
        String eid = signalsExp.getId();
        experiment.setEid(eid);
        return eid;
    }

    public String createChemDraw(Integer molId, String experimentEid) {
        String fileName = "molId_" + molId;
        return inhouseDB.getExperimentRestService()
                .createNewChemicalDrawingAsExperimentChild(experimentEid, fileName, "");
    }

    public String loadIpbCodeByMolId(Integer molId) {
        InhouseCompound compound = inhouseDB.getInhouseDbService().loadCompoundByMolId(molId);
        return (compound != null && compound.getIpbCode() != null && !compound.getIpbCode().isBlank())
                ? compound.getIpbCode()
                : "";
    }
}
