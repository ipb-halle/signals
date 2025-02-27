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

package de.ipb_halle.signals.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmbeddedKeyValueTest {

    @Test
    public void testEmbeddedKeyValue() {
        EmbeddedKeyValue e1 = new EmbeddedKeyValue("A","B");
        EmbeddedKeyValue e2 = new EmbeddedKeyValue("A", "B");
        Assertions.assertTrue(e1.hashCode() == e2.hashCode());
        Assertions.assertTrue(e1.equals(e1));
        Assertions.assertTrue(e1.equals(e2));
        Assertions.assertTrue(e2.equals(e1));
        Assertions.assertFalse(e1.equals("Hallo"));
        Assertions.assertFalse(e1.equals(null));
        e2.setValue("C");
        Assertions.assertFalse(e1.equals(e2));
        e2.setValue("B");
        Assertions.assertTrue(e1.equals(e2));
        e2.setId("D");
        Assertions.assertFalse(e1.equals(e2));
    }
}
