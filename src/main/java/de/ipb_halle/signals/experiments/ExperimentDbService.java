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

package de.ipb_halle.signals.experiments;

import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@PersistenceElements(entities = {ExperimentEntity.class})
public class ExperimentDbService {

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager entityManager;

    public ExperimentEntity loadExperimentById(String id) {
        return entityManager.find(ExperimentEntity.class, id);
    }

    public void save(Experiment experiment) {
        ExperimentEntity experimentEntity = experiment.createEntity();

        entityManager.merge(experimentEntity);
    }
}
