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
package de.ipb_halle.signals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;


public class SignalsEntityTest {

    private final String TEST_RESOURCE = "SignalsEntityTest001.json";
    private final String TEST_ID = "location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt";

    @Test
    public void entityTest() {

        String test = TestBase.readStream(
                    getClass().getResourceAsStream(TEST_RESOURCE));
        JsonElement j = JsonParser.parseString(test);
        SignalsEntityRestService svc = new SignalsEntityRestService ();
        SignalsEntity entity = svc.createEntity(j);

        assertEquals("id matches", TEST_ID, entity.getId());
        assertEquals("json string matches", test, entity.getJsonString());
    }
}
