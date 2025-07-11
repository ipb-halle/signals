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

public class AdoCreator {

    private final AdoManager adoManager;

    public AdoCreator(AdoManager adoManager){
        this.adoManager = adoManager;
    }

    public Ado createAdo(String ancestorId, String name, String ipbCode){
        return adoManager.findIpbCodeAdoForInhouseExperiment(ancestorId, name, ipbCode);
    }
}
