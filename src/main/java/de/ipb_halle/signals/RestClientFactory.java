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
package de.ipb_halle.signals;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Singleton;

/** 
 * Factory for RestClients
 */
@Singleton
public class RestClientFactory {

    @Resource(name="signalsConfig")
    private SignalsConfig signalsConfig;

    private Map<String, String> responseMap;

    /**
     * constructor
     */
    public RestClientFactory() {
        responseMap = new HashMap<> ();
    }

    /**
     * to be used during testing
     */
    public void addResponse(String key, String value) {
        responseMap.put(key, value);
    }

    public RestClient getRestClient() {
        if (signalsConfig.getRestClientClassName() == null) {
            return new RestClient()
                .setApiKey(signalsConfig.getApiKey())
                .setBaseUrl(signalsConfig.getBaseUrl());
        }
        try {
            RestClient client = ((Class<RestClient>) Class.forName(
                        signalsConfig.getRestClientClassName())).newInstance();
            return client
                .setApiKey(signalsConfig.getApiKey())
                .setBaseUrl(signalsConfig.getBaseUrl())
                .setResponseMap(responseMap);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
