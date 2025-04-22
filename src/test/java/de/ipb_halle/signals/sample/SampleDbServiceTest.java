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

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.users.UserReference;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PostgresqlContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SampleDbServiceTest {

    @Inject
    @DeploymentElement
    private SampleDbService sampleDbService;

    @Inject
    @DeploymentElement
    public DynEnumManager dynEnumMgr;

    private Sample sample;

    @BeforeEach
    public void setUp() {
        dynEnumMgr.allowEnumDiscovery();
        sample = new Sample();
        sample.setId("sample:test-id");
        sample.setName("Test Sample");
        sample.setType((EntityType) dynEnumMgr.valueOf(EntityType.valueOf("sample")));
        sample.setDescription("Just testing");
        sample.setTemplateId("template:123");
        sample.setAncestorId("ancestor:xyz");
        sample.setCreatedAt(new Date());
        sample.setEditedAt(new Date());
        sample.setDigest(123456L);
        UserReference user = new UserReference("user:2");
        sample.setCreatedBy(user);
        sample.setEditedBy(user);
        sample.setOwner(user);

        SampleProperty prop = new SampleProperty();
        prop.setPropertyId("prop1");
        prop.setPropertyName("TestProp");
        prop.setPropertyType("STRING");

        SamplePropertyValue val = new SamplePropertyValue();
        val.setPropertyId("prop1");
        val.setSampleId("sample:test-id");
        val.setPropertyValue("42");

        sample.addProperty(prop);
        sample.addPropertyValue(val);
    }

    @Test
    public void testSaveAndLoadSampleEntity() {

        sampleDbService.save(sample);

        SampleEntity loaded = sampleDbService.loadSampleEntityById(sample.getId());
        assertNotNull(loaded);
        assertEquals(sample.getId(), loaded.getId());
    }

    @Test
    public void testLoadSamplePropertyValuesWithProperties() {
        sampleDbService.save(sample);

        Sample newSample = new Sample();
        newSample.setId("sample:test-id");
        newSample.setCreatedAt(new Date());
        newSample.setEditedAt(new Date());
//        UserReference user = new UserReference("user:2");
//        newSample.setCreatedBy(user);
//        newSample.setEditedBy(user);
//        newSample.setOwner(user);

        sampleDbService.loadSamplePropertyValuesWithProperties(newSample);

        assertEquals(1, newSample.getProperties().size());
        assertEquals(1, newSample.getPropertyValues().size());

        SamplePropertyValue value = newSample.getPropertyValues().stream().toList().get(0);
        assertEquals("42", value.getPropertyValue());
    }

}
