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
import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PostgresqlContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SampleRestServiceTest {


    private final String TEST_RESOURCE_1 = "SampleRestServiceTest001.json";
    private final String TEST_KEY_1 = "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/entities/sample:5af19f39-cd6a-4d65-ab8d-46f586e3c035";
    private final String TEST_ENDPOINT_1 = "/entities/%s";
    private final String TEST_SAMPLE_ID = "sample:5af19f39-cd6a-4d65-ab8d-46f586e3c035";

    @BeforeEach
    public void testSetup() {
        dynEnumManager.allowEnumDiscovery();
        TestBase.prepareRestClients(mockRestClient, TEST_KEY_1, getClass().getResourceAsStream(TEST_RESOURCE_1));
    }

    @Inject
    @DeploymentElement
    private SampleRestService sampleRestService;

    @Inject
    @DeploymentElement(mock = "de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;


    @Test
    public void testFetchSample() throws Exception {
        JsonElement element = sampleRestService.fetchSample(TEST_ENDPOINT_1, TEST_SAMPLE_ID);
        Assertions.assertNotNull(element, "Returned JSON element should not be null");
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

}
