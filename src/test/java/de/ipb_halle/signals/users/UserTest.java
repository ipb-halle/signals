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
package de.ipb_halle.signals.users;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.ipb_halle.signals.TestBase;
import java.text.DateFormat;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;


public class UserTest {

    private final String TEST_RESOURCE = "UserTest001.json";
    private final int id = 116;
    private final String userName = "user.two@someplace.invalid";
    private final long created = 1654850485098L;
    private final long lastLogin = 1660721583522L;

    @Test
    public void userTest() {

        String test = TestBase.readStream(
                    getClass().getResourceAsStream(TEST_RESOURCE));
        JsonElement j = JsonParser.parseString(test);
        User user = User.createUser(j);

        assertEquals("id matches", id, (int) user.getId());
        assertEquals("createdAt date matches", created, user.getCreatedAt().getTime());
        assertEquals("lastLoginAt date matches", lastLogin, user.getLastLoginAt().getTime());
        assertEquals("userName matches", userName, user.getUserName());
    }
}
