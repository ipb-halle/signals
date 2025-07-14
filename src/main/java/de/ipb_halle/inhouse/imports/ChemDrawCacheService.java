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
import de.ipb_halle.inhouse.Experiments;
import de.ipb_halle.inhouse.InhouseCorrelation;
import de.ipb_halle.inhouse.InhouseDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ChemDrawCacheService {

    private final InhouseDB inhouseDB;
    private final Logger logger = LogManager.getLogger(ChemDrawCacheService.class);

    private final Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache = new HashMap<>();
    private final Map<Integer, List<InhouseCorrelation>> correlationCache = new HashMap<>();

    public ChemDrawCacheService(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    public List<Optional<Experiments.ChemDrawData>> getChemDrawData(int procedureId) {
        return cdxmlCache.computeIfAbsent(procedureId, this::loadChemDrawData);
    }

    private List<Optional<Experiments.ChemDrawData>> loadChemDrawData(int procId) {
        List<Optional<Experiments.ChemDrawData>> result = new ArrayList<>();
        List<InhouseCorrelation> correlations = getCorrelations(procId);

        for (InhouseCorrelation correlation : correlations) {
            Integer molId = correlation.getMolId();
            if (molId == null) {
                result.add(Optional.empty());
                continue;
            }

            Path path = Path.of(String.format(inhouseDB.getConfigString("compounds.chemicalDrawing"), molId));
            if (!Files.exists(path)) {
                logMissingCDXML(molId, path);
                result.add(Optional.empty());
                continue;
            }

            try {
                byte[] bytes = Files.readAllBytes(path);
                String cdxml = new String(bytes, StandardCharsets.ISO_8859_1);
                result.add(Optional.of(new Experiments.ChemDrawData(molId, cdxml)));
            } catch (IOException e) {
                throw new RuntimeException("Cannot read CDXML file for molId=" + molId, e);
            }
        }

        return result;
    }

    private List<InhouseCorrelation> getCorrelations(int procId) {
        return correlationCache.computeIfAbsent(procId, id -> {
            try {
                return inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(id);
            } catch (Exception e) {
                logger.warn("No correlation found for procId={}", id);
                return List.of();
            }
        });
    }

    private void logMissingCDXML(Integer molId, Path path) {
        try (ErrorLogger logger = new ErrorLogger("cdxml_missing.log")) {
            logger.log(String.format("Missing CDXML file for molId=%s at %s", molId, path));
        } catch (Exception e) {
            this.logger.warn("Error writing to cdxml_missing.log", e);
        }
    }

    public Map<Integer, List<Optional<Experiments.ChemDrawData>>> getCdxmlCache() {
        return cdxmlCache;
    }
}

