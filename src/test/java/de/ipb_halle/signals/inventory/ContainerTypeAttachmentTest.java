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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerTypeAttachmentTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        ContainerTypeAttachment cta = new ContainerTypeAttachment()
                .setAttachmentId("attachment:test123")
                .setContainerTypeId("type:vial");

        Assertions.assertEquals("attachment:test123", cta.getAttachmentId());
        Assertions.assertEquals("type:vial", cta.getContainerTypeId());
    }

    @Test
    public void testParameterizedConstructor() {
        ContainerTypeAttachment cta = new ContainerTypeAttachment("type:box", "attachment:file001");

        Assertions.assertEquals("type:box", cta.getContainerTypeId());
        Assertions.assertEquals("attachment:file001", cta.getAttachmentId());
    }

    @Test
    public void testEqualsAndHashCode() {
        ContainerTypeAttachment cta1 = new ContainerTypeAttachment("type:box", "attachment:file001");
        ContainerTypeAttachment cta2 = new ContainerTypeAttachment("type:box", "attachment:file001");
        ContainerTypeAttachment cta3 = new ContainerTypeAttachment("type:box", "attachment:file002");
        ContainerTypeAttachment cta4 = null;

        Assertions.assertEquals(cta1, cta2, "Objects with same values must be equal");
        Assertions.assertEquals(cta1.hashCode(), cta2.hashCode(), "Hash codes must be equal for equal objects");
        Assertions.assertNotEquals(cta1, cta3, "Objects with different values must not be equal");
        Assertions.assertNotEquals(cta1, cta4, "Object must not equal null");
        Assertions.assertNotEquals(cta1, "some string", "Object must not equal different class");
    }
}
