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

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.UpdateConfig;
import de.ipb_halle.signals.TestBase;
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
public class LibraryManagerTest {

    private final String TEST_RESOURCE_1 = "LibraryManagerTest001.json";
    private final String TEST_KEY_1 = 
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/materials/libraries";
    private final String TEST_LIBRARY_ID = "6215104dab0ad27bf79429ff";
    private final String TEST_LIBRARY_NAME = "Reagents (SNB)";
    private final int TEST_LIBRARY_ASSET_FIELD_COUNT = 12;
    private final String TEST_LIBRARY_ASSET_FIELD_ID = "6215104dab0ad27bf79429f7";
    private final String TEST_LIBRARY_ASSET_FIELD_TITLE = "Chemical Name";

    @Inject
    private MockRestClient mockRestClient;

    @Inject
    private LibraryManager manager;

    @Module
    @Classes(cdi = true, value = { MockRestClient.class, SignalsConfig.class, 
        FieldDefinition.class, FieldDefinitionDbService.class, 
        Library.class, LibraryEntity.class, 
        LibraryFieldDefinition.class, LibraryFieldDefinitionId.class,
        LibraryDbService.class, LibraryManager.class, LibraryRestService.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ Library.class.getName(), LibraryFieldDefinition.class.getName(), FieldDefinition.class.getName() });
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

        UpdateConfig config = new UpdateConfig();
        List<Library> libraries = manager.getSnbLibraries();
        manager.save(config, libraries);

        Library lib = manager.getDbLibrary(TEST_LIBRARY_ID);
        assertEquals("Library name mismatch", TEST_LIBRARY_NAME, lib.getName());

        // field definitions
        assertEquals("Asset field count matches", TEST_LIBRARY_ASSET_FIELD_COUNT, 
                lib.getAssetFieldDefinitions().size());

        FieldDefinition fd = getFieldDefinitionById(
                lib.getAssetFieldDefinitions(), 
                TEST_LIBRARY_ASSET_FIELD_ID);
        assertEquals("Asset field definition key matches", TEST_LIBRARY_ASSET_FIELD_TITLE, fd.getTitle());
    }
}
