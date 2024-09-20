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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

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

        assertTrue("groups are equal", groupA.equals(groupB));
        assertTrue("hashCodes are equal", groupA.hashCode() == groupB.hashCode());

        groupA.setName("OtherName");
        assertTrue("name has been modified", groupA.isModified(CompareType.SNB, groupB));
        assertTrue("groups are equal", groupA.equals(groupB));

        groupA.setName("TestGroup");
        groupA.setDescription("other description");
        assertTrue("description has been modified", groupA.isModified(CompareType.SNB, groupB));
    
        groupA.setDescription("Description of TestGroup");
        groupA.setId("2");
        assertFalse("Change of id is no modification", groupA.isModified(CompareType.SNB, groupB));
        assertFalse("groups are NOT equal", groupA.equals(groupB));
        assertFalse("hashCodes do NOT match", groupA.hashCode() == groupB.hashCode());
    }
}
