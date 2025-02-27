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
package de.ipb_halle.signals.attachment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.rest.*;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;


/**
 * service for attachments (not yet a real REST service)
 */

@Local
public class AttachmentRestService {

    private Logger logger = LoggerFactory.getLogger(AttachmentRestService.class);

    @Inject
    private RestClient restClient;

    /**
     * Actually do a REST call to obtain a single attachment
     *
     * @param endpoint    the specific endpoint with id
     * @param contentType MIME type of the attachment
     * @return path of the received attachment in the staging area
     */
    public RestReply fetchAttachment(String endpoint, String contentType) {
        try {
            restClient.reset()
                    .setMethod(Method.GET)
                    .setContentType(contentType)
                    .setResponseType(RestClient.RestType.STREAM)
                    .setEndpoint(endpoint)
                    .execute();
            return restClient.getResponse();
        } catch (UnexpectedResponseCodeException e) {
            // attachment (drawing, image, sequence) may not be available
            logger.debug("Unexpected response code {}", restClient.getResponseCode(), e);
        } catch (IOException e) {
            logger.warn("caught IOException: {}", e.getMessage(), e);
        } catch (URISyntaxException e) {
            logger.warn("URISyntaxException: {}", e.getMessage(), e);
        }
        return null;
    }
}
