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
package de.ipb_halle.signals.entity;


/** 
 * Field types as defined by SNB Swagger API
 */

public enum FieldType {

    ATTACHED_FILE,
    ATTRIBUTE,
    ATTRIBUTELIST,
    BOOLEAN,
    CAS_NUMBER,
    CHEMICAL_DRAWING,
    DATETIME,
    DECIMAL,
    DENSITY,
    EXTERNAL_LINK,
    INTEGER,
    LINK,
    LIST,
    MASS,
    MOLECULAR_FORMULA,
    MOLECULAR_MASS,
    MOLARITY,
    PERCENTAGE,
    SEQUENCE,
    TEMPERATURE,
    TEXT,
    UNIT,
    USER,
    VARIABLE_MEASURE;

    /**
     * Return a FieldType corresponding to a given String. As 
     * the SNB Swagger API uses mixed casing, the String will be
     * converted to all uppercase before looking up the FieldType.
     * @param v the string 
     * @return the corresponding FieldType
     */
    public static FieldType valueOfAnyCase(String v) {
        return valueOf(v.toUpperCase());
    }
}
