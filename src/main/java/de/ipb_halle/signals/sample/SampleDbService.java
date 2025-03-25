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

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Stateless
@PersistenceElements(entities = {SampleEntity.class})
public class SampleDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Inject
    private DynEnumManager dynEnumManager;

    @Inject
    private FieldDbService fieldDbService;

    private static final Logger logger = LogManager.getLogger(SamplesManager.class);


    public void save(Sample sample) {
        SampleEntity se = sample.createEntity();
        for (SampleProperty sp : sample.getProperties()) {
            SamplePropertyEntity spe = sp.createEntity();
            this.em.merge(spe);
        }
        this.em.merge(se);
    }
}
