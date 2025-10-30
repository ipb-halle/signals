package de.ipb_halle.inhouse.imports;

import de.ipb_halle.inhouse.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.*;

/**
 * Filters a list of {@link InhouseExperiment} objects into categories
 * based on their metadata and the presence of associated ChemDraw data.
 *
 * <p>This class separates experiments into three main types:
 * <ul>
 *   <li>{@link InhouseImportType#STRUCTURE} – Experiments with valid ChemDraw structures (molId, optional cdxml)</li>
 *   <li>{@link InhouseImportType#ORGANISM} – Experiments without ChemDraw structures
 *       but linked to an organism ID</li>
 *   <li>{@link InhouseImportType#UNKNOWN} – Experiments without ChemDraw structures
 *       and without any organism ID</li>
 * </ul>
 *
 * <p>It also counts and logs common data issues, such as:
 * <ul>
 *   <li>Missing ThreeLC code</li>
 *   <li>Missing procedure ID</li>
 *   <li>Missing ChemDraw data</li>
 * </ul>
 *
 * <p>Filtering results are returned as a map from {@link InhouseImportType} to
 * lists of matching experiments.
 *
 * <p>Dependencies:
 * <ul>
 *   <li>{@link InhouseDB} – Used to query organism relationships</li>
 *   <li>{@link ChemDrawCacheService} – Used to check if ChemDraw structures exist</li>
 *   <li>{@link ErrorLogger} – Logs missing/invalid experiment data</li>
 * </ul>
 */
public class InhouseExperimentFilter {

    private final InhouseDB inhouseDB;
    private final Logger logger = (Logger) LogManager.getLogger(InhouseExperimentFilter.class);

    public InhouseExperimentFilter(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
    }

    /**
     * Filters a list of experiments into STRUCTURE, ORGANISM, and UNKNOWN categories.
     *
     * <p>Rules:
     * <ol>
     *   <li>If the experiment is missing a ThreeLC code → it is skipped and counted as missing ThreeLC</li>
     *   <li>If the experiment has a procedure ID of 0 → it is skipped and counted as missing ProcId</li>
     *   <li>If the experiment has no ChemDraw data:
     *       <ul>
     *         <li>If it is linked to an organism ID → goes into ORGANISM list</li>
     *         <li>Otherwise → goes into UNKNOWN list</li>
     *       </ul>
     *   </li>
     *   <li>If the experiment has ChemDraw data → goes into STRUCTURE list</li>
     * </ol>
     *
     * <p>Logs summary counts for missing fields and category sizes.
     *
     * @param experiments   List of experiments to filter
     * @param chemDrawCache Cache service for retrieving ChemDraw data by procedure ID
     * @param errorLogger   Logger for recording missing/invalid field information
     * @return Map of {@link InhouseImportType} to filtered experiment lists
     * @throws Exception if data retrieval from the database or ChemDraw cache fails
     */
    public Map<InhouseImportType, List<InhouseExperiment>> filter(
            List<InhouseExperiment> experiments,
            ChemDrawCacheService chemDrawCache,
            ErrorLogger errorLogger) throws Exception {

        int missingThreeLc = 0;
        int missingProcId = 0;
        int missingChemDraw = 0;

        List<InhouseExperiment> structures = new ArrayList<>();
        List<InhouseExperiment> extracts = new ArrayList<>();
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
                if (hasOrgId(procId) && hasExtract(procId)) {
                    extracts.add(exp);
                } else {
                    unknown.add(exp);
                }
            } else {
                structures.add(exp);
            }
        }

        errorLogger.log("Experiments with missing threeLC = " + missingThreeLc);
        logger.info("IEF-> MISSING THREELC = {}\n", missingThreeLc);
        errorLogger.log("Experiments with missing procedureId = " + missingProcId);
        logger.info("IEF-> MISSING PROCID = {}\n", missingProcId);
        errorLogger.log("Experiments with missing ChemDraw = " + missingChemDraw);
        logger.info("IEF-> MISSING CHEMDRAW = {}\n", missingChemDraw);

        logger.info("IEF-> STRUCTURE ARRAY = {}\n, ORGANISM ARRAY ={}\n, UNKNOWN ARRAY = {}\n", structures.size(), extracts.size(), unknown.size());

        return Map.of(
                InhouseImportType.STRUCTURE, structures,
                InhouseImportType.ORGANISM, extracts,
                InhouseImportType.UNKNOWN, unknown
        );
    }

    // toDo: test schreiben!!!! eintrag zu extractvorhandensein in physischen verkörperung und eintrag in labor journal trennen! (nicht jeder orgproc hat einen extrakt)
    private boolean hasExtract(int procId) {
        List<InhouseCorrelation> correlations = inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId);
        for (InhouseCorrelation ic : correlations) {
            if (ic.getContext().equalsIgnoreCase("orgproc")) {
                List<InhouseExtract> inhouseExtracts = inhouseDB.getInhouseDbService().loadExtractByCorrOrgProcId(ic.getCorrId());
                if (!inhouseExtracts.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a given procedure ID is linked to at least one organism.
     *
     * @param procId The procedure ID to check
     * @return {@code true} if any correlation entry contains a non-null organism ID, {@code false} otherwise
     */
    private boolean hasOrgId(int procId) {
        return inhouseDB.getInhouseDbService().loadCorrelationByProcedureId(procId).stream()
                .anyMatch(c -> c.getOrganismId() != null);
    }

}
