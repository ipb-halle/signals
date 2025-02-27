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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.rest.MockRestClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;

public class TestBase {

    /**
     * provide the ejb configuration
     * @return a properties object
     */
    public static Properties configuration() {
        Properties properties = new Properties();
        properties.put("openejb.configuration", TestBase.class.getResource("/test-openejb.xml").getFile());
        return properties;
    }

    /**
     * create a PersistenceUnit using the provided entities
     * @param entities array of JPA entity classes to be handled by the PersistenceUnit
     * @return configured PersistenceUnit
     */
   public static PersistenceUnit persistence(String [] entities) {
        if (entities.length == 0) {
            return null;
        }

        PersistenceUnit unit = new PersistenceUnit("signalsDB");
        unit.setJtaDataSource("testDS");
        unit.setNonJtaDataSource("testDSNonJTA");
        unit.setProvider("org.hibernate.jpa.HibernatePersistenceProvider");
        unit.getClazz().addAll(Arrays.asList(entities));
        unit.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//      unit.setProperty("hibernate.show_sql", "true");
//      unit.setProperty("hibernate.format_sql", "true");
//      unit.setProperty("hibernate.use_sql_comments", "true");
        unit.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        unit.setProperty("jakarta.persistence.schema-generation.database.action", "none");

        unit.setProperty("tomee.jpa.cdi", "false");
        return unit;
    }

    /**
     * add test data to the resultMap of the MockRestClient
     * to enable it to return meaningful test data.
     * @param client the MockRestClient
     * @param urlKey the concatenated HTTP method and the request url
     * @param stream an InputStream obtained by Class.getResourceAsStream()
     * and holding the expected test data
     */
    public static void prepareRestClients(MockRestClient client, String urlKey, InputStream stream) {
        client.addResponse(urlKey, readStream(stream));
    }

    /**
     * add test data to the resultMap of the MockRestClient, use a JSON
     * array of key / resource mappings as input
     * @param client the MockRestClient
     * @param clazz the context in which the resource request is to be processed
     * @param config resource name of the MockRestClientConfig
     */
    public static void prepareRestClients(MockRestClient client, Class clazz, String config) {
        InputStreamReader reader = new InputStreamReader(clazz.getResourceAsStream(config));
        JsonElement json = JsonParser.parseReader(reader);
        Iterator<JsonElement> iter = json.getAsJsonArray().iterator();
        while(iter.hasNext()) {
            JsonObject obj = iter.next().getAsJsonObject();
            client.addResponse(obj.get("key").getAsString(),
                    readStream(clazz.getResourceAsStream(obj.get("resource").getAsString())));
        }
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
