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
import de.ipb_halle.signals.entity.FieldDefinition;
import de.ipb_halle.signals.entity.FieldDefinitionDbService;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import jakarta.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(CdiTestRunner.class)
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
    private RestClient mockRestClient;

    @Inject
    private LibraryManager manager;

    @Before
    public void testSetup() {
        TestBase.prepareRestClients((MockRestClient) mockRestClient,
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
