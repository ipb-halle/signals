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

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class MaterialsManagerTest {

    private final String TEST_RESOURCE_1 = "LibraryManagerTest001.json";
    private final String TEST_KEY_1 =
        "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/materials/libraries";
    private final String TEST_LIBRARY_ID = "assetType:6215104dab0ad27bf79429ff";
    private final String TEST_LIBRARY_NAME = "Reagents (SNB)";
    private final int TEST_LIBRARY_ASSET_FIELD_COUNT = 12;
    private final String TEST_LIBRARY_ASSET_FIELD_ID = "6215104dab0ad27bf79429f7";
    private final String TEST_LIBRARY_ASSET_FIELD_TITLE = "Chemical Name";


    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumMgr;

    @Inject
    @DeploymentElement(mock="de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private LibraryDbService dbService;

    @Inject
    @DeploymentElement
    private MaterialsManager manager;

    @BeforeAll
    public void testSetup() {
        dynEnumMgr.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient,
            TEST_KEY_1,
            getClass().getResourceAsStream(TEST_RESOURCE_1));
    }

    private Field getFieldById(Set<Field> fdSet, String id) {
        for (Field fd : fdSet) {
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
        Assertions.assertEquals(TEST_LIBRARY_NAME, lib.getName(), "Library name mismatch");


        // field definitions
        Assertions.assertEquals(TEST_LIBRARY_ASSET_FIELD_COUNT,
                lib.getAssetFields().size(),
                "Asset field count matches");

        Field fd = getFieldById(
                lib.getAssetFields(),
                TEST_LIBRARY_ASSET_FIELD_ID);
        Assertions.assertEquals(TEST_LIBRARY_ASSET_FIELD_TITLE,
                fd.getTitle(), "Asset field definition key matches");
    }
}
