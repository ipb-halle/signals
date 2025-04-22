package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.users.UserReference;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PostgresqlContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SampleTest {

    @Inject
    @DeploymentElement
    public DynEnumManager dynEnumMgr;

    @Test
    public void testSettersAndGetters() {
        dynEnumMgr.allowEnumDiscovery();


        Sample sample = new Sample();
        sample.setId("sample:123");
        sample.setName("Test Sample");
        sample.setDescription("Desc");
        sample.setTemplateId("template:abc");
        sample.setAncestorId("journal:xyz");
        sample.setType((EntityType) dynEnumMgr.valueOf(EntityType.valueOf("sample")));
        sample.setDigest(123456L);
        sample.setCreatedAt(new Date());
        sample.setEditedAt(new Date());

        UserReference user = new UserReference("user:1");
        sample.setCreatedBy(user);
        sample.setEditedBy(user);
        sample.setOwner(user);

        assertEquals("sample:123", sample.getId());
        assertEquals("Test Sample", sample.getName());
        assertEquals("Desc", sample.getDescription());
        assertEquals("template:abc", sample.getTemplateId());
        assertEquals("journal:xyz", sample.getAncestorId());
        assertEquals((EntityType) dynEnumMgr.valueOf(EntityType.valueOf("sample")), sample.getType());
        assertEquals(123456L, sample.getDigest());
        assertEquals(user, sample.getCreatedBy());
        assertEquals(user, sample.getEditedBy());
        assertEquals(user, sample.getOwner());
    }

    @Test
    public void testCollections() {
        Sample sample = new Sample();
        SampleProperty prop = new SampleProperty();
        SamplePropertyValue val = new SamplePropertyValue();

        sample.addProperty(prop);
        sample.addPropertyValue(val);
        sample.addAncestor(new SignalsEntity());
        sample.addChild(new SignalsEntity());

        assertEquals(1, sample.getProperties().size());
        assertEquals(1, sample.getPropertyValues().size());
        assertEquals(1, sample.getAncestors().size());
        assertEquals(1, sample.getChildren().size());

        sample.setProperties(Set.of(prop));
        sample.addPropertyValue(val);
        sample.setAncestors(Set.of(new SignalsEntity()));
        sample.setChildren(Set.of(new SignalsEntity()));

        assertEquals(1, sample.getProperties().size());
        assertEquals(1, sample.getChildren().size());
    }

    @Test
    public void testCreateEntityReturnsNullOrValid() {
        dynEnumMgr.allowEnumDiscovery();


        Sample sample = new Sample();
        sample.setId("sample:123");
        sample.setName("Test Sample");
        sample.setDescription("Desc");
        sample.setTemplateId("template:abc");
        sample.setAncestorId("journal:xyz");
        sample.setType((EntityType) dynEnumMgr.valueOf(EntityType.valueOf("sample")));
        sample.setDigest(123456L);
        sample.setCreatedAt(new Date());
        sample.setEditedAt(new Date());

        UserReference user = new UserReference("user:1");
        sample.setCreatedBy(user);
        sample.setEditedBy(user);
        sample.setOwner(user);

        SampleEntity entity = sample.createEntity();
        assertNotNull(entity);
    }

    @Test
    public void testSampleConstructorFromEntity() {
        dynEnumMgr.allowEnumDiscovery();

        SampleEntity entity = new SampleEntity();
        entity.setId("sample:456");
        entity.setName("Constructed Sample");
        entity.setDescription("From Entity");
        entity.setType(dynEnumMgr.valueOf(EntityType.valueOf("sample")).getId());
        entity.setCreatedAt(new Date());
        entity.setEditedAt(new Date());
        entity.setDigest(789L);
        entity.setCreatedBy("user:creator");
        entity.setEditedBy("user:editor");
        entity.setOwner("user:owner");
        entity.setAncestorId("ancestor:abc");
        entity.setParentContainerId("container:xyz");
        entity.setStoicRefId("stoic:eid");
        entity.setStoicRefRowId("stoic:row");
        entity.setTemplateId("template:999");

        Sample sample = new Sample(entity, dynEnumMgr);

        assertEquals("sample:456", sample.getId());
        assertEquals("Constructed Sample", sample.getName());
        assertEquals("From Entity", sample.getDescription());
        assertEquals(7, sample.getType().getId());
        assertEquals("ancestor:abc", sample.getAncestorId());
        assertEquals("container:xyz", sample.getParentContainerId());
        assertEquals("template:999", sample.getTemplateId());
        assertEquals(789L, sample.getDigest());

        assertNotNull(sample.getCreatedBy());
        assertNotNull(sample.getEditedBy());
        assertNotNull(sample.getOwner());
        assertNotNull(sample.getCreatedAt());
        assertNotNull(sample.getEditedAt());

        assertNotNull(sample.getStoicRef());
        assertEquals("stoic:eid", sample.getStoicRef().getEid());
        assertEquals("stoic:row", sample.getStoicRef().getRowId());

        assertTrue(sample.getProperties().isEmpty());
        assertTrue(sample.getPropertyValues().isEmpty());
        assertTrue(sample.getAncestors().isEmpty());
        assertTrue(sample.getChildren().isEmpty());
    }

}
