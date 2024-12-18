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
package de.ipb_halle.signals.field;

import de.ipb_halle.signals.dynEnum.DynEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/** 
 * Field types as defined by SNB Swagger API
 */

@Entity
@DiscriminatorValue("FieldType")
public class FieldType extends DynEnum <FieldType> {

    public final static String ATTACHED_FILE = "ATTACHED_FILE";
    public final static String CHEMICAL_DRAWING = "CHEMICAL_DRAWING";
    public final static String SEQUENCE = "SEQUENCE";
    public final static String TEXT = "TEXT";

    /**
     * private no-argument constructor
     */
    private FieldType() { }

    private FieldType(String v) {
        super(v);
    }

    public static FieldType valueOf(String v) {
        return new FieldType(v);
    }

/*
     * Return a FieldType corresponding to a given String. As 
     * the SNB Swagger API uses mixed casing, the String will be
     * converted to all uppercase before looking up the FieldType.
     * @param v the string 
     * @return the corresponding FieldType
     *
    public static FieldType valueOfAnyCase(String v) {
        return valueOf(v.toUpperCase());
    }
*/


}
