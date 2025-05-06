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

import de.ipb_halle.signals.field.FieldValue;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerEntityTest {

    @Test
    public void testDump(){
        ContainerEntity entity = new ContainerEntity();
        entity.setId("container:123");
        entity.setName("MyContainer");
        String dump = entity.dump();
        assertTrue(dump.contains("container:123"));
        assertTrue(dump.contains("MyContainer"));
    }

    @Test
    public void testGetDigest() {
        ContainerEntity entity = new ContainerEntity();
        entity.setDigest("digest-value");
        assertEquals("digest-value", entity.getDigest());
    }

    @Test
    public void testSetFieldValues() {
        ContainerEntity entity = new ContainerEntity();
        FieldValue fv = new FieldValue().setValue("val");
        Set<FieldValue> set = new HashSet<>();
        set.add(fv);
        entity.setFieldValues(set);
        assertEquals(1, entity.getFieldValues().size());
        assertEquals("val", entity.getFieldValues().iterator().next().getValue());
    }

    @Test
    public void testToString() {
        ContainerEntity entity = new ContainerEntity();
        entity.setId("container:abc")
                .setAmount(42.0)
                .setBarcode("BARCODE123")
                .setContainerTypeId("type:xyz")
                .setCoordinateX(1)
                .setCoordinateY(2)
                .setCreatedAt(new Date(100000))
                .setCreatedBy("user:1")
                .setDigest("digest123")
                .setLocationId("location:1")
                .setName("TestContainer")
                .setUnit("mg")
                .setUpdatedAt(new Date(200000))
                .setUpdatedBy("user:2")
                .setContainerTypeName("Vial");

        String result = entity.toString();
        assertTrue(result.contains("container:abc"));
        assertTrue(result.contains("BARCODE123"));
        assertTrue(result.contains("digest123"));
        assertTrue(result.contains("TestContainer"));
        assertTrue(result.contains("Vial"));
    }
}
