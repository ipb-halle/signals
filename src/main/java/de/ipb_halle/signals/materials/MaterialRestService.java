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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestService;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;

import java.io.IOException;
import java.net.URISyntaxException;

import jakarta.ejb.Local;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * REST service for materials
 */

@Local
public class MaterialRestService implements RestService<Material> {

    public final String MATERIAL_ENDPOINT = "/materials/%s";

    @Inject
    private RestClient restClient;

    private Logger logger = LoggerFactory.getLogger(MaterialRestService.class);

    public Material createEntity(JsonElement json) {
        Material mat = new Material();
        JsonObject j = json.getAsJsonObject();
        JsonObject attributes  = j.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        mat.setId(RestHelper.parseString(j, RestHelper.ATTR_ID));

        return mat;
    }
    
    private JsonElement fetch(String id) {
        JsonElement jsonResult;
        try {
            restClient.setMethod(Method.GET)
                .setEndpoint(String.format(MATERIAL_ENDPOINT, id))
                .execute();

            jsonResult = JsonParser.parseString(restClient.getResponse());
            return jsonResult.getAsJsonObject().getAsJsonObject(RestHelper.ATTR_DATA);

        } catch(UnexpectedResponseCodeException ue) {
            logger.warn("Unexpected code");
        } catch(URISyntaxException me) {
            logger.warn("Malformed URL");
        } catch(IOException ioe) {
            logger.warn("IOException", (Throwable) ioe);
        }
        return null; 
    }

    public Material doGetMaterial(String id) {
        return createEntity(fetch(id));
    }
}
