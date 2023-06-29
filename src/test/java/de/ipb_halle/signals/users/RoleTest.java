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

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public class RoleTest {

    private Role createRole(String id, String name, String description) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        return role;
    }


    @Test
    public void roleTest() {
        Role roleA = createRole("1", "TestRole", "Description of TestRole");
        Role roleB = createRole("1", "TestRole", "Description of TestRole");
        IRole roleRefA = new RoleReference().setId("1");

        roleA.addPrivilege("canShare");
        roleB.addPrivilege("canArchive");

        assertTrue("roles are equal", roleA.equals(roleB));
        assertTrue("hashCodes are equal", roleA.hashCode() == roleB.hashCode());

        assertTrue("isModified() is true", roleA.isModified(roleB));

        assertTrue("Role equals RoleRef", roleA.equals(roleRefA));
        assertTrue("RoleRef equals Role", roleRefA.equals(roleA));

        Set<IRole> setA = new HashSet<> ();
        setA.add(roleRefA);
        Set<IRole> setB = new HashSet<> ();
        setB.add(roleB);
        assertTrue("Sets are equal", setA.equals(setB));
        assertFalse("role is already present in set", setB.add(roleA));
    }
}
