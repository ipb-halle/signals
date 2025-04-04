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

package de.ipb_halle.signals.sample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PostgresqlContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SampleRestServiceTest {


    private final String TEST_RESOURCE_1 = "SampleRestServiceTest001.json";
    private final String TEST_KEY_1 = "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/sample:5af19f39-cd6a-4d65-ab8d-46f586e3c035";
    private final String TEST_ENDPOINT_1 = "/entities/%s";
    private final String TEST_SAMPLE_ID = "sample:5af19f39-cd6a-4d65-ab8d-46f586e3c035";

    @Inject
    @DeploymentElement
    private SampleRestService sampleRestService;

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @BeforeAll
    public void testSetup() {
        TestBase.prepareRestClients(mockRestClient, TEST_KEY_1, getClass().getResourceAsStream(TEST_RESOURCE_1));
        dynEnumManager.allowEnumDiscovery();
    }

    @Test
    public void testFetchSample() throws Exception {
        JsonElement element = sampleRestService.fetchSample(TEST_ENDPOINT_1, TEST_SAMPLE_ID);
        Assertions.assertNotNull(element, "Returned JSON element should not be null");
    }

    @Test
    public void testFetchSample_exception() throws Exception {
        String errorKey = "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/sample:5af19f39-cd6a-4d65-ab8d-46f586e3c035";
        mockRestClient.addResponse(
                errorKey,
                "Error-Payload",
                500
        );

        // Act & Assert
        Assertions.assertThrows(UnexpectedResponseCodeException.class, () -> {
            sampleRestService.fetchSample(TEST_ENDPOINT_1, TEST_SAMPLE_ID);
        });
    }


    @Test
    public void testParseReply() throws Exception {
        JsonElement element = sampleRestService.fetchSample(TEST_ENDPOINT_1, TEST_SAMPLE_ID);

        Assertions.assertNotNull(element, "Returned JSON element should not be null");

        Sample sample = sampleRestService.parseReply(element);
        Assertions.assertNotNull(sample, "Parsed sample should not be null");
        Assertions.assertEquals(TEST_SAMPLE_ID, sample.getId(), "Sample ID should match the expected value");

        System.out.println("Fetched Sample: " + sample);
    }

    @Test
    public void testGetSampleAncestors() throws Exception {
        // prepare mocks
        TestBase.prepareRestClients(mockRestClient, "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/journal:fe624b25-959e-4359-9ff4-372d396c1392", getClass().getResourceAsStream("SampleAncestor001.json"));
        TestBase.prepareRestClients(mockRestClient, "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/experiment:9c57624e-99c7-44db-afad-e579d5fe32cc", getClass().getResourceAsStream("SampleAncestor002.json"));
        TestBase.prepareRestClients(mockRestClient, "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/samplesContainer:28268246-f154-42fb-8348-cd76ad356f19", getClass().getResourceAsStream("SampleAncestor003.json"));

        JsonElement element = sampleRestService.fetchSample(TEST_ENDPOINT_1, TEST_SAMPLE_ID);
        Sample sample = sampleRestService.parseReply(element);

        JsonObject relationships = element.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);
        sampleRestService.getSampleAncestors(sample, relationships);

        Assertions.assertFalse(sample.getAncestors().isEmpty(), "Ancestors should be loaded");
        Assertions.assertEquals(sample.getAncestors().size(), 3);
    }

    @Test
    public void testGetSampleChildren() throws Exception {
        // prepare mocks
        TestBase.prepareRestClients(mockRestClient, "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/text:e4f69432-2c96-4139-ae4e-84cc24d7a9ac", getClass().getResourceAsStream("SampleChild001.json"));

        JsonElement element = sampleRestService.fetchSample(TEST_ENDPOINT_1, TEST_SAMPLE_ID);
        Sample sample = sampleRestService.parseReply(element);

        JsonObject relationships = element.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);
        sampleRestService.getSampleChildren(sample, relationships);

        Assertions.assertFalse(sample.getChildren().isEmpty(), "Child should be loaded");
        Assertions.assertEquals(sample.getChildren().size(), 1);
    }
}
