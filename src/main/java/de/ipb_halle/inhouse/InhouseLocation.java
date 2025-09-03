/*
 *
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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

package de.ipb_halle.inhouse;

import de.ipb_halle.signals.inventory.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inhouse_locations")
public class InhouseLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column
    private String name;

    @Column(name = "location_columns")
    private int columns;

    @Column(name = "location_rows")
    private int rows;

    @Column(name = "zero_based")
    private boolean zeroBased;


    public Integer getId() {
        return id;
    }

    public InhouseLocation setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public InhouseLocation setName(String name) {
        this.name = name;
        return this;
    }

    public int getColumns() {
        return columns;
    }

    public InhouseLocation setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    public int getRows() {
        return rows;
    }

    public InhouseLocation setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseLocation setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public boolean isZeroBased() {
        return zeroBased;
    }

    public InhouseLocation setZeroBased(boolean zeroBased) {
        this.zeroBased = zeroBased;
        return this;
    }


}
