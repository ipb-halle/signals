/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.inhouse;

import jakarta.persistence.*;

@Entity
@Table(name = "inhouse_extract")
public class InhouseExtract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column(name = "extract_id")
    private Integer extractId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "last_solvent")
    private String lastSolvent;

    @Column(name = "storage_place")
    private String storagePlace;

    @Column(name = "extract_code")
    private String extractCode;

    @Column
    private String tara;

    @Column
    private Double amount;

    @Column
    private Double volume;

    @Column
    private Double concentration;

    @Column
    private boolean solution;

    @Column
    private String remarks;

    @Column
    private String hplc;

    @Column (name = "extract_plate_id")
    private String extractPlateId;

    @Column (name ="extract_position")
    private String extractPosition;

    @Column(name ="extract_barcode")
    private String extractBarcode;

    @Column(name ="ipb_code")
    private String ipbCode;

    // Getter and Setter
    public Integer getId() {
        return id;
    }

    public InhouseExtract setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseExtract setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public Integer getExtractId() {
        return extractId;
    }

    public InhouseExtract setExtractId(Integer extractId) {
        this.extractId = extractId;
        return this;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public InhouseExtract setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public String getLastSolvent() {
        return lastSolvent;
    }

    public InhouseExtract setLastSolvent(String lastSolvent) {
        this.lastSolvent = lastSolvent;
        return this;
    }

    public String getStoragePlace() {
        return storagePlace;
    }

    public InhouseExtract setStoragePlace(String storagePlace) {
        this.storagePlace = storagePlace;
        return this;
    }

    public String getExtractCode() {
        return extractCode;
    }

    public InhouseExtract setExtractCode(String extractCode) {
        this.extractCode = extractCode;
        return this;
    }

    public String getTara() {
        return tara;
    }

    public InhouseExtract setTara(String tara) {
        this.tara = tara;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public InhouseExtract setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Double getVolume() {
        return volume;
    }

    public InhouseExtract setVolume(Double volume) {
        this.volume = volume;
        return this;
    }

    public Double getConcentration() {
        return concentration;
    }

    public InhouseExtract setConcentration(Double concentration) {
        this.concentration = concentration;
        return this;
    }

    public boolean isSolution() {
        return solution;
    }

    public InhouseExtract setSolution(boolean solution) {
        this.solution = solution;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseExtract setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getHplc() {
        return hplc;
    }

    public InhouseExtract setHplc(String hplc) {
        this.hplc = hplc;
        return this;
    }

    public String getExtractPlateId() {
        return extractPlateId;
    }

    public InhouseExtract setExtractPlateId(String extractPlateId) {
        this.extractPlateId = extractPlateId;
        return this;
    }

    public String getExtractPosition() {
        return extractPosition;
    }

    public InhouseExtract setExtractPosition(String extractPosition) {
        this.extractPosition = extractPosition;
        return this;
    }

    public String getExtractBarcode() {
        return extractBarcode;
    }

    public InhouseExtract setExtractBarcode(String extractBarcode) {
        this.extractBarcode = extractBarcode;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public InhouseExtract setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    @Override
    public String toString() {
        return "InhouseExtract{" +
                "id=" + id +
                ", eid='" + eid + '\'' +
                ", extractId=" + extractId +
                ", correlationId='" + correlationId + '\'' +
                ", lastSolvent='" + lastSolvent + '\'' +
                ", storagePlace='" + storagePlace + '\'' +
                ", extractCode='" + extractCode + '\'' +
                ", tara='" + tara + '\'' +
                ", amount=" + amount +
                ", volume=" + volume +
                ", concentration=" + concentration +
                ", solution=" + solution +
                ", remarks='" + remarks + '\'' +
                ", hplc='" + hplc + '\'' +
                ", extractPlateId='" + extractPlateId + '\'' +
                ", extractPosition='" + extractPosition + '\'' +
                ", extractBarcode='" + extractBarcode + '\'' +
                ", ipbCode='" + ipbCode + '\'' +
                '}';
    }
}
