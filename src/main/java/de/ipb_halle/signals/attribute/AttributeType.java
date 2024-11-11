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
package de.ipb_halle.signals.attribute;

import de.ipb_halle.signals.dynEnum.DynEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** 
 * Dynamically discovered entity types from Signals Notebook
 */

@Entity
@DiscriminatorValue("AttributeType")
public class AttributeType extends DynEnum <AttributeType> {

    public final static String CHOICE = "choice";
    public final static String SEQUENCE = "auto";

    /**
     * private no-argument constructor
     */
    private AttributeType() { }

    private AttributeType(String v) {
        super(v);
    }

    public static AttributeType valueOf(String v) {
        return new AttributeType(v);
    }
}
