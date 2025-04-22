package de.ipb_halle.signals.sample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(PostgresqlContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SampleRestServiceTest {

    private static final String TEST_SAMPLE_ID = "sample:5af19f39-cd6a-4d65-ab8d-46f586e3c035";
    private static final String TEST_RESOURCE_1 = "SampleRestServiceTest001.json";
    private static final String TEST_KEY_ENTITY = "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/" + TEST_SAMPLE_ID;
    private static final String TEST_KEY_PROPERTIES = "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/samples/" + TEST_SAMPLE_ID + "/properties";
    private static final String POST_SAMPLE_KEY = "POST:https://endpoint.somewhere.invalid/api/rest/v1.0/entities?force=true";

    @Inject
    @DeploymentElement
    private SampleRestService sampleRestService;

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @BeforeEach
    public void setup() {
        assumeTrue(mockRestClient != null, "MockRestClient was not injected. Test skipped.");
        TestBase.prepareRestClients(mockRestClient, TEST_KEY_ENTITY, getClass().getResourceAsStream(TEST_RESOURCE_1));
        dynEnumManager.allowEnumDiscovery();
    }


    @Test
    public void testDoGetSample() throws Exception {
        prepareAncestorsAndChildren();
        Sample sample = sampleRestService.doGetSample(TEST_SAMPLE_ID);
        assertEquals(TEST_SAMPLE_ID, sample.getId());
        assertEquals(3, sample.getAncestors().size());
        assertEquals(1, sample.getChildren().size());
    }

    @Test
    public void testDoGetSampleProperties() throws Exception {
        Sample sample = sampleRestService.doGetSample(TEST_SAMPLE_ID);
        TestBase.prepareRestClients(mockRestClient, TEST_KEY_PROPERTIES, getClass().getResourceAsStream("SamplePropertiesJson001.json"));
        sampleRestService.doGetSampleProperties(sample);

        assertFalse(sample.getProperties().isEmpty());
        assertEquals(20, sample.getProperties().size());
    }

    @Test
    public void testFetchSampleSuccess() throws Exception {
        JsonElement element = sampleRestService.fetchSample("/entities/%s", TEST_SAMPLE_ID);
        assertNotNull(element);
    }

    @Test
    public void testFetchSampleThrowsUnexpectedResponseCodeException() {
        mockRestClient.addResponse(TEST_KEY_ENTITY, "Error", 500);
        assertThrows(UnexpectedResponseCodeException.class, () ->
                sampleRestService.fetchSample("/entities/%s", TEST_SAMPLE_ID));
    }

    @Test
    public void testFetchSampleThrowsIOException() {
        assertThrows(IOException.class, () ->
                sampleRestService.fetchSample("/entities/%s", "non-existent"));
    }

    @Test
    public void testFetchSampleThrowsURISyntaxException() {
        String invalidEndpoint = "GET:https://[invalidUrl]";
        mockRestClient.addResponse(invalidEndpoint, "", 500);
        assertThrows(URISyntaxException.class, () ->
                sampleRestService.fetchSample(invalidEndpoint, TEST_SAMPLE_ID));
    }

    @Test
    public void testParseReply() throws Exception {
        JsonElement element = sampleRestService.fetchSample("/entities/%s", TEST_SAMPLE_ID);
        Sample sample = sampleRestService.parseReply(element);
        assertEquals(TEST_SAMPLE_ID, sample.getId());
    }

    @Test
    public void testCreateNewSample() throws Exception {
        Sample sample = TestBase.createSampleWithProperties(TEST_SAMPLE_ID);
        mockRestClient.addResponse(POST_SAMPLE_KEY, "", 201);
        sampleRestService.createNewSample(sample);
    }

    @Test
    public void testPrivatePrepareSampleMethod() throws Exception {
        Sample sample = TestBase.createSampleWithProperties(TEST_SAMPLE_ID);
        Method method = SampleRestService.class.getDeclaredMethod("prepareSample", Sample.class);
        method.setAccessible(true);
        JsonObject json = (JsonObject) method.invoke(sampleRestService, sample);
        assertNotNull(json);
        assertTrue(json.has("data"));
    }

    private void prepareAncestorsAndChildren() {
        TestBase.prepareRestClients(mockRestClient,
                "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/journal:fe624b25-959e-4359-9ff4-372d396c1392",
                getClass().getResourceAsStream("SampleAncestor001.json"));
        TestBase.prepareRestClients(mockRestClient,
                "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/experiment:9c57624e-99c7-44db-afad-e579d5fe32cc",
                getClass().getResourceAsStream("SampleAncestor002.json"));
        TestBase.prepareRestClients(mockRestClient,
                "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/samplesContainer:28268246-f154-42fb-8348-cd76ad356f19",
                getClass().getResourceAsStream("SampleAncestor003.json"));
        TestBase.prepareRestClients(mockRestClient,
                "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/text:e4f69432-2c96-4139-ae4e-84cc24d7a9ac",
                getClass().getResourceAsStream("SampleChild001.json"));
    }
}
