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
package de.ipb_halle.signals.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import jakarta.ejb.LocalBean;

import static org.testcontainers.shaded.org.bouncycastle.oer.its.ieee1609dot2.SignerIdentifier.digest;

@LocalBean
public class MockRestClient extends RestClientImpl {

    private static final Map<String, String> responseMap = new HashMap<> ();

    public void addResponse(String key, String value) {
        responseMap.put(key, value);
    }

    @Override
    public RestClient execute(int expectedResponseCode) throws IOException, URISyntaxException, UnexpectedResponseCodeException {
        StringJoiner sj = new StringJoiner(":");
        String key = sj.add(getMethod().toString())
                .add(getURI().toString())
                .toString();
        String response = responseMap.get(key);
        if (response == null) {
            if (getRequestData() != null) {
                System.out.println("\n*\n* DUMP REQUEST DATA\n*");
                System.out.println(getRequestData());
                System.out.println("\n*\n* \n*");
            }
            throw new NullPointerException("MockRestClient not configured for key: ".concat(key));
        }
        setMockResponse(response);
        return this;
    }

    private void setMockResponse(String response) throws IOException {
        switch(getResponseType()) {
            case STRING:
                setResponse(response);
                break;
            case STREAM:
                try {
                    Path p = Files.createTempFile(
                            Path.of("/tmp"),
                            "mock",
                            "bin",
                            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")));
                    digest(this.getClass().getResourceAsStream(response), p);
                    Files.delete(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
    }
}
