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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;


public class UserEntityTest {

    private final String TEST_RESOURCE = "UserEntityTest001.json";
    private final String TEST_ID = "116";
    private final String TEST_USER_NAME = "user.two@someplace.invalid";
    private final long TEST_CREATED = 1654850485098L;
    private final long TEST_LAST_LOGIN = 1660721583522L;

    @Test
    public void userTest() {

        String test = TestBase.readStream(
                    getClass().getResourceAsStream(TEST_RESOURCE));
        JsonElement j = JsonParser.parseString(test);

        UserRestService svc = new UserRestService();
        User user = svc.parseUser(j);
        UserEntity entity = user.createEntity();
        user = new User(entity);

        assertEquals("id matches", TEST_ID, user.getId());
        assertEquals("createdAt date matches", TEST_CREATED, user.getCreatedAt().getTime());
        assertEquals("lastLoginAt date matches", TEST_LAST_LOGIN, user.getLastLoginAt().getTime());
        assertEquals("userName matches", TEST_USER_NAME, user.getUserName());
    }
}
