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

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class LocationTypeManagerTest {

    private final String TEST_RESOURCE_1 = "LocationTypeManagerTest001.json";
    private final String TEST_KEY_1 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/inventory/types?page%5Blimit%5D=20&page%5Boffset%5D=0&entityType=location";
    private final String TEST_LOCATION_TYPE_ID = "location:017929f3-cd0e-466c-ac94-c21ce5fe8c31:ivt";
    private final String TEST_LOCATION_TYPE_NAME = "Cabinet";
    private final String TEST_LOCATION_FIELD_ID = "PE_INV_SYSTEM_Barcode:location:017929f3-cd0e-466c-ac94-c21ce5fe8c31:ivt";
    private final String TEST_LOCATION_FIELD_KEY = "Barcode";

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private LocationTypeManager locationTypeManager;


    @Inject
    @DeploymentElement
    private LocationTypeDbService locationTypeDbService;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @BeforeAll
    public void testSetup() {
        dynEnumManager.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient, TEST_KEY_1, getClass().getResourceAsStream(TEST_RESOURCE_1));
    }

    private Field getFieldById(Set<Field> fieldSet, String id) {
        for (Field f : fieldSet) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    @Test
    public void locationTypeManagerTest() {

        List<LocationType> ltypes = locationTypeManager.getSnbLocationTypes();
        locationTypeManager.save(ltypes);

        LocationType lt = locationTypeManager.getDbLocationType(TEST_LOCATION_TYPE_ID);
        Assertions.assertEquals(TEST_LOCATION_TYPE_NAME, lt.getName(), "LocationType name mismatch");

        // field definitions
        Field f = getFieldById(lt.getFields(), TEST_LOCATION_FIELD_ID);
        Assertions.assertEquals(TEST_LOCATION_FIELD_KEY, f.getKey(), "Field definition key matches");
    }
}
