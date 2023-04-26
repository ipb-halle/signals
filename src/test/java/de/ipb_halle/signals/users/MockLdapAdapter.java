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
package de.ipb_halle.signals.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;


/**
 * Mock LDAPAdapter 
 */
public class MockLdapAdapter implements AutoCloseable, LdapAdapter {

    private final static String TEST_DATA = "LdapData.json";
    private Map<String, Attributes> objects;

    public MockLdapAdapter() {
        objects = new HashMap<> ();
        parseLdapData();
    }

    public void close() throws IOException {
    }

    public Attributes getAttributes(String dn) throws NamingException {
        return objects.get(dn);
    }

    private void parseLdapData() {
        try {
            InputStream is = this.getClass().getResourceAsStream(TEST_DATA);
            Reader reader = new InputStreamReader(is);
            JsonElement elem = JsonParser.parseReader(reader);
            parseObjects(elem);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void parseObjects(JsonElement element) {
        if (element.isJsonArray()) {
            Iterator<JsonElement> iter = element.getAsJsonArray().iterator();
            while (iter.hasNext()) {
                JsonObject jsonObj = iter.next().getAsJsonObject();
                parseObject(jsonObj);
            }
        }
    }

    private void parseObject(JsonObject obj) {
        Attributes attributes = new BasicAttributes();
        String dn = "";
        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            switch(key) {
                case "dn" : 
                    dn = element.getAsJsonPrimitive().getAsString();
                    break;
                case "member" :
                case "memberOf" :
                case "objectClass" :
                    attributes.put(parseArrayAttribute(key, element));
                    break;
                default: 
                    attributes.put(new BasicAttribute(
                            key, 
                            element.getAsJsonPrimitive().getAsString()));
            }
        }
        objects.put(dn, attributes);
    }

    public BasicAttribute parseArrayAttribute(String id, JsonElement element) {
        BasicAttribute attribute = new BasicAttribute(id);
        Iterator<JsonElement> iter = element.getAsJsonArray().iterator();
        while (iter.hasNext()) {
                String value = iter.next().getAsJsonPrimitive().getAsString();
                attribute.add(value);
        }
        return attribute;
    }
}
