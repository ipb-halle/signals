/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.field.*;
import de.ipb_halle.tda.PersistenceElements;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Database service for containers
 */

@Stateless
@PersistenceElements(entities = {ContainerEntity.class})
public class ContainerDbService {

    public static final Logger logger = LogManager.getLogger(ContainerDbService.class);

    @Inject
    private FieldDbService fieldDbService;


    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;


    public Container loadById(String id) {
        ContainerEntity entity = this.em.find(ContainerEntity.class, id);
        if (entity == null) {
            return null;
        }
        return new Container(entity);
    }

    public void save(Container ct) {
        ContainerEntity ce = ct.createEntity();
        this.em.merge(ce);
        for (Field f : ct.getFields()) {
            f.setId(f.getId() + ":" + ct.getId());
            fieldDbService.save(f);
        }

        for (FieldValue fv : ct.getFieldValues()) {
            FieldValueEntity fve = fv.createEntity();
            fve.setEntityId(ct.getId());
            fve.setFieldDefinitionId(fv.getFieldId() + ":" + ct.getId());
            em.merge(fve);
        }
    }

}

