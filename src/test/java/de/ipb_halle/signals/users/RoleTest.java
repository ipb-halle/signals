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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


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

        Assertions.assertTrue(roleA.equals(roleB), "roles are equal");
        Assertions.assertTrue(roleA.hashCode() == roleB.hashCode(), "hashCodes are equal");

        Assertions.assertTrue(roleA.isModified(roleB), "isModified() is true");

        Assertions.assertTrue(roleA.equals(roleRefA), "Role equals RoleRef");
        Assertions.assertTrue(roleRefA.equals(roleA), "RoleRef equals Role");

        Set<IRole> setA = new HashSet<> ();
        setA.add(roleRefA);
        Set<IRole> setB = new HashSet<> ();
        setB.add(roleB);
        Assertions.assertTrue(setA.equals(setB), "Sets are equal");
        Assertions.assertFalse(setB.add(roleA), "role is already present in set");
    }
}
