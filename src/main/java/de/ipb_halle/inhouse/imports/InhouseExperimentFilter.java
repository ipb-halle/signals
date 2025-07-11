package de.ipb_halle.inhouse.imports;

import de.ipb_halle.inhouse.*;

import java.util.*;

public class InhouseExperimentFilter {

    private final InhouseDB inhouseDB;

    public InhouseExperimentFilter(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    public Map<InhouseImportType, List<InhouseExperiment>> filter(
            List<InhouseExperiment> experiments,
            ChemDrawCacheService chemDrawCache,
            ErrorLogger errorLogger) throws Exception {

        int missingThreeLc = 0;
        int missingProcId = 0;
        int missingChemDraw = 0;

        List<InhouseExperiment> structure = new ArrayList<>();
        List<InhouseExperiment> organism = new ArrayList<>();
        List<InhouseExperiment> unknown = new ArrayList<>();

        for (InhouseExperiment exp : experiments) {
            if (exp.getThreelc() == null) {
                missingThreeLc++;
                continue;
            }
            if (exp.getProcId() == 0) {
                missingProcId++;
                continue;
            }

            int procId = exp.getProcId();
            List<Optional<Experiments.ChemDrawData>> cdxmlList = chemDrawCache.getChemDrawData(procId);

            if (cdxmlList.isEmpty() || cdxmlList.stream().allMatch(Optional::isEmpty)) {
                missingChemDraw++;
                if (hasOrgId(procId)) {
                    organism.add(exp);
                } else {
                    unknown.add(exp);
                }
            } else {
                structure.add(exp);
            }
        }

        errorLogger.log("Experiments with missing threeLC = " + missingThreeLc);
        errorLogger.log("Experiments with missing procedureId = " + missingProcId);
        errorLogger.log("Experiments with missing ChemDraw = " + missingChemDraw);

        return Map.of(
                InhouseImportType.STRUCTURE, structure,
                InhouseImportType.ORGANISM, organism,
                InhouseImportType.UNKNOWN, unknown
        );
    }

    private boolean hasOrgId(int procId) {
        return inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId).stream()
                .anyMatch(c -> c.getOrganismId() != null);
    }

}
