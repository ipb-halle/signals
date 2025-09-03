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

import de.ipb_halle.inhouse.InhouseContainer;
import de.ipb_halle.inhouse.InhouseCorrelation;
import de.ipb_halle.inhouse.InhouseDB;
import de.ipb_halle.signals.ado.Ado;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import de.ipb_halle.signals.sample.StoicRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SampleCreator {


    public static final String FIELD_DESCRIPTION = "samples.fields.description";
    public static final String FIELD_IPB_CODE = "samples.fields.ipbCode";
    public static final String ADO_TEMPLATE_ID = "ados.templateAdo2";

    private final InhouseDB inhouseDB;
    private final AdoCreator adoCreator;
    private final SampleContainerAttacher attacher;
    private final static Logger logger = (Logger) LogManager.getLogger(SampleCreator.class);
    private final DynEnumManager dynEnumManager;

    public SampleCreator(InhouseDB inhouseDB, AdoCreator adoCreator) {
        this.inhouseDB = inhouseDB;
        this.adoCreator = adoCreator;
        this.attacher = new SampleContainerAttacher(inhouseDB);
        this.dynEnumManager = inhouseDB.getDynEnumManager();
    }

    public void createChemicalSample(String sampleTemplateId,
                                     String chemDrawId,
                                     String rowId,
                                     String ancestorId,
                                     String description,
                                     String ipbCode,
                                     Integer procId) throws Exception {

        // 1) Create a new Sample and assign the chemical sample template
        Sample sample = baseSample(sampleTemplateId, ancestorId);
        sample.setName(description);

        // Create the stoichiometry reference pointing to a row in the chemical drawing
        StoicRef ref = new StoicRef();
        ref.setEid(chemDrawId);
        ref.setRowId(rowId);
        sample.setStoicRef(ref);

        // Create the sample via REST and store the returned Signals sample ID
        String eid = inhouseDB.getSampleRestService().createNewSample(sample);

        // Description property
        SamplePropertyValue desc = attachDescription(sample, eid, description);

        // === ADO link (optional) ===
        // Load or create ADO
        attachIPB_CodeAdo(sample, eid, ipbCode);

       // attachSampleContainer(sample, eid, procId);
        attacher.attachSampleContainer(sample, eid, procId);
        // Prepare properties as key-value pairs for PATCH update
        patch(sample, eid);
    }



    public void createExtractSample(String templateId, String ancestorId, String description, String ipbCode) {
        Sample sample = baseSample(templateId, ancestorId);

        String sampleId = inhouseDB.getSampleRestService().createNewSample(sample);
        attachDescription(sample, sampleId, description);
        attachIPB_CodeAdo(sample, sampleId, ipbCode);
        //attachOrganism()
        //attachChemDraw()
        patch(sample, sampleId);
    }

    private Sample baseSample(String sampleTemplateId, String ancestorId) {
        Sample sample = new Sample();
        sample.setTemplateId(inhouseDB.getConfigString(sampleTemplateId));

        // Set the ancestor relationship (usually a sample container or experiment)
        SignalsEntity ancestor = new SignalsEntity();
        ancestor.setEid(ancestorId);
        sample.addAncestor(ancestor);
        sample.setAncestorId(ancestorId);
        return sample;
    }

    private SamplePropertyValue attachDescription(Sample sample, String sampleId, String description) {
        SamplePropertyValue desc = new SamplePropertyValue();
        desc.setPropertyId(inhouseDB.getConfigString(FIELD_DESCRIPTION));
        desc.setPropertyValue(description);
        sample.setId(sampleId);
        desc.setSampleId(sampleId);
        sample.addPropertyValue(desc);
        return desc;
    }

    private void attachIPB_CodeAdo(Sample sample, String sampleId, String ipbCode) {
        if (ipbCode != null) {
            logger.info("SampleCreator:-> Starting create Ados");
            String adoTemplateId = inhouseDB.getConfigString(ADO_TEMPLATE_ID);
            Optional<Ado> maybeAdo = adoCreator.findOrCreateAdoByIpbCode(ipbCode, adoTemplateId);
            if (maybeAdo.isPresent()) {
                Ado ado = maybeAdo.get();
                SamplePropertyValue adoRef = new SamplePropertyValue();
                adoRef.setPropertyId(inhouseDB.getConfigString(FIELD_IPB_CODE));
                adoRef.setPropertyValue(ado.getType() + ";" + ado.getName() + ";" + ado.getEid());
                sample.addPropertyValue(adoRef);
                adoRef.setSampleId(sampleId);
            } else {
                logger.info("Skipping ADO link: not found/created for ipbCode={}", ipbCode);
            }
        } else {
            logger.info("Skipping ADO link: missing IPB code");
        }
    }

    private void patch(Sample sample, String sampleId) {
        HashMap<String, String> propertyKeyToValue = new HashMap<>();
        for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
            //                          id                                  value
            propertyKeyToValue.put(samplePropertyValue.getPropertyId(), samplePropertyValue.getPropertyValue());
        }

        // Perform a PATCH request to update the properties of the newly created sample
        inhouseDB.getSampleRestService().updateSamplePropertyValues(propertyKeyToValue, sampleId);
    }
}
