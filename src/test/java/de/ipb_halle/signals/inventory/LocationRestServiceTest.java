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
import com.google.gson.JsonObject;
import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.attachment.AttachmentRevision;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldParser;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestReply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationRestServiceTest {

    @InjectMocks
    private LocationRestService locationRestService;

    @Mock
    private AttachmentRestService attachmentRestService;

    @Mock
    private RestClient restClient;

    @Mock
    private DynEnumManager dynEnumManager;

    @Mock
    private FieldParser fieldParser;

    @Test
    public void testDoGetLocationAttachment_shouldCallAttachmentRestService() {
        Location location = new Location();
        location.setId("location:abc");

        Field field = new Field();
        field.setId("field:123");

        RestReply dummyReply = new RestReply(Path.of("/tmp/file.pdf"), "digest123", "application/pdf");

        when(attachmentRestService.fetchAttachment(contains("/locations/location:abc/fields/"), eq("application/pdf")))
                .thenReturn(dummyReply);

        RestReply result = locationRestService.doGetLocationAttachment(location, field, "application/pdf");

        assertNotNull(result);
        assertEquals("digest123", result.getDigest());
    }

    @Test
    public void testParseAttachmentMimeType_shouldReturnCorrectMimeType() {
        String json = """
                   {
                       "attachment": {
                           "mimeType": "image/png"
                       }
                   }
                """;
        FieldValue fieldValue = new FieldValue();
        fieldValue.setValue(json);

        String result = locationRestService.parseAttachmentMimeType(fieldValue);

        assertEquals("image/png", result);
    }

    @Test
    public void testParseAttachmentRevisionInfo_shouldExtractFieldCorrectly() {
        String json = """
                {
                  "attachment": {
                    "fileName": "test.png",
                    "mimeType": "image/png",
                    "fileSize": 12345
                  }
                }
                """;

        FieldValue fieldValue = new FieldValue();
        fieldValue.setValue(json);

        AttachmentRevision revision = new AttachmentRevision();
        locationRestService.parseAttachmentRevisionInfo(revision, fieldValue);

        assertEquals("test.png", revision.getOriginalName());
        assertEquals("image/png", revision.getMimeType());
        assertEquals(12345L, revision.getSize());
    }

    @Test
    public void testParseAncestor_shouldSetAncestorCorrectly() {
        JsonArray ancestors = new JsonArray();
        JsonObject ancestor = new JsonObject();
        ancestor.addProperty("id", "id123");
        ancestor.addProperty("name", "Ancient Box");
        ancestors.add(ancestor);

        Location loc = new Location();
        locationRestService.parseAncestor(ancestors, loc);

        assertEquals("id123", loc.getAncestorId());
        assertEquals("Ancient Box", loc.getAncestorName());
    }

    @Test
    public void testParseAncestor_shouldSetNullIfEmpty() {
        JsonArray emptyArray = new JsonArray();
        Location loc = new Location();

        locationRestService.parseAncestor(emptyArray, loc);

        assertNull(loc.getAncestorId());
        assertNull(loc.getAncestorName());
    }

    @Test
    public void testDoCreateLocation_shouldSendCorrectRequest() throws Exception {
        LocationType type = new LocationType().setId("locationType:xyz");
        Location location = new Location();
        location.setName("TestLocation");
        location.setDescription("For testing");

        when(restClient.reset()).thenReturn(restClient);
        when(restClient.setMethod(Method.POST)).thenReturn(restClient);
        when(restClient.setEndpoint(anyString())).thenReturn(restClient);
        when(restClient.setRequestData(anyString())).thenReturn(restClient);
        when(restClient.execute(eq(RestClient.HTTP_CREATED))).thenReturn(restClient);

        locationRestService.doCreateLocation(type, location);

        verify(restClient).execute(RestClient.HTTP_CREATED);
    }

    @Test
    public void testPrepareFields_shouldIncludeOnlyRelevantFields() {
        Field requiredField = new Field();
        requiredField.setId("required:123");
        requiredField.setRequired(true);
        requiredField.setCalculated(false);
        requiredField.setReadOnly(false);

        Field optionalEditableField = new Field();
        optionalEditableField.setId("optional:456");
        optionalEditableField.setRequired(false);
        optionalEditableField.setCalculated(false);
        optionalEditableField.setReadOnly(false);

        Field readOnlyField = new Field();
        readOnlyField.setId("readonly:789");
        readOnlyField.setRequired(false);
        readOnlyField.setCalculated(false);
        readOnlyField.setReadOnly(true);

        // Setup field values
        FieldValue fv1 = new FieldValue();
        fv1.setField(requiredField);
        fv1.setFieldId("required:123");
        fv1.setValue("ValueA");

        FieldValue fv2 = new FieldValue();
        fv2.setField(optionalEditableField);
        fv2.setFieldId("optional:456");
        fv2.setValue("ValueB");

        FieldValue fv3 = new FieldValue();
        fv3.setField(readOnlyField);
        fv3.setFieldId("readonly:789");
        fv3.setValue("SHOULD_NOT_APPEAR");

        Location location = new Location();
        location.addFieldValues(Set.of(fv1, fv2, fv3));

        JsonArray fieldsArray = (JsonArray) locationRestService.prepareFields(location);

        assertEquals(2, fieldsArray.size(), "Only two field values should be included");

        JsonObject first;
        JsonObject second;

        if (fieldsArray.get(0).getAsJsonObject().get("id").getAsString().equalsIgnoreCase("optional")) {
            first = fieldsArray.get(1).getAsJsonObject();
            second = fieldsArray.get(0).getAsJsonObject();

        }else {
            first = fieldsArray.get(0).getAsJsonObject();
            second = fieldsArray.get(1).getAsJsonObject();

        }

        assertEquals("optional", second.get("id").getAsString());
        assertEquals("ValueB", second.get("content").getAsJsonObject().get("value").getAsString());

        assertEquals("required", first.get("id").getAsString());
        assertEquals("ValueA", first.get("content").getAsJsonObject().get("value").getAsString());
    }

}
