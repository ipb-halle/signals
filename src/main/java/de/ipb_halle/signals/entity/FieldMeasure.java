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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/** 
 * Quality (measure) - field assignments
 */

@Entity
@Table(name="field_measures")
public class FieldMeasure {


    @EmbeddedId
    private FieldMeasureId id;


    public FieldMeasure() {
        id = new FieldMeasureId();
    }

    public FieldMeasure(String i, Quality q) {
        id = new FieldMeasureId();
        id.setFieldId(i);
        id.setMeasure(q);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) { 
            return false;
        } 
        FieldMeasure other = (FieldMeasure) o;
        return (id.getFieldId() == other.getFieldId())
            && (id.getMeasure() == other.getMeasure());
    }

    public String getFieldId() {
        return id.getFieldId();
    }

    public Quality getMeasure() {
        return id.getMeasure();
    }

    @Override
    public int hashCode() {
        return getFieldId().hashCode() + getMeasure().hashCode();
    }

    public void setFieldId(String i) {
        id.setFieldId(i);
    }

    public void setMeasure(Quality q) {
        id.setMeasure(q);
    }
}
