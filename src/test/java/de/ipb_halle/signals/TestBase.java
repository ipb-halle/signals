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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Properties;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.api.LocalClient;


public class TestBase {

    /*
     * Obtain a EJB context and bind-inject a bean to it
     * @param bean the bean 
     * @return the context
     */
    public static Context getTestContext(Object bean) {
        try {

            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.put("openejb.configuration", TestBase.class.getResource("/test-openejb.xml").getFile());

            EJBContainer container = EJBContainer.createEJBContainer(properties);
            Context ctx = container.getContext();

            ctx.bind("inject", bean);
            return ctx;

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * add test data to the resultMap of the RestClientFactory.
     * The RestClientFactory loads this map into the MockRestClients
     * to enable them to return meaningful test data.
     * @param factory the singleton RestClientFactory
     * @param urlKey the concatenated HTTP method and the request url 
     * @param stream an InputStream obtained by Class.getResourceAsStream() 
     * and holding the expected test data
     */
    public static void prepareRestClients(RestClientFactory factory, String urlKey, InputStream stream) {
        factory.addResponse(urlKey, readStream(stream));
    }

    /**
     * @param stream the InputStream as obtained from Class.getResourceAsStream()
     * @return the stream content 
     */
    public static String readStream(InputStream stream) {
        StringBuilder sb = new StringBuilder();

        try (InputStreamReader streamReader = new InputStreamReader(stream);
                BufferedReader bufReader = new BufferedReader(streamReader)) {
            String line = bufReader.readLine();
            while (line != null) {
                sb.append(line);
                line = bufReader.readLine();
            }
        } catch(IOException e) {
            return "";
        }
        return sb.toString();
    }
}
