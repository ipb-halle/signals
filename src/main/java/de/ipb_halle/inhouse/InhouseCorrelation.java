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
@Table(name = "inhouse_correlation")
public class InhouseCorrelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "corr_id")
    private Integer corrId;

    @Column
    private String context;

    @Column(name = "mol_id")
    private Integer molId;

    @Column(name = "proc_id")
    private Integer procedureId;

    @Column(name = "org_id")
    private Integer organismId;

    public Integer getId() {
        return id;
    }

    public InhouseCorrelation setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getContext() {
        return context;
    }

    public InhouseCorrelation setContext(String context) {
        this.context = context;
        return this;
    }

    public Integer getMolId() {
        return molId;
    }

    public InhouseCorrelation setMolId(Integer molId) {
        this.molId = molId;
        return this;
    }

    public Integer getProcedureId() {
        return procedureId;
    }

    public InhouseCorrelation setProcedureId(Integer procedureId) {
        this.procedureId = procedureId;
        return this;
    }

    public Integer getOrganismId() {
        return organismId;
    }

    public InhouseCorrelation setOrganismId(Integer organismId) {
        this.organismId = organismId;
        return this;
    }

    public Integer getCorrId() {
        return corrId;
    }

    public InhouseCorrelation setCorrId(Integer corrId) {
        this.corrId = corrId;
        return this;
    }
}
