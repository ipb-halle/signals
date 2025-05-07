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

import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocationEntityTest {

    @Test
    public void testDump_shouldReturnFormattedString() {
        LocationEntity entity = new LocationEntity();
        entity.setId("location:123");
        entity.setName("Freezer 1");

        String expected = "Location(location:123): name=Freezer 1\n";
        assertEquals(expected, entity.dump());
    }

    @Test
    public void testSetType_shouldSetAllFields() {
        LocationType type = new LocationType();
        type.setId("type:abc");
        type.setName("Shelf");

        LocationEntity entity = new LocationEntity();
        entity.setType(type);

        assertEquals("type:abc", entity.getTypeId());
        assertEquals("Shelf", entity.getTypeName());
    }

    @Test
    public void testSetFieldValues_shouldAddFieldValues() {
        FieldValue fv1 = new FieldValue();
        fv1.setFieldId("field:1");
        fv1.setValue("value1");
        fv1.setField(new Field());

        FieldValue fv2 = new FieldValue();
        fv2.setFieldId("field:2");
        fv2.setValue("value2");
        fv2.setField(new Field());

        Set<FieldValue> set = new HashSet<>();
        set.add(fv1);
        set.add(fv2);

        LocationEntity entity = new LocationEntity();
        entity.setFieldValues(set);

        assertEquals(2, entity.getFieldValues().size());
        assertTrue(entity.getFieldValues().contains(fv1));
        assertTrue(entity.getFieldValues().contains(fv2));
    }
}
