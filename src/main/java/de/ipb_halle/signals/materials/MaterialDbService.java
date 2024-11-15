/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.FieldValue;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Stateless
public class MaterialDbService {

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    @Inject
    private FieldDbService fieldDbService;

    private Logger logger = LoggerFactory.getLogger(LibraryDbService.class);

    public Material loadById(String id) {
        MaterialEntity entity = this.em.find(MaterialEntity.class, id);
        Material mat = new Material(entity);
        mat.addAllSynonyms(loadSynonyms(id));
        mat.addAllFieldValues(loadFieldValues(id));
        return mat;
    }

    private List<FieldValue> loadFieldValues(String id) {
        return new ArrayList<FieldValue> ();
    }

    private List<Synonym> loadSynonyms(String id) {
        return new ArrayList<Synonym>();
    }

    public void save(Material mat) {
        MaterialEntity entity = mat.createEntity();
        this.em.merge(entity);
        saveSynonyms(mat.getSynonyms());
        // fieldDbService.save(mat.getFieldValues());
    }

    private void saveSynonyms(Collection<Synonym> synonyms) {
        for (Synonym synonym : synonyms) {
            this.em.merge(synonym);
        }
    }
}
