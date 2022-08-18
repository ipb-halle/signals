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

    private final String test = "{\"type\":\"entity\",\"id\":\"location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt\",\"links\":{\"self\":"
            + "\"https://ipb-halle-trial.signalsnotebook.perkinelmercloud.eu/api/rest/v1.0/entities/location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt\"}"
            + ",\"attributes\":{\"id\":\"location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt\",\"eid\":\"location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt\""
            + ",\"name\":\"Bin\",\"description\":\"\",\"createdAt\":\"2022-02-22T16:33:39.417Z\",\"editedAt\":\"2022-07-16T17:00:07.828Z\",\"type\":"
            + "\"location\",\"digest\":\"49106222\",\"fields\":{\"Description\":{\"value\":\"\"},\"Inventory Location Library\":{\"value\":\"Bin\"}"
            + ",\"Inventory Security\":{\"value\":\"\"},\"Name\":{\"value\":\"Bin\"}},\"flags\":{\"canEdit\":true}},\"relationships\":{\"created By\":"
            + "{\"links\":{\"self\":\"https://ipb-halle-trial.signalsnotebook.perkinelmercloud.eu/api/rest/v1.0/users/3\"},\"data\":{\"type\":\"user\","
            + "\"id\":\"3\"}},\"editedBy\":{\"links\":{\"self\":\"https://ipb-halle-trial.signalsnotebook.perkinelmercloud.eu/api/rest/v1.0/users/3\"},"
            + "\"data\":{\"type\":\"user\",\"id\":\"3\"}},\"owner\":{\"links\":{\"self\":"
            + "\"https://ipb-halle-trial.signalsnotebook.perkinelmercloud.eu/api/rest/v1.0/users/3\"},\"data\":{\"type\":\"user\",\"id\":\"3\"}},"
            + "\"source\":{\"links\":{\"self\":"
            + "\"https://ipb-halle-trial.signalsnotebook.perkinelmercloud.eu/api/rest/v1.0/entities/location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt/export\"}}}}";

    private final String id = "location:44ab8051-81fe-4f48-b251-8f629a89ddf4:ivt";

    @Test
    public void entityTest() {

        JsonElement j = JsonParser.parseString(test);
        SignalsEntity entity = new SignalsEntity(j);

        assertEquals("id matches", id, entity.getId());
        assertEquals("json string matches", test, entity.getJsonString());
    }
}
