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

import de.ipb_halle.signals.ado.Ado;
import de.ipb_halle.signals.ado.AdoManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Responsible for retrieving or creating ADOs by IPB code.
 * Delegates logic to AdoManager.
 */
public class AdoCreator {

    private final AdoManager adoManager;
    private final static Logger logger = LogManager.getLogger(AdoCreator.class);

    public AdoCreator(AdoManager adoManager) {
        this.adoManager = adoManager;
    }

    /**
     * For Development
     * Returns ADO with the given IPB code. If not found, tries to generate it.
     *
     * @param ipbCode    IPB code to search
     * @param templateId ADO template
     * @return Optional of ADO if found or created
     */
    public Optional<Ado> findOrCreateAdoByIpbCode(String ipbCode, String templateId) {
        return adoManager.findOrCreateAdoByIpbCode(ipbCode, templateId);
    }

    /**
     * For Production
     * Generates all ADOs up to a given max IPB code.
     */
    public void ensureAllAdoGenerated() {
        List<Ado> all = adoManager.createAllMissingAdosTillMaxIpbCode();
        logger.info("ensureAllAdosGenerated -> total ADOs ensured: {}\n", all.size());
    }
}
