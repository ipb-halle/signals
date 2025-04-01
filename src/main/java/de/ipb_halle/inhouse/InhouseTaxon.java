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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "inhouse_taxonomy")
public class InhouseTaxon {

    public final static String TAXONOMY_CLASS = "class";
    public final static String TAXONOMY_FAMILY = "family";
    public final static String TAXONOMY_SPECIES = "species";
    public final static String TAXONOMY_STRAIN = "strain";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column(name="inhouse_id")
    private Integer inhouseId;

    @Column(name = "inhouse_parent_id")
    private Integer inhouseParentId;        // parent id from inhouseDB

    @Column(name = "organism_id")
    private Integer organismId;

    @Column
    private String parent;                  // parent material from signals

    @Column
    private String level;

    @Column
    private String name;

    private transient Set<Synonym> synonyms;

    public InhouseTaxon() {
        synonyms = new HashSet<>();
    }

    public InhouseTaxon addSynonyms(Collection<InhouseSynonym> synonyms) {
        for(InhouseSynonym ics : synonyms) {
            this.synonyms.add(new Synonym("", ics.getSynonym()));
        }
        return this;
    }

    public Integer getId() {
        return id;
    }

    public InhouseTaxon setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseTaxon setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public Integer getInhouseParentId() {
        return inhouseParentId;
    }

    public InhouseTaxon setInhouseParentId(Integer inhouseParentId) {
        this.inhouseParentId = inhouseParentId;
        return this;
    }

    public Integer getInhouseId() {
        return inhouseId;
    }

    public InhouseTaxon setInhouseId(Integer inhouseId) {
        this.inhouseId = inhouseId;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public InhouseTaxon setLevel(String level) {
        this.level = level;
        return this;
    }

    public Integer getOrganismId() {
        return organismId;
    }

    public InhouseTaxon setOrganismId(Integer organismId) {
        this.organismId = organismId;
        return this;
    }


    public Set<Synonym> getSynonyms() {
        return synonyms;
    }

    public String getParent() {
        return parent;
    }

    public InhouseTaxon setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getName() {
        return name;
    }

    public InhouseTaxon setName(String name) {
        this.name = name;
        return this;
    }
}
