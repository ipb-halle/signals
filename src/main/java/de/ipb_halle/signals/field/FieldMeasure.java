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

import java.util.Objects;

import de.ipb_halle.signals.entity.Quality;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;


/** 
 * Quality (measure) - field assignments
 */

@Entity
@IdClass(FieldMeasureId.class)
@Table(name="field_measures")
public class FieldMeasure {


    @Id
    private String field_id;

    @Id
    private Quality measure;

    /**
     * default constructor
     */
    public FieldMeasure() {
    }

    public FieldMeasure(String i, Quality q) {
        field_id = i;
        measure = q;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        FieldMeasure other = (FieldMeasure) o;
        return Objects.equals(field_id, other.field_id)
            && Objects.equals(measure, other.measure);
    }

    public String getFieldId() {
        return field_id;
    }

    public Quality getMeasure() {
        return measure;
    }

    @Override
    public int hashCode() {
        return getFieldId().hashCode() + getMeasure().hashCode();
    }

    public void setFieldId(String i) {
        field_id = i;
    }

    public void setMeasure(Quality q) {
        measure = q;
    }
}
