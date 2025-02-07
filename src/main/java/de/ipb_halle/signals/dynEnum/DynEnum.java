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
package de.ipb_halle.signals.dynEnum;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;


/**
 * Dynamically discovered "enum" types from Signals Notebook
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "DYN_ENUMS")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 40)
public abstract class DynEnum <T> {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private String value;

    /**
     * default - no argument - constructor
     */
    public DynEnum() {
    }

    /**
     * concrete value constructor
     */
    public DynEnum(String v) {
        value = v;
    }

    public Integer getId() {
        return id;
    }

    public String getShortType() {
        String cn = this.getClass().getName();
        return cn.substring(cn.lastIndexOf("."));
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && o.getClass().equals(this.getClass())) {
            DynEnum e = (DynEnum) o;
            if ((getValue() != null) && getValue().equals(e.getValue())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        return value.hashCode() + this.getClass().hashCode();
    }

    public String toString() {
        return value + getShortType();
    }
}
