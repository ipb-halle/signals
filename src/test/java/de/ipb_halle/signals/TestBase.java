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
import java.util.Arrays;
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
        PersistenceUnit unit = new PersistenceUnit("signalsDB");
        unit.setJtaDataSource("testDS");
        unit.setNonJtaDataSource("testDSNonJTA");
        unit.setProvider("org.hibernate.jpa.HibernatePersistenceProvider");
        unit.getClazz().addAll(Arrays.asList(entities)); 
        unit.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        unit.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        unit.setProperty("javax.persistence.schema-generation.database.action", "create-drop");
        unit.setProperty("javax.persistence.schema-generation.create-script-source", "schema.sql");
        unit.setProperty("javax.persistence.schema-generation.drop-script-source", "drop_schema.sql");
        unit.setProperty("hibernate.hbm2ddl.import_files_sql_extractor", "org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");
        unit.setProperty("tomee.jpa.cdi", "false");
        return unit;
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
