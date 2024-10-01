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
package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.field.FieldDefinition;
import de.ipb_halle.signals.field.FieldDefinitionDbService;
import de.ipb_halle.signals.rest.MockRestClient;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import jakarta.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(ApplicationComposer.class)
public class ContainerTypeManagerTest {

    private final String TEST_RESOURCE_1 = "ContainerTypeManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/types?page%5Blimit%5D=20&page%5Boffset%5D=0&entityType=container";
    private final String TEST_CONTAINER_TYPE_ID = "b17da130-259d-4009-b99e-49e9352b3b89";
    private final String TEST_CONTAINER_TYPE_NAME = "Bottle";
    private final String TEST_CONTAINER_ATTACHMENT_ID = "7e38bb31-4860-4d6f-b481-e83ba32d27fb";
    private final String TEST_CONTAINER_ATTACHMENT_FILE_NAME = "DefaultImage_Container_Bottle.png";
    private final String TEST_CONTAINER_FIELD_ID = "PE_INV_SYSTEM_Barcode";
    private final String TEST_CONTAINER_FIELD_KEY = "Barcode";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private ContainerTypeManager manager;

    @Module
    @Classes(cdi = true, value = { MockRestClient.class, SignalsConfig.class, 
        Attachment.class, AttachmentDbService.class, 
        FieldDefinition.class, FieldDefinitionDbService.class, 
        ContainerType.class, ContainerTypeEntity.class, 
        ContainerTypeAttachment.class, ContainerTypeAttachmentId.class,
        ContainerTypeFieldDefinition.class, ContainerTypeFieldDefinitionId.class,
        ContainerTypeDbService.class, ContainerTypeManager.class, ContainerTypeRestService.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ ContainerType.class.getName(), 
                ContainerTypeFieldDefinition.class.getName(), FieldDefinition.class.getName(),
                ContainerTypeAttachment.class.getName(), Attachment.class.getName()
            });
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @Before
    public void testSetup() {
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }

    private Attachment getAttachmentById(Set<Attachment> aSet, String id) {
        for (Attachment a : aSet) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    private FieldDefinition getFieldDefinitionById(Set<FieldDefinition> fdSet, String id) {
        for (FieldDefinition fd : fdSet) {
            if (fd.getId().equals(id)) {
                return fd;
            }
        }
        return null;
    }

    @Test
    public void containerTypeManagerTest() {

        List<ContainerType> ctypes = manager.getSnbContainerTypes();
        manager.save(ctypes);

        ContainerType ct = manager.getDbContainerType(TEST_CONTAINER_TYPE_ID);
        assertEquals("ContainerType name mismatch", TEST_CONTAINER_TYPE_NAME, ct.getName());

        // attachments
        Attachment a = getAttachmentById(
                ct.getAttachments(), 
                TEST_CONTAINER_ATTACHMENT_ID);
        assertEquals("Attachment file name matches", TEST_CONTAINER_ATTACHMENT_FILE_NAME, a.getFileName());

        // field definitions
        FieldDefinition fd = getFieldDefinitionById(
                ct.getFieldDefinitions(), 
                TEST_CONTAINER_FIELD_ID);
        assertEquals("Field definition key matches", TEST_CONTAINER_FIELD_KEY, fd.getKey());
    }
}
