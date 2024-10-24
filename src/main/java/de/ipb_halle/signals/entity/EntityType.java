/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.dynEnum.DynEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** 
 * Dynamically discovered entity types from Signals Notebook
 */

@Entity
@DiscriminatorValue("EntityType")
public class EntityType extends DynEnum <EntityType> { 

    /**
     * private no-argument constructor
     */
    private EntityType() { }

    private EntityType(String v) {
        super(v);
    }

    public static EntityType valueOf(String v) {
        return new EntityType(v);
    }
}
