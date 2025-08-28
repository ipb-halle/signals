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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * Joint object for InhouseDB "Samples" and "Extracts".
 */
@Entity
@Table(name = "inhouse_container")
public class InhouseContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column(name = "sample_id")
    private Integer sampleId;

    @Column
    private Double amount;              // mg

    @Column
    private Double tara;                // mg

    @Column
    private Double volume;              // ml

    @Column
    private Double concentration;       // mg/ml

    @Column(name = "sample_code")
    private String sampleCode;

    @Column
    private Integer purity;

    @Column
    private String appearance;

    @Column
    private String remarks;

    @Column(name = "ipb_code")
    private String ipbCode;             // "Extracts" only!

    @Column(name = "last_solvent")
    private String lastSolvent;

    @Column(name = "compound_correlation_id")
    private Integer molProcId;

    @Column(name = "organism_correlation_id")
    private Integer organismCorrelationId;

    @Column(name = "location")
    private String location;

    @Column(name = "location_id")
    private Integer locationId;

    @Column
    private Integer row;

    @Column(name = "container_column")
    private Integer column;

    public Integer getId() {
        return id;
    }

    public InhouseContainer setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseContainer setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public InhouseContainer setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public InhouseContainer setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Double getTara() {
        return tara;
    }

    public InhouseContainer setTara(Double tara) {
        this.tara = tara;
        return this;
    }

    public Double getVolume() {
        return volume;
    }

    public InhouseContainer setVolume(Double volume) {
        this.volume = volume;
        return this;
    }

    public Double getConcentration() {
        return concentration;
    }

    public InhouseContainer setConcentration(Double concentration) {
        this.concentration = concentration;
        return this;
    }

    public String getSampleCode() {
        return sampleCode;
    }

    public InhouseContainer setSampleCode(String sampleCode) {
        this.sampleCode = sampleCode;
        return this;
    }

    public Integer getPurity() {
        return purity;
    }

    public InhouseContainer setPurity(Integer purity) {
        this.purity = purity;
        return this;
    }

    public String getAppearance() {
        return appearance;
    }

    public InhouseContainer setAppearance(String appearance) {
        this.appearance = appearance;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseContainer setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public InhouseContainer setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    public String getLastSolvent() {
        return lastSolvent;
    }

    public InhouseContainer setLastSolvent(String lastSolvent) {
        this.lastSolvent = lastSolvent;
        return this;
    }

    public Integer getMolProcId() {
        return molProcId;
    }

    public InhouseContainer setMolProcId(Integer compoundCorrelationId) {
        this.molProcId = compoundCorrelationId;
        return this;
    }

    public Integer getOrganismCorrelationId() {
        return organismCorrelationId;
    }

    public InhouseContainer setOrganismCorrelationId(Integer organismCorrelationId) {
        this.organismCorrelationId = organismCorrelationId;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public InhouseContainer setLocation(String location) {
        this.location = location;
        return this;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public InhouseContainer setLocationId(Integer locationId) {
        this.locationId = locationId;
        return this;
    }

    public Integer getRow() {
        return row;
    }

    public InhouseContainer setRow(Integer row) {
        this.row = row;
        return this;
    }

    public Integer getColumn() {
        return column;
    }

    public InhouseContainer setColumn(Integer column) {
        this.column = column;
        return this;
    }
}
