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

        //assertNull(entity);
    }
}
