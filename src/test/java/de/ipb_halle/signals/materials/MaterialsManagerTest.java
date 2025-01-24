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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.attachment.*;
import de.ipb_halle.signals.config.LocalConfig;
import de.ipb_halle.signals.config.LocalConfigDbService;
import de.ipb_halle.signals.dynEnum.DynEnum;
import de.ipb_halle.signals.dynEnum.DynEnumDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.field.*;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.storage.StorageService;
import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class MaterialsManagerTest {

    private final String TEST_RESOURCE_1 = "LibraryManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/materials/libraries";
    private final String TEST_LIBRARY_ID = "6215104dab0ad27bf79429ff";
    private final String TEST_LIBRARY_NAME = "Reagents (SNB)";
    private final int TEST_LIBRARY_ASSET_FIELD_COUNT = 12;
    private final String TEST_LIBRARY_ASSET_FIELD_ID = "6215104dab0ad27bf79429f7";
    private final String TEST_LIBRARY_ASSET_FIELD_TITLE = "Chemical Name";


    @Inject
    private DynEnumManager dynEnumMgr;

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private LibraryDbService dbService;
    
    @Inject
    private MaterialsManager manager;

    @Module
    @Classes(cdi = true, value = {MockRestClient.class, SignalsConfig.class,
            LocalConfig.class, LocalConfigDbService.class, AttachmentRestService.class,
            AttachmentDbService.class, StorageService.class, AttachmentEntity.class,
            AttachmentRevision.class, AttachmentFile.class,
            FieldDefinition.class, FieldDbService.class, FieldParser.class,
            FieldValueEntity.class, SignalsEntityRestService.class, SignalsEntityDbService.class,
            DynEnum.class, DynEnumDbService.class, DynEnumManager.class, MaterialProcessorBean.class,
            Library.class, LibraryEntity.class, LibraryField.class, EmbeddedKeyValue.class, StorageService.class,
            Material.class, MaterialEntity.class, MaterialDbService.class, MaterialRestService.class,
            LibraryDbService.class, MaterialsManager.class, LibraryRestService.class})
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ LibraryEntity.class.getName(),  LibraryField.class.getName(),
                LocalConfig.class.getName(),AttachmentEntity.class.getName(),
                AttachmentRevision.class.getName(), AttachmentFile.class.getName(),
            FieldDefinition.class.getName(), DynEnum.class.getName(), FieldType.class.getName(), MaterialEntity.class.getName() });
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @Before
    public void testSetup() {
        dynEnumMgr.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
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
    public void libraryManagerTest() {

        RuntimeConfig config = new RuntimeConfig();
        manager.manageLibraries(config);

        Library lib = dbService.loadById(TEST_LIBRARY_ID);
        assertEquals("Library name mismatch", TEST_LIBRARY_NAME, lib.getName());

        /*
        // field definitions
        assertEquals("Asset field count matches", TEST_LIBRARY_ASSET_FIELD_COUNT, 
                lib.getAssetFieldDefinitions().size());

        FieldDefinition fd = getFieldDefinitionById(
                lib.getAssetFieldDefinitions(), 
                TEST_LIBRARY_ASSET_FIELD_ID);
        assertEquals("Asset field definition key matches", TEST_LIBRARY_ASSET_FIELD_TITLE, fd.getTitle());
         */
    }
}
