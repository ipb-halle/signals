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

import de.ipb_halle.inhouse.InhouseDB;
import de.ipb_halle.signals.ado.Ado;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.signals.sample.SamplePropertyValue;
import de.ipb_halle.signals.sample.StoicRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.HashMap;

public class SampleCreator {

    public static final String CHEMICAL_SAMPLE_TEMPLATE_ID = "samples.templateIdChemicalSample";
    public static final String FIELD_DESCRIPTION = "samples.fields.description";
    public static final String FIELD_IPB_CODE = "samples.fields.ipbCode";
    public static final String ADO_TEMPLATE_ID = "ados.templateAdo2";
    public static final String FIELD_ADO_IPB_CODE = "ados.fields.ipbCode";
    public static final String FIELD_ADO_NAME = "ados.fields.name";

    private final InhouseDB inhouseDB;
    private final AdoCreator adoCreator;
    private final static Logger logger = (Logger) LogManager.getLogger(SampleCreator.class);

    public SampleCreator(InhouseDB inhouseDB, AdoCreator adoCreator) {
        this.inhouseDB = inhouseDB;
        this.adoCreator = adoCreator;
    }

    public void createSample(String chemDrawId, String rowId, String ancestorId, String description, String ipbCode) {
        // Create a new Sample and assign the chemical sample template
        Sample sample = new Sample();
        sample.setTemplateId(inhouseDB.getConfigString(CHEMICAL_SAMPLE_TEMPLATE_ID));

        // Set the ancestor relationship (usually a sample container or experiment)
        SignalsEntity ancestor = new SignalsEntity();
        ancestor.setEid(ancestorId);
        sample.addAncestor(ancestor);
        sample.setAncestorId(ancestorId);

        // Create the stoichiometry reference pointing to a row in the chemical drawing
        StoicRef ref = new StoicRef();
        ref.setEid(chemDrawId);
        ref.setRowId(rowId);
        sample.setStoicRef(ref);

        // Description property
        SamplePropertyValue desc = new SamplePropertyValue();
        desc.setPropertyId(inhouseDB.getConfigString(FIELD_DESCRIPTION));
        desc.setPropertyValue(description);
        sample.addPropertyValue(desc);


//        // Create ADO
//        logger.info("SampleCreator:-> Starting create Ados");
//        String adoTemplateId = inhouseDB.getConfigString(ADO_TEMPLATE_ID);
//
//        Ado ado = adoCreator.createAdo( description, ipbCode, adoTemplateId);
//        if(ado == null){
//            logger.error("Failed to create or load ADO for IPB code '{}'", ipbCode);
//            return;
//        }else {
//            logger.info("SampleCreator-> ADO created = {}\n", ado.toString());
//        }
//
//
//        SamplePropertyValue adoRef = new SamplePropertyValue();
//        adoRef.setPropertyId(inhouseDB.getConfigString(FIELD_IPB_CODE));
//        adoRef.setPropertyValue(ado.getType() + ";" + ado.getName() + ";" + ado.getEid());
//        sample.addPropertyValue(adoRef);

        // Create the sample via REST and store the returned Signals sample ID
        sample.addPropertyValue(desc);
      //  sample.addPropertyValue(adoRef);
        String sampleId = inhouseDB.getSampleRestService().createNewSample(sample);
        logger.info("SC-> sample = {}\n", sample.toString());
        sample.setId(sampleId);
        desc.setSampleId(sampleId);
       // adoRef.setSampleId(sampleId);


        // Prepare properties as key-value pairs for PATCH update
        HashMap<String, String> propertyKeyToValue = new HashMap<>();
        for (SamplePropertyValue samplePropertyValue : sample.getPropertyValues()) {
            // id                                  // value
            propertyKeyToValue.put(samplePropertyValue.getPropertyId(), samplePropertyValue.getPropertyValue());
        }

        // Perform a PATCH request to update the properties of the newly created sample
        inhouseDB.getSampleRestService().updateSamplePropertyValues(propertyKeyToValue, sampleId);
    }
}
