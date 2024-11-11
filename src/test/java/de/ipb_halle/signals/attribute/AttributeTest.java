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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.TestBase;
import de.ipb_halle.signals.dynEnum.DynEnum;
import de.ipb_halle.signals.dynEnum.DynEnumDbService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.MockRestClient;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestReplyParser;
import jakarta.inject.Inject;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class AttributeTest {

    private final String TEST_RESOURCE = "AttributeTest001.json";
    private final String TEST_ID_1 = "attribute:1234";
    private final String TEST_ID_2 = "attribute:17";


    @Inject
    private AttributeDbService dbService;

    @Inject
    private DynEnumManager dynEnumMgr;

    @Module
    @Classes(cdi = true, value = {
            Attribute.class, AttributeType.class, AttributeDefinition.class, AttributeValue.class,
            AttributeDbService.class,
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

    @Before
    public void setup() {
        dynEnumMgr.allowEnumDiscovery();
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

        assertEquals("id matches", TEST_ID_1, fromDb.getId());
        assertEquals("name matches", "testAttribute", fromDb.getName());
        assertEquals("option count matches", 3, fromDb.getOptions().size());
    }
}
