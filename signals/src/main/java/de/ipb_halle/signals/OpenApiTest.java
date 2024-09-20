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

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import de.ipb_halle.signalsAPI.UsersApi;
import de.ipb_halle.signalsModel.UserApiResponse;
import de.ipb_halle.signalsModel.UserApiResponseData;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.client.spring.JaxRsWebClientConfiguration;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the OpenAPI of Signals Notebook
 */
@Stateless
public class OpenApiTest {



    @Resource
    private SignalsConfig signalsConfig;

    private Logger logger = LoggerFactory.getLogger(OpenApiTest.class);

    public void test() {
        /*
        UsersApi api = JAXRSClientFactory.create(signalsConfig.getBaseUrl(), UsersApi.class);
        Client client = WebClient.client(api);
        client.header("X-API-KEY", signalsConfig.getApiKey());
        */
        final Bus defaultBus = BusFactory.getDefaultBus();
        final ConduitInitiatorManager extension = defaultBus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/http", new HTTPTransportFactory());

        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setResourceClass(UsersApi.class);
        bean.setAddress(signalsConfig.getBaseUrl());
        bean.setProvider(new JacksonJsonProvider());
        Map<String, String> headers = new HashMap<> ();
        headers.put("X-API-KEY", signalsConfig.getApiKey());
        bean.setHeaders(headers);
        BindingFactoryManager manager = bean.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(bean.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
        UsersApi api = bean.create(UsersApi.class);
        WebClient client = bean.createWebClient();

        try {
            UserApiResponse response = api.findUserById("103");
            System.out.println(response.getData().toString());
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
