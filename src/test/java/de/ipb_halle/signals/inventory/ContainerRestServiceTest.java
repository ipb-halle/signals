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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.Unit;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.materials.MaterialReference;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClientImpl;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestReply;
import de.ipb_halle.signals.sample.SampleProcessorBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContainerRestServiceTest {
    private static final String answer = "container saved";
    private static final String endpoint = "POST:https://endpoint.somewhere.invalid/inventory/containers";
    private final String TEST_RESOURCE_1 = "ContainerManagerTest001.json";

    @InjectMocks
    private ContainerRestService containerRestService;

    @Mock
    private SampleProcessorBean sampleProcessorBean;


    @Test
    public void doCreateContainerTest() throws NoSuchFieldException, IllegalAccessException {
        DynEnumManager dynEnumManager = mock(DynEnumManager.class);
        MockRestClient mockRestClient = new MockRestClient();
        mockRestClient.addResponse(endpoint, answer, 201);
        TestBase.prepareRestClients(mockRestClient, endpoint, getClass().getResourceAsStream(TEST_RESOURCE_1), 201);

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


        java.lang.reflect.Field restClientField = ContainerRestService.class.getDeclaredField("restClient");
        restClientField.setAccessible(true);
        restClientField.set(containerRestService, mockRestClient);

        java.lang.reflect.Field dynEnumField = ContainerRestService.class.getDeclaredField("dynEnumManager");
        dynEnumField.setAccessible(true);
        dynEnumField.set(containerRestService, dynEnumManager);

        SignalsConfig mockConfig = mock(SignalsConfig.class);
        when(mockConfig.getBaseUrl()).thenReturn("https://endpoint.somewhere.invalid");

        java.lang.reflect.Field configFieldInMockClient = RestClientImpl.class.getDeclaredField("signalsConfig");
        configFieldInMockClient.setAccessible(true);
        configFieldInMockClient.set(mockRestClient, mockConfig);

        containerRestService.doCreateContainer(containerType, container);
    }

    @Test
    public void parseContainerContentsTest_Sample() {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();

        jsonObject.add(ContainerEntity.ATTR_CONTENT_TYPE, new JsonPrimitive("sample"));
        jsonObject.add(ContainerEntity.ATTR_CONTENT_ID, new JsonPrimitive("sample:e9435f5a-7e55-450d-957b-2f8d9710eb18"));

        jsonArray.add(jsonObject);

        containerRestService.parseReply(buildMockedJson(jsonArray));

        verify(sampleProcessorBean, times(1)).processSingleSample("sample:e9435f5a-7e55-450d-957b-2f8d9710eb18");
    }

    @Test
    public void parseContainerContentsTest_Asset() {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();

        jsonObject.add(ContainerEntity.ATTR_CONTENT_TYPE, new JsonPrimitive("asset"));
        jsonObject.add(ContainerEntity.ATTR_CONTENT_ID, new JsonPrimitive("asset:test"));

        jsonArray.add(jsonObject);

        Container result = containerRestService.parseReply(buildMockedJson(jsonArray));

        assertNotNull(result);
        assertNotNull(result.getMaterial());
        assertEquals("asset:test", result.getMaterial().getId());
    }

    private JsonElement buildMockedJson(JsonArray containerContents) {
        JsonObject container = new JsonObject();
        JsonObject attributes = new JsonObject();

        attributes.add(ContainerEntity.ATTR_CONTENTS, containerContents);
        attributes.addProperty(RestHelper.ATTR_NAME, "TestContainer");

        attributes.add(RestHelper.ATTR_UNIT, new JsonPrimitive("g"));
        attributes.add(RestHelper.ATTR_FIELDS, new JsonArray());

        container.add(RestHelper.ATTR_ID, new JsonPrimitive("test-id"));
        container.add(RestHelper.ATTR_ATTRIBUTES, attributes);

        JsonObject dataWrapper = new JsonObject();
        dataWrapper.add(RestHelper.ATTR_DATA, container);
        return dataWrapper.get(RestHelper.ATTR_DATA);
    }

    @Test
    public void doGetContainerAttachmentTest() throws Exception {
        AttachmentRestService attachmentRestService = mock(AttachmentRestService.class);
        ContainerRestService containerRestService = new ContainerRestService();

        java.lang.reflect.Field attachmentField = ContainerRestService.class.getDeclaredField("attachmentRestService");
        attachmentField.setAccessible(true);
        attachmentField.set(containerRestService, attachmentRestService);

        Container container = new Container();
        container.setId("container:abc");

        Field field = new Field();
        field.setId("field:testField");

        String mimeType = "application/pdf";
        Path dummyPath = Path.of("/tmp/fake.pdf");
        RestReply expectedReply = new RestReply(dummyPath, "dummy-digest", mimeType);

        when(attachmentRestService.fetchAttachment(
                contains("/containers/container:abc/fields/"), eq(mimeType))
        ).thenReturn(expectedReply);

        RestReply reply = containerRestService.doGetContainerAttachment(container, field, mimeType);

        assertNotNull(reply);
        assertEquals(dummyPath, reply.getPath());
        assertEquals(mimeType, reply.getMimeType());
    }

    @Test
    public void test_parseAttachmentMimeType_shouldReturnMimeType() {
        ContainerRestService service = new ContainerRestService();

        String jsonValue = "{\"attachment\": {\"mimeType\": \"application/pdf\"}}";

        FieldValue fieldValue = new FieldValue();
        fieldValue.setValue(jsonValue);

        String mimeType = service.parseAttachmentMimeType(fieldValue);

        assertEquals("application/pdf", mimeType);
    }

    @Test
    public void test_parseAttachmentRevisionInfo_shouldFillAttachmentRevisionCorrectly() {
        ContainerRestService service = new ContainerRestService();

        String jsonValue = """
        {
            "attachment": {
                "filename": "certificate.pdf",
                "mimeType": "application/pdf",
                "size": 21568
            }
        }
        """;

        FieldValue fieldValue = new FieldValue();
        fieldValue.setValue(jsonValue);

        AttachmentRevision revision = new AttachmentRevision();

        service.parseAttachmentRevisionInfo(revision, fieldValue);

        assertEquals("certificate.pdf", revision.getOriginalName());
        assertEquals("application/pdf", revision.getMimeType());
        assertEquals(21568L, revision.getSize());
    }


}
