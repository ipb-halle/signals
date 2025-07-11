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

import java.util.HashMap;

public class SampleCreator {

    public static final String TEMPLATE_ID = "samples.templateIdChemicalSample";
    public static final String FIELD_DESCRIPTION = "samples.fields.description";
    public static final String FIELD_IPB_CODE = "samples.fields.ipbCode";

    private final InhouseDB inhouseDB;
    private final AdoCreator adoCreator;

    public SampleCreator(InhouseDB inhouseDB, AdoCreator adoCreator) {
        this.inhouseDB = inhouseDB;
        this.adoCreator = adoCreator;
    }

    public void createSample(String chemDrawId, String rowId, String ancestorId, String description, String ipbCode) {
        Sample sample = new Sample();
        sample.setTemplateId(TEMPLATE_ID);

        SignalsEntity ancestor = new SignalsEntity();
        ancestor.setEid(ancestorId);
        sample.addAncestor(ancestor);
        sample.setAncestorId(ancestorId);

        StoicRef ref = new StoicRef();
        ref.setEid(chemDrawId);
        ref.setRowId(rowId);
        sample.setStoicRef(ref);

        // Add description
        SamplePropertyValue desc = new SamplePropertyValue();
        desc.setPropertyId(inhouseDB.getConfigString(FIELD_DESCRIPTION));
        desc.setPropertyValue(description);
        sample.addPropertyValue(desc);

        String sampleId = inhouseDB.getSampleRestService().createNewSample(sample);
        sample.setId(sampleId);
        desc.setSampleId(sampleId);

        // Create ADO
        Ado ado = adoCreator.createAdo(sampleId, description, ipbCode);
        SamplePropertyValue adoRef = new SamplePropertyValue();
        adoRef.setPropertyId(inhouseDB.getConfigString(FIELD_IPB_CODE));
        adoRef.setSampleId(sampleId);
        adoRef.setPropertyValue(ado.getType() + ";" + ado.getName() + ";" + ado.getEid());
        sample.addPropertyValue(adoRef);

        // Update via PATCH
        HashMap<String, String> properties = new HashMap<>();
        for (SamplePropertyValue val : sample.getPropertyValues()) {
            properties.put(val.getPropertyId(), val.getPropertyValue());
        }
        inhouseDB.getSampleRestService().updateSamplePropertyValues(properties, sampleId);
    }
}
