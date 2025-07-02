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

import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import de.ipb_halle.signals.sample.StoicRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StructureImportStrategy implements InhouseImportStrategy {

    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "sample:0174e78c-0b95-49f9-8a57-39061bbc0050";
    public static final String CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION = "2";
    public static final String CHEMICAL_SAMPLE_IPB_CODE_INTERNAL_REFERENCE_ID = "109";
    public static final String SIGNALS_ENTITY_CUSTOM_OBJECT_IPB_CODE = "ado-1:4b98a234-e223-4c67-b6b1-f9df67958460";
    private final Logger logger = LogManager.getLogger(StructureImportStrategy.class);

    @Override
    public void importGroup(InhouseDB inhouseDB, String threeLc, List<InhouseExperiment> group, Map<Integer, List<Optional<Experiments.ChemDrawData>>> cdxmlCache) throws Exception {

        logger.info("Ich bin in Structure import strategy\n");

        if (group.isEmpty()) {
            logger.warn("StructureImportStrategy:-> Empty experiment list grouped by threeLc {}\n", threeLc);
            return;
        }

        //Set an error logger for writing an errors in the file
        try (ErrorLogger errorLogger = new ErrorLogger("errorLog_for_structureImportStrategy.txt");) {

            // set the experiment for 10 samples
            Experiments helper = new Experiments(inhouseDB);
            int chunkSize = 10;
            int experimentCounter = 1;

            for (int i = 0; i < group.size(); i += chunkSize) {
                int toIndex = Math.min(i + chunkSize, group.size());
                List<InhouseExperiment> chunk = group.subList(i, toIndex);

                InhouseExperiment main = chunk.get(0);
                String experimentName = threeLc + "-MOL-" + experimentCounter++;
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
                            errorLogger.log("Missing ChemDrawData (should not happen) for procedureId = " + procId);
                            continue;
                        }

                        // Create ChemDraw parent entity -> empty
                        Experiments.ChemDrawData data = optional.get();
                        String chemDrawId = helper.createChemDrawForInhouseExperiment(data.molId(), eid);

                        // Add a cdxml to empty chemDrawing entity as a product
                        if (!data.fieldValueCdxml().isEmpty()) {
                            inhouseDB.getExperimentRestService().addReactionToExperiment(
                                    // POSITIONS-> = reactants|products|reagents|grid
                                    chemDrawId, "products", data.fieldValueCdxml());
                        }

                        // setting description field value
                        String desc = String.format("MolId: %s, Experiment: %s%s, Journal: %s", data.molId(), threeLc, procId, exp.getJournal());

                        // Load IpbCode
                        String ipbCode = loadIpbCodeByMolId(data.molId(), inhouseDB);
                        if (ipbCode == null || ipbCode.trim().isEmpty()) {
                            ipbCode = "IPB code not assigned";
                        }

                        // create non-chemical sample with field values in sampleContainer -> sample table -> rest call POST
                        createSampleForChemicalDrawing(inhouseDB, chemDrawId, "1", eid, desc, ipbCode);

                        exp.setEid(eid);
                        exp.setImportSuccessful(true);
                        inhouseDB.getInhouseDbService().markAsSuccessfullyImported(exp);
                    }
                }
            }
        }
    }


    /**
     * Creates a new chemical sample in the Signals platform based on a given chemical drawing.
     * <p>
     * The method performs the following steps:
     * <ul>
     *     <li>Initializes a new {@link Sample} object with the correct chemical sample template.</li>
     *     <li>Sets the ancestor (parent) container or experiment by its Signals EID.</li>
     *     <li>Assigns the stoichiometry reference (stoicRef) linking the sample to the chemical drawing and its row.</li>
     *     <li>Sets initial sample property values such as molecule ID (molId).</li>
     *     <li>Creates the sample via the REST API and retrieves the generated sample ID.</li>
     *     <li>Updates the sample's properties using a PATCH request to the sample API endpoint.</li>
     * </ul>
     * <p>
     * This method is used when importing data from the legacy database and linking it to a chemical drawing in Signals.
     *
     * @param chemDrawId the EID of the chemical drawing to which the sample should be linked
     * @param rowId      the row index of the drawing’s stoichiometry table
     * @param ancestorId the EID of the container or experiment to act as the sample's parent
     */
    public void createSampleForChemicalDrawing(InhouseDB inhouseDB, String chemDrawId, String rowId, String ancestorId, String desc, String ipbCode) {
        // Create a new Sample and assign the chemical sample template
        Sample sample = new Sample();
        sample.setTemplateId(CHEMICAL_SAMPLE_TEMPLATE_ID);

        // Set the ancestor relationship (usually a sample container or experiment)
        SignalsEntity signals = new SignalsEntity();
        signals.setEid(ancestorId);
        sample.addAncestor(signals);
        sample.setAncestorId(ancestorId);

        // Create the stoichiometry reference pointing to a row in the chemical drawing
        StoicRef stoicRef = new StoicRef();
        stoicRef.setEid(chemDrawId);
        stoicRef.setRowId(rowId);
        sample.setStoicRef(stoicRef);

        // Set the propertyValue description
        SamplePropertyValue fvDescription = new SamplePropertyValue();
        fvDescription.setPropertyId(CHEMICAL_SAMPLE_PROPERTY_ID_DESCRIPTION);
        fvDescription.setPropertyValue(desc);

        // Create the sample via REST and store the returned Signals sample ID
        sample.addPropertyValue(fvDescription);
        String sampleId = inhouseDB.getSampleRestService().createNewSample(sample);
        sample.setId(sampleId);
        fvDescription.setSampleId(sampleId);

        // Create a custom object IPB code (as name for this custom object the desc will be used , because it contains all needed information for structure)
        SignalsEntityDTO eidCustomObjectIpbCode = createCustomObjectIpbCodeAsSignalsEntity(sampleId, desc, inhouseDB);

        //Add a custom object with IPB code
        SamplePropertyValue fvAdoRef = new SamplePropertyValue();
        fvAdoRef.setPropertyId(CHEMICAL_SAMPLE_IPB_CODE_INTERNAL_REFERENCE_ID);
        //for internal reference the format should be type:name:eid
        String value = eidCustomObjectIpbCode.getType() + ";" + eidCustomObjectIpbCode.getName() + ";" + eidCustomObjectIpbCode.getEid();
        fvAdoRef.setPropertyValue(value);
        sample.addPropertyValue(fvAdoRef);
        fvAdoRef.setSampleId(sampleId);

        // Prepare properties as key-value pairs for PATCH update
        HashMap<String, String> propertyKeyToValue = new HashMap<>();
        for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
            logger.info("StructureImportStrategy:-> sample properties key value map -> key =  {}, value = {}\n",
                    samplePropertyValue.getPropertyId(), samplePropertyValue.getPropertyValue());
            // id                                  // value
            propertyKeyToValue.put(samplePropertyValue.getPropertyId(), samplePropertyValue.getPropertyValue());
        }

        // Perform a PATCH request to update the properties of the newly created sample
        inhouseDB.getSampleRestService().updateSamplePropertyValues(propertyKeyToValue, sampleId);
    }

    private SignalsEntityDTO createCustomObjectIpbCodeAsSignalsEntity(String ancestorId, String name, InhouseDB inhouseDB) {

        return inhouseDB.getSignalsEntityRestService().createIpbCodeCustomObjectSignalsEntity(ancestorId, name, SIGNALS_ENTITY_CUSTOM_OBJECT_IPB_CODE);
    }

    public String loadIpbCodeByMolId(Integer mol_id, InhouseDB inhouseDB) {
        InhouseCompound compound = inhouseDB.getInhouseDbService().loadCompoundByMolId(mol_id);
        return (compound != null && compound.getIpbCode() != null && !compound.getIpbCode().trim().isEmpty())
                ? compound.getIpbCode()
                : "";

    }


}
