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
@Table(name = "inhouse_organisms")
public class InhouseOrganism {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "species_script_id")
    private String speciesScriptId;

    @Column(name = " strain_script_id")
    private String strainScriptId;

    @Column
    private String remarks;

    // Getters and Setters


    public Integer getId() {
        return id;
    }

    public InhouseOrganism setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public InhouseOrganism setOrgId(Integer orgId) {
        this.orgId = orgId;
        return this;
    }

    public String getSpeciesScriptId() {
        return speciesScriptId;
    }

    public InhouseOrganism setSpeciesScriptId(String speciesScriptId) {
        this.speciesScriptId = speciesScriptId;
        return this;
    }

    public String getStrainScriptId() {
        return strainScriptId;
    }

    public InhouseOrganism setStrainScriptId(String strainScriptId) {
        this.strainScriptId = strainScriptId;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseOrganism setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    @Override
    public String toString() {
        return "InhouseOrganism{" +
                "id=" + id +
                ", orgId='" + orgId + '\'' +
                ", speciesScriptId='" + speciesScriptId + '\'' +
                ", strainScriptId='" + strainScriptId + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
