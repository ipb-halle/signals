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

import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class GroupTest {

    private Group createGroup(String id, String name, String description) {
        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setDescription(description);
        group.setCreatedAt(new Date());
        group.setEditedAt(new Date());
        group.setSystem(true);
        return group;
    }


    @Test
    public void groupTest() {
        Group groupA = createGroup("1", "TestGroup", "Description of TestGroup");
        Group groupB = createGroup("1", "TestGroup", "Description of TestGroup");

        Assertions.assertTrue(groupA.equals(groupB),"groups are equal");
        Assertions.assertTrue(groupA.hashCode() == groupB.hashCode(), "hashCodes are equal");

        groupA.setName("OtherName");
        Assertions.assertTrue(groupA.isModified(CompareType.SNB, groupB), "name has been modified");
        Assertions.assertTrue(groupA.equals(groupB), "groups are equal");

        groupA.setName("TestGroup");
        groupA.setDescription("other description");
        Assertions.assertTrue(groupA.isModified(CompareType.SNB, groupB), "description has been modified");

        groupA.setDescription("Description of TestGroup");
        groupA.setId("2");
        Assertions.assertFalse(groupA.isModified(CompareType.SNB, groupB), "Change of id is no modification");
        Assertions.assertFalse(groupA.equals(groupB), "groups are NOT equal");
        Assertions.assertFalse(groupA.hashCode() == groupB.hashCode(), "hashCodes do NOT match");
    }
}
