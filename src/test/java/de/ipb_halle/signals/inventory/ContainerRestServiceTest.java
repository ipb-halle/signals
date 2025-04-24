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

package de.ipb_halle.signals.inventory;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClientImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContainerRestServiceTest {
    private static final String answer = "container saved";
    private static final String endpoint = "POST:https://endpoint.somewhere.invalid/inventory/containers";
    private final String TEST_RESOURCE_1 = "ContainerManagerTest001.json";

    @Test
    public void doCreateContainerTest() throws NoSuchFieldException, IllegalAccessException {
        DynEnumManager dynEnumManager = mock(DynEnumManager.class);

        MockRestClient mockRestClient = new MockRestClient();
        mockRestClient.addResponse(endpoint, answer, 201);

        TestBase.prepareRestClients(mockRestClient, endpoint, getClass().getResourceAsStream(TEST_RESOURCE_1), 201);


        //create sample of container
        Container container = new Container();
        container.setId("container:abc");
        container.setName("testContainer");
        container.setLocation(new LocationReference().setId("location:testLocation"));
        container.setUnit(Unit.getUnit("g"));
        container.setMaterial(new MaterialReference().setId("asset:testAsset"));

        Set<Field> fields = new HashSet<>();
        Field field1 = new Field();
        field1.setId("testField1");
        field1.setRequired(true);
        field1.setCalculated(false);
        field1.setReadOnly(false);
        fields.add(field1);

        Field field2 = new Field();
        field2.setId("testField2");
        field2.setRequired(false);
        field2.setCalculated(false);
        field2.setReadOnly(false);
        fields.add(field2);

        container.addFields(fields);

        Set<FieldValue> fieldValues = new HashSet<>();
        FieldValue fieldValue = new FieldValue();
        fieldValue.setEntityId(container.getId());
        fieldValue.setField(field1);
        fieldValue.setFieldId(field1.getId());
        fieldValues.add(fieldValue);

        container.addFieldValues(fieldValues);

        ContainerType containerType = new ContainerType();
        containerType.setId("vial:abc");
        containerType.setName("testVial");

        ContainerRestService containerRestService = new ContainerRestService();

        java.lang.reflect.Field restClientField = ContainerRestService.class.getDeclaredField("restClient");
        restClientField.setAccessible(true);
        restClientField.set(containerRestService, mockRestClient);

        java.lang.reflect.Field dynEnumField = ContainerRestService.class.getDeclaredField("dynEnumManager");
        dynEnumField.setAccessible(true);
        dynEnumField.set(containerRestService, dynEnumManager);

        SignalsConfig mockConfig = mock(SignalsConfig.class);
        when(mockConfig.getBaseUrl()).thenReturn("https://endpoint.somewhere.invalid");
        when(mockConfig.getApiKey()).thenReturn("dummy-api-key");

        java.lang.reflect.Field configFieldInMockClient = RestClientImpl.class.getDeclaredField("signalsConfig");
        configFieldInMockClient.setAccessible(true);
        configFieldInMockClient.set(mockRestClient, mockConfig);

        containerRestService.doCreateContainer(containerType, container);
    }
}
