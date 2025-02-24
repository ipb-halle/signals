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

package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "location_type_fields")
public class LocationTypeField {

    @EmbeddedId
    private EmbeddedKeyValue id;

    public LocationTypeField() {
        id = new EmbeddedKeyValue();
    }

    public LocationTypeField(String lt, String f) {
        id = new EmbeddedKeyValue(lt, f);
    }

    public String getFieldId() {
        return id.getValue();
    }

    public String getLocationTypeId() {
        return id.getId();
    }

    public LocationTypeField setFieldId(String f) {
        id.setValue(f);
        return this;
    }

    public LocationTypeField setLocationTypeId(String lt) {
        id.setId(lt);
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LocationTypeField that = (LocationTypeField) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
