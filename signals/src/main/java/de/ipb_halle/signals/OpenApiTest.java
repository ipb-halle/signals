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
import de.ipb_halle.signalsAPI.EntitiesApi;
import de.ipb_halle.signalsAPI.UsersApi;
import de.ipb_halle.signalsModel.EntityApiResponse;
import de.ipb_halle.signalsModel.EntityApiResponseData;
import de.ipb_halle.signalsModel.JsonApiRelationship;
import de.ipb_halle.signalsModel.UserApiResponse;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Test the OpenAPI of Signals Notebook
 */
@Stateless
public class OpenApiTest {


    @Resource
    private SignalsConfig signalsConfig;

    @Inject
    private SignalsEntityDbService dbService;

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    private Logger logger = LoggerFactory.getLogger(OpenApiTest.class);

    public <T> T buildAPI(Class<T> apiClass) {
        final Bus defaultBus = BusFactory.getDefaultBus();
        final ConduitInitiatorManager extension = defaultBus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/http", new HTTPTransportFactory());

        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setResourceClass(apiClass);
        bean.setAddress(signalsConfig.getBaseUrl());
        bean.setProvider(new JacksonJsonProvider());
        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-KEY", signalsConfig.getApiKey());
        bean.setHeaders(headers);
        BindingFactoryManager manager = bean.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(bean.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
        T api = bean.create(apiClass);
        WebClient client = bean.createWebClient();
        return api;
    }

    public void testGetEntityById() {
        try {
            EntitiesApi api = buildAPI(EntitiesApi.class);
            EntityApiResponse response = api.fetchEntityByID("location:16db1e73-03f8-44b4-a22b-c8a29d30ac71:ivt", null);
            System.out.println(response.getData().toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void testGetUserById() {
        try {
            UsersApi api = buildAPI(UsersApi.class);
            UserApiResponse response = api.findUserById("100");
            System.out.println(response.getData().toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public void testGetListOfAllEntities() {
        try {
            EntitiesApi api = buildAPI(EntitiesApi.class);
            EntityApiResponse response = api.fetchAllEntities(0l, 10l, null, null, null, null, null, "");
            System.out.println(response.getData().toString());
            SignalsEntity entity = new SignalsEntity();
            for (EntityApiResponseData entityData : response.getData()) {

                entity.setId(entityData.getId());
                entity.setEid(entityData.getAttributes().getEid());
                entity.setName(entityData.getAttributes().getName());
                entity.setDescription(entityData.getAttributes().getDescription());
                entity.setFlags(Collections.singletonList(entityData.getAttributes().getFlags().toString()));
                entity.setType(entityData.getAttributes().getType());
                entity.setCreatedAt(parseDate(entityData.getAttributes().getCreatedAt()));
                entity.setEditedAt(parseDate(entityData.getAttributes().getEditedAt()));
               // entity.setCreatedBy((JsonApiRelationship) entityData.getRelationships());
                entity.setTimeStamp(new Date());

                dbService.save(entity);
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void test() {
        // testGetUserById();
        // testGetEntityById();
        testGetListOfAllEntities();
    }

}
