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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;

import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.api.LocalClient;

@LocalBean
public class MockRestClient extends RestClientImpl {

    private static Map<String, String> responseMap = new HashMap<> ();

    public void addResponse(String key, String value) {
        responseMap.put(key, value);
    }

    @Override
    public RestClient execute(int expectedResponseCode) throws IOException, MalformedURLException, UnexpectedResponseCodeException {
        StringJoiner sj = new StringJoiner(":");
        String key = sj.add(getMethod().toString())
                .add(getURL().toString())
                .toString();
        String response = responseMap.get(key);
        if (response == null) {
            throw new NullPointerException("MockRestClient not configured for key: ".concat(key));
        }
        setResponse(response);
        return this;
    }
}
