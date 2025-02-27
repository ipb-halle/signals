/*
 *
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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

package de.ipb_halle.signals.attachment;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestReply;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AttachmentRestServiceTest {

    public final static String TEST_RESOURCE = "/de/ipb_halle/signals/attachment/testfile.txt";
    public final static String TEST_METHOD = "GET:";
    public final static String TEST_ENDPOINT = "/mock/endpoint";
    public final static String TEST_DIGEST = "dcf7aa16156bf23e5bf99becd47407ee6d4f388ad236ea10ba4708baba9431ff";
    public final static long TEST_SIZE = 63;

    @Resource
    private SignalsConfig signalsConfig;

    @Inject
    @DeploymentElement(mock="de.ipb_halle.signals.rest.MockRestClient")
    private MockRestClient mockRestClient;

    @Inject
    @DeploymentElement
    AttachmentRestService attachmentRestService;

    @Test
    public void testFetch() {
        mockRestClient.addResponse(TEST_METHOD
                + signalsConfig.getBaseUrl()
                + TEST_ENDPOINT, TEST_RESOURCE);
        RestReply reply = attachmentRestService.fetchAttachment(TEST_ENDPOINT, RestClient.TEXT_PLAIN);
        Assertions.assertTrue(TEST_DIGEST.equals(reply.getDigest()));
        Assertions.assertEquals(TEST_SIZE, reply.getFileSize());
    }
}
