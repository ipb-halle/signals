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
package de.ipb_halle.signals.field;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


/** 
 * DB service for field definitions
 */

@Stateless
public class FieldDefinitionDbService {


    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;


    /**
     * Load a FieldDefinition entity by its ID.
     * @param id the ID of the FieldDefinition entity
     * @return the FieldDefinition entity
     */
    public FieldDefinition loadById(String id) {
        return this.em.find(FieldDefinition.class, id);
    }

    /**
     * Save a FieldDefinition entity to the database.
     * @param fd the FieldDefinition entity to save
     */
    public void save(FieldDefinition fd) {
        this.em.merge(fd);
    }
}

