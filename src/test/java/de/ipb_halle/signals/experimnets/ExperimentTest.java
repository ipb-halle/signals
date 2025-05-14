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

package de.ipb_halle.signals.experimnets;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.experiments.Experiment;
import de.ipb_halle.signals.experiments.ExperimentEntity;
import de.ipb_halle.signals.experiments.ExperimentProperty;
import de.ipb_halle.signals.experiments.ExperimentPropertyValue;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExperimentTest {

    @Test
    public void testSettersAndGetters() {
        Experiment experiment = new Experiment();
        Date now = new Date();
        IUser user = mock(IUser.class);

        experiment.setId("exp:001");
        experiment.setName("Test experiment");
        experiment.setDescription("A sample experiment");
        experiment.setCreatedAt(now);
        experiment.setEditedAt(now);
        experiment.setCreatedBy(user);
        experiment.setEditedBy(user);
        experiment.setOwner(user);
        experiment.setDigest(123L);
        experiment.setAncestorId("ancestor:123");
        experiment.setTemplateId("template:123");

        assertEquals("exp:001", experiment.getId());
        assertEquals("Test experiment", experiment.getName());
        assertEquals("A sample experiment", experiment.getDescription());
        assertEquals(now, experiment.getCreatedAt());
        assertEquals(now, experiment.getEditedAt());
        assertEquals(user, experiment.getCreatedBy());
        assertEquals(user, experiment.getEditedBy());
        assertEquals(user, experiment.getOwner());
        assertEquals(123L, experiment.getDigest());
        assertEquals("ancestor:123", experiment.getAncestorId());
        assertEquals("template:123", experiment.getTemplateId());
    }

    @Test
    public void testConstructorWithEntity() {
        DynEnumManager dynEnumManager = mock(DynEnumManager.class);

        EntityType mockEntityType = mock(EntityType.class);
        when(mockEntityType.getId()).thenReturn(2);

        when(dynEnumManager.valueOf(2)).thenReturn(mockEntityType);

        ExperimentEntity entity = new ExperimentEntity()
                .setId("exp:002")
                .setName("Entity Name")
                .setDescription("Entity Desc")
                .setType(2)
                .setCreatedAt(new Date())
                .setEditedAt(new Date())
                .setCreatedBy("u1")
                .setEditedBy("u1")
                .setOwner("u1")
                .setDigest(456L)
                .setAncestorId("ancestor:002")
                .setTemplateId("template:002");

        Experiment experiment = new Experiment(entity, dynEnumManager);

        assertEquals("exp:002", experiment.getId());
        assertEquals("Entity Name", experiment.getName());
        assertEquals(mockEntityType, experiment.getType());
        assertEquals("Entity Desc", experiment.getDescription());
        assertEquals("u1", experiment.getCreatedBy().getId());
        assertEquals("u1", experiment.getEditedBy().getId());
        assertEquals("u1", experiment.getOwner().getId());
        assertEquals(456L, experiment.getDigest());
        assertEquals("ancestor:002", experiment.getAncestorId());
        assertEquals("template:002", experiment.getTemplateId());
    }

    @Test
    public void testCreateEntity() {
        EntityType mockEntityType = mock(EntityType.class);
        when(mockEntityType.getId()).thenReturn(2);

        Experiment experiment = new Experiment();
        experiment.setId("exp:003");
        experiment.setName("Test");
        experiment.setDescription("Create entity");
        experiment.setType(mockEntityType);
        experiment.setCreatedAt(new Date());
        experiment.setEditedAt(new Date());
        experiment.setCreatedBy(new UserReference("u1"));
        experiment.setEditedBy(new UserReference("u1"));
        experiment.setOwner(new UserReference("u1"));
        experiment.setDigest(789L);
        experiment.setAncestorId("ancestor:003");
        experiment.setTemplateId("template:003");

        ExperimentEntity entity = experiment.createEntity();

        assertEquals("exp:003", entity.getId());
        assertEquals(2, entity.getType());
    }

    @Test
    public void testCollectionsAdd() {
        Experiment experiment = new Experiment();
        SignalsEntity ancestor = mock(SignalsEntity.class);
        SignalsEntity child = mock(SignalsEntity.class);
        ExperimentProperty prop = mock(ExperimentProperty.class);
        ExperimentPropertyValue value = mock(ExperimentPropertyValue.class);

        experiment.addAncestor(ancestor);
        experiment.addChild(child);
        experiment.addProperty(prop);
        experiment.addPropertyValue(value);

        assertTrue(experiment.getAncestors().contains(ancestor));
        assertTrue(experiment.getChildren().contains(child));
        assertTrue(experiment.getProperties().contains(prop));
        assertTrue(experiment.getPropertyValues().contains(value));
    }

    @Test
    public void testSetAndGetAncestors() {
        Experiment experiment = new Experiment();

        SignalsEntity ancestor1 = mock(SignalsEntity.class);
        SignalsEntity ancestor2 = mock(SignalsEntity.class);

        Set<SignalsEntity> ancestors = new HashSet<>();
        ancestors.add(ancestor1);
        ancestors.add(ancestor2);

        experiment.setAncestors(ancestors);

        assertEquals(2, experiment.getAncestors().size());
        assertTrue(experiment.getAncestors().contains(ancestor1));
        assertTrue(experiment.getAncestors().contains(ancestor2));
    }

    @Test
    public void testSetAndGetChildren() {
        Experiment experiment = new Experiment();

        SignalsEntity child1 = mock(SignalsEntity.class);
        SignalsEntity child2 = mock(SignalsEntity.class);

        Set<SignalsEntity> children = new HashSet<>();
        children.add(child1);
        children.add(child2);

        experiment.setChildren(children);

        assertEquals(2, experiment.getChildren().size());
        assertTrue(experiment.getChildren().contains(child1));
        assertTrue(experiment.getChildren().contains(child2));
    }

    @Test
    public void testSetAndGetProperties() {
        Experiment experiment = new Experiment();

        ExperimentProperty prop1 = mock(ExperimentProperty.class);
        ExperimentProperty prop2 = mock(ExperimentProperty.class);

        Set<ExperimentProperty> props = new HashSet<>();
        props.add(prop1);
        props.add(prop2);

        experiment.setProperties(props);

        assertEquals(2, experiment.getProperties().size());
        assertTrue(experiment.getProperties().contains(prop1));
        assertTrue(experiment.getProperties().contains(prop2));
    }

    @Test
    public void testSetAndGetPropertyValues() {
        Experiment experiment = new Experiment();

        ExperimentPropertyValue val1 = mock(ExperimentPropertyValue.class);
        ExperimentPropertyValue val2 = mock(ExperimentPropertyValue.class);

        Set<ExperimentPropertyValue> values = new HashSet<>();
        values.add(val1);
        values.add(val2);

        experiment.setPropertyValues(values);

        assertEquals(2, experiment.getPropertyValues().size());
        assertTrue(experiment.getPropertyValues().contains(val1));
        assertTrue(experiment.getPropertyValues().contains(val2));
    }
}
