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

@Entity
@Table(name = "inhouse_experiments")
public class InhouseExperiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column
    private String threelc;

    @Column(name="code")
    private String individualCode;

    @Column
    private String journal;

    @Column(name = "proc_id")
    private int procId;

    @Column
    private String remarks;

    public Integer getId() {
        return id;
    }

    public InhouseExperiment setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseExperiment setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public String getThreelc() {
        return threelc;
    }

    public InhouseExperiment setThreelc(String threelc) {
        this.threelc = threelc;
        return this;
    }

    public String getIndividualCode() {
        return individualCode;
    }

    public InhouseExperiment setIndividualCode(String individualCode) {
        this.individualCode = individualCode;
        return this;
    }

    public String getJournal() {
        return journal;
    }

    public InhouseExperiment setJournal(String journal) {
        this.journal = journal;
        return this;
    }

    public int getProcId() {
        return procId;
    }

    public InhouseExperiment setProcId(int procId) {
        this.procId = procId;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseExperiment setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }
}
