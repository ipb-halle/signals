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

import de.ipb_halle.inhouse.ErrorLogger;
import de.ipb_halle.inhouse.ImportMode;
import de.ipb_halle.inhouse.InhouseExperiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentGrouper {

    public Map<String, List<InhouseExperiment>> groupByThreeLC(List<InhouseExperiment> experiments, ImportMode mode, ErrorLogger errorLogger) throws Exception {
        Map<String, List<InhouseExperiment>> grouped = new HashMap<>();
        String firstKey = null;

        for (InhouseExperiment exp : experiments) {
            String key = exp.getThreelc();
            if (key == null) {
                errorLogger.log("Missing 3LC: " + exp);
                continue;
            }

            switch (mode) {
                case TESTING:
                    if (firstKey == null) {
                        firstKey = key;
                        grouped.put(firstKey, new ArrayList<>());
                    }
                    if (key.equals(firstKey)) {
                        grouped.get(firstKey).add(exp);
                    } else {
                        return grouped;
                    }
                    break;

                case PRODUCTION:
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(exp);
                    break;
            }
        }
        return grouped;
    }
}
