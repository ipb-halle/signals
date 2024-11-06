/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
 * Physical quality of units of measurement
 *
 * @author fbroda
 */
public enum Quality {

    PIECES("ea", "count", "Count"),
    LENGTH("m", "length", "Length"),
    AREA("m^2", "area", "Area"),
    VOLUME("m^3", "volume", "Volume"),
    MASS("kg", "mass", "Mass"),
    DENSITY("kg/m^3", "density", "Density"),
    AMOUNT_OF_SUBSTANCE("mol", "amount_of_substance", "Amount"),
    MOLAR_MASS("kg/mol", "molar mass", "molar mass"),
    MOLAR_CONCENTRATION("mol/m^3", "molarity", "Molarity"),
    PERCENT_CONCENTRATION("[1]", "fraction", "Percentage"),
    MASS_CONCENTRATION("kg/m^3", "mass conc.", "mass conc."),
    CELL_COUNT_MASS("[1]", "integer", "Cell Count Mass");

    /*
     * Do not change this to Unit, because this will clash with the static block in
     * Unit.
     */
    private String baseUnit;
    private String label;
    private String snbMeasure;

    /**
     * SNB constants
     */
    public final static String ATTR_LABEL = "label";
    public final static String ATTR_MEASURE = "measure";

    private Quality(String baseUnit, String m, String l) {
        baseUnit = baseUnit;
        label = l;
        snbMeasure = m;
    }

    public Unit getBaseUnit() {
        return Unit.getUnit(baseUnit);
    }

    public String getLabel() {
        return label;
    }

    public String getSnbMeasure() {
        return snbMeasure;
    }
}
