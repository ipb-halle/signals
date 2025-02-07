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
package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.persistence.*;

import java.io.Serializable;


/**
 * Quality (measure) - field assignments
 */

@Entity
@Table(name="signalsentities_children")
public class SignalsEntityChild implements Serializable {

    private final static long serialVersionUID = 1L;

    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "signals_entity_id")),
            @AttributeOverride(name = "value", column = @Column(name = "child_id"))
    })
    @EmbeddedId
    private EmbeddedKeyValue id;

    /**
     * default constructor
     */
    public SignalsEntityChild() {
        id = new EmbeddedKeyValue();
    }

    public SignalsEntityChild(String entity, String child) {
        id = new EmbeddedKeyValue(entity, child);
    }

    public String getSignalsEntityId() {
        return id.getId();
    }

    public String getChildId() {
        return id.getValue();
    }

    public SignalsEntityChild setSignalsEntityId(String e) {
        id.setId(e);
        return this;
    }

    public SignalsEntityChild setChildId(String i) {
        id.setValue(i);
        return this;
    }
}
