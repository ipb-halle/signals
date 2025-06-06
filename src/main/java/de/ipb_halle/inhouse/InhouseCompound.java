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

import de.ipb_halle.signals.materials.Synonym;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "inhouse_compounds")
public class InhouseCompound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column(name = "mol_id")
    private Integer molId;

    @Column
    private String casrn;

    @Column
    private String remarks;

    @Column(name = "ipb_code")
    private String ipbCode;

    @Column
    private String name;

    private transient Set<Synonym> synonyms;

    public InhouseCompound() {
        synonyms = new HashSet<>();
    }

    public void addSynonyms(Collection<InhouseSynonym> synonyms) {
        for (InhouseSynonym ics : synonyms) {
            this.synonyms.add(new Synonym("", ics.getSynonym()));
        }
    }

    public Set<Synonym> getSynonyms() {
        return synonyms;
    }

    public String getEid() {
        return eid;
    }

    public InhouseCompound setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public InhouseCompound setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getMolId() {
        return molId;
    }

    public InhouseCompound setMolId(Integer molId) {
        this.molId = molId;
        return this;
    }

    public String getCasrn() {
        return casrn;
    }

    public InhouseCompound setCasrn(String casrn) {
        this.casrn = casrn;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseCompound setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public InhouseCompound setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    public String getName() {
        return name;
    }

    public InhouseCompound setName(String name) {
        this.name = name;
        return this;
    }


}
