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
package de.ipb_halle.signals.attribute;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnum;
import de.ipb_halle.signals.dynEnum.DynEnumDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestReplyParser;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Properties;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;

@RunWithApplicationComposer
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttributeTest {

    // contains attribute ids 17, 48, 21 in this order
    private final String TEST_RESOURCE_1 = "AttributeTest001.json";
    private final String TEST_ID_1 = "attribute:1234";
    private final String TEST_KEY_1 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/attributes";

    private final String TEST_RESOURCE_2 = "AttributeTest002.json";
    private final String TEST_ID_2 = "attribute:17";
    private final String TEST_KEY_2 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/attributes/attribute:17";

    private final String TEST_RESOURCE_3 = "AttributeTest003.json";
    private final String TEST_ID_3 = "attribute:48";
    private final String TEST_KEY_3 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/attributes/attribute:48";

    private final String TEST_RESOURCE_4 = "AttributeTest004.json";
    private final String TEST_ID_4 = "attribute:21";
    private final String TEST_KEY_4 =
            "GET:https://endpoint.somewhere.invalid/api/rest/v1.0/attributes/attribute:21";


    @Inject
    private AttributeDbService dbService;

    @Inject
    private AttributeRestService restService;

    @Inject
    private DynEnumManager dynEnumMgr;

    @Inject
    private MockRestClient mockRestClient;

    @Module
    @Classes(cdi = true, value = {
            Attribute.class, AttributeType.class, AttributeDefinition.class, AttributeValue.class,
            AttributeDbService.class, AttributeRestService.class,
            DynEnum.class, DynEnumDbService.class, DynEnumManager.class,
            SignalsConfig.class,
            RestClient.class, MockRestClient.class, RestReplyParser.class
            })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ AttributeDefinition.class.getName(),
                AttributeType.class.getName(), AttributeValue.class.getName(),
                DynEnum.class.getName()});
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }

    @BeforeAll
    public void setup() {

        dynEnumMgr.allowEnumDiscovery();

        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_1,
                getClass().getResourceAsStream(TEST_RESOURCE_1));
        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_2,
                getClass().getResourceAsStream(TEST_RESOURCE_2));
        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_3,
                getClass().getResourceAsStream(TEST_RESOURCE_3));
        TestBase.prepareRestClients(mockRestClient,
                TEST_KEY_4,
                getClass().getResourceAsStream(TEST_RESOURCE_4));
    }

    @Test
    public void entityTest() {

        Attribute attr = new Attribute();
        attr.setId(TEST_ID_1);
        attr.setName("testAttribute");
        attr.setType((AttributeType) dynEnumMgr.valueOf(AttributeType.valueOf(AttributeType.CHOICE)));
        attr.addOption("gestern");
        attr.addOption("heute");
        attr.addOption("morgen");

        dbService.save(attr);

        Attribute fromDb = dbService.loadById(TEST_ID_1);

        Assertions.assertEquals(TEST_ID_1, fromDb.getId(), "id matches");
        Assertions.assertEquals("testAttribute", fromDb.getName(), "name matches");
        Assertions.assertEquals(3, fromDb.getOptions().size(), "option count matches");
    }

    @Test
    public void endpointTest() {
        List<Attribute> attributes = restService.doGetAllAttributes();
        for (Attribute attr : attributes) {
            dbService.save(attr);
        }
        Attribute fromDb = dbService.loadById(TEST_ID_4);
        Assertions.assertEquals("{user.alias}{###}", fromDb.getFormat(), "Attr format matches");
        fromDb = dbService.loadById(TEST_ID_2);
        AttributeValue option = new AttributeValue(TEST_ID_2, "Bottle");
        Assertions.assertTrue(fromDb.getOptions().contains(option), "Option is present");
        fromDb = dbService.loadById(TEST_ID_3);
        option = new AttributeValue("TEST_ID_3", "foobar_does_not_exist");
        Assertions.assertEquals("NMR Experiment Types", fromDb.getName(), "Name matches");
        Assertions.assertEquals("selectable in NMR Request Sheet", fromDb.getDescription(), "Description matches");
        Assertions.assertFalse(fromDb.getOptions().contains(option), "Bogus option not present");
    }
}
