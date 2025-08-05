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

import de.ipb_halle.inhouse.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InhouseImportManager {

    private final InhouseDB inhouseDB;
    private final Map<InhouseImportType, InhouseImportStrategy> strategies;


    public InhouseImportManager(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        this.strategies = Map.of(
                InhouseImportType.STRUCTURE, new StructureImportStrategy()
                // TODO: add ORGANISM, EXTRACT later
        );
    }

    public void importAll(Map<InhouseImportType, List<InhouseExperiment>> experimentsByType,
                          ChemDrawCacheService chemDrawCache) throws Exception {

        for (InhouseImportType type : InhouseImportType.values()) {
            List<InhouseExperiment> list = experimentsByType.getOrDefault(type, List.of());
            if (list.isEmpty() || !strategies.containsKey(type)) continue;


            ExperimentGrouper grouper = new ExperimentGrouper();
            ErrorLogger logger = new ErrorLogger("grouping_" + type.name().toLowerCase() + ".log");
            Map<String, List<InhouseExperiment>> grouped = grouper.groupByThreeLC(list, ImportMode.TESTING, logger);

            for (Map.Entry<String, List<InhouseExperiment>> entry : grouped.entrySet()) {

                Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache =
                        (type == InhouseImportType.STRUCTURE) ? chemDrawCache.getCdxmlCache() : Map.of();

                strategies.get(type).importGroup(
                        inhouseDB,
                        entry.getKey(),
                        entry.getValue(),
                        cdxmlCache
                );
            }
        }
    }
}
