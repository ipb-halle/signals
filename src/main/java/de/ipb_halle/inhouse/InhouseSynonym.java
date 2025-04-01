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
@Table(name = "inhouse_synonyms")
public class InhouseSynonym {

    public final static String SYNONYM_COMPOUND = "compound";
    public final static String SYNONYM_ORGANISM = "organism";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "inhouse_id")
    private Integer inhouseId;

    @Column
    private String type;

    @Column
    private String synonym;

    public Integer getId() {
        return id;
    }

    public InhouseSynonym setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getInhouseId() {
        return inhouseId;
    }

    public InhouseSynonym setInhouseId(Integer inhouseId) {
        this.inhouseId = inhouseId;
        return this;
    }

    public String getSynonym() {
        return synonym;
    }

    public InhouseSynonym setSynonym(String synonym) {
        this.synonym = synonym;
        return this;
    }

    public String getType() {
        return type;
    }

    public InhouseSynonym setType(String type) {
        this.type = type;
        return this;
    }
}
