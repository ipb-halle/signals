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

/**
 * Coordinates the import of {@link InhouseExperiment} records into the Signals platform
 * using different {@link InhouseImportStrategy} implementations depending on experiment type.
 *
 * <p>This class:
 * <ul>
 *   <li>Holds a map of supported import strategies (e.g., STRUCTURE, ORGANISM)</li>
 *   <li>Groups experiments by their three-letter code (ThreeLC) before import</li>
 *   <li>Delegates the actual import work to the correct strategy</li>
 * </ul>
 *
 * <p>The manager does not handle filtering — it assumes the experiments are already separated
 * into categories (STRUCTURE, ORGANISM, etc.) before calling {@link #importAll(Map, ChemDrawCacheService)}.
 *
 * <p>Dependencies:
 * <ul>
 *   <li>{@link InhouseDB} – Database connection and services</li>
 *   <li>{@link ChemDrawCacheService} – Cached ChemDraw structure data for STRUCTURE imports</li>
 *   <li>{@link ExperimentGrouper} – Groups experiments by ThreeLC</li>
 *   <li>{@link ErrorLogger} – Logs issues encountered during grouping</li>
 * </ul>
 */
public class InhouseImportManager {

    private final InhouseDB inhouseDB;
    private final Map<InhouseImportType, InhouseImportStrategy> strategies;

    /**
     * Creates a new import manager bound to a specific Inhouse database instance.
     *
     * <p>Initializes the strategy map with:
     * <ul>
     *   <li>{@link InhouseImportType#STRUCTURE} → {@link StructureImportStrategy}</li>
     *   <li>{@link InhouseImportType#ORGANISM} → {@link ExtractImportStrategy}</li>
     * </ul>
     *
     * @param inhouseDB The Inhouse database service used during import
     */
    public InhouseImportManager(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        this.strategies = Map.of(
                 InhouseImportType.STRUCTURE, new StructureImportStrategy()
              //  InhouseImportType.ORGANISM, new ExtractImportStrategy()
        );
    }

    /**
     * Imports all given experiments by delegating them to the appropriate
     * {@link InhouseImportStrategy} based on {@link InhouseImportType}.
     *
     * <p>Steps:
     * <ol>
     *   <li>Iterate through all possible {@link InhouseImportType} values</li>
     *   <li>Skip types that have no experiments or no matching strategy</li>
     *   <li>Group experiments by their ThreeLC code using {@link ExperimentGrouper}</li>
     *   <li>Log grouping results to a file named <code>grouping_{type}.log</code></li>
     *   <li>If the type is STRUCTURE, pass the full ChemDraw cache to the strategy;
     *       otherwise, pass an empty map</li>
     *   <li>Call {@link InhouseImportStrategy#importGroup(InhouseDB, String, List, Map)}
     *       for each group</li>
     * </ol>
     *
     * @param experimentsByType A map of experiments already separated by {@link InhouseImportType}
     * @param chemDrawCache     Cache of ChemDraw data (used only for STRUCTURE imports)
     * @throws Exception If grouping or importing fails
     */
    public void importAll(Map<InhouseImportType, List<InhouseExperiment>> experimentsByType, ChemDrawCacheService chemDrawCache) throws Exception {

        // extraction of List<InhouseExperinet> from Map according to InhouseImpotType (STRUCTURE, EXTRACT, UNKNOWN)
        for (InhouseImportType type : InhouseImportType.values()) {
            List<InhouseExperiment> list = experimentsByType.getOrDefault(type, List.of());
            if (list.isEmpty() || !strategies.containsKey(type)) continue;


            // Grouping of  InhouseExperiments according to threeLc in to chunks with size of 10 experiments
            ExperimentGrouper grouper = new ExperimentGrouper();
            ErrorLogger logger = new ErrorLogger("grouping_" + type.name().toLowerCase() + ".log");
            //   threeLc, grouped experiments toDO: weiter schreiben ab dieser Stelle
            Map<String, List<InhouseExperiment>> grouped = grouper.groupByThreeLC(list, ImportMode.TESTING, logger);


            for (Map.Entry<String, List<InhouseExperiment>> entry : grouped.entrySet()) {

                //getting ChemDrawData(molId, cdxml) from cache service chemDrawCache if type is Structure
                Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache = (type == InhouseImportType.STRUCTURE) ? chemDrawCache.getCdxmlCache() : Map.of();

                //starting of import strategy up to InhouseImportType
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
