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

import jakarta.ejb.LocalBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@LocalBean
public class MockRestClient extends RestClientImpl {

    //innerClas  for saving of status code and answers
    private static class MockResponse {
        private final String content;
        private final int statusCode;

        private MockResponse(String content, int statusCode) {
            this.content = content;
            this.statusCode = statusCode;
        }

        public String getContent() {
            return content;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    private static final Map<String, MockResponse> responseMap = new HashMap<>();

    // method for adding the response with status code
    public void addResponse(String key, String content, int statusCode) {
        responseMap.put(key, new MockResponse(content, statusCode));
    }

    // simplified standard method with response code 200
    public void addResponse(String key, String value) {
        addResponse(key, value, 200);
    }

    @Override
    public RestClient execute(int expectedResponseCode) throws IOException, URISyntaxException, UnexpectedResponseCodeException {
        StringJoiner sj = new StringJoiner(":");

        String key = sj.add(getMethod().toString())
                .add(getURI().toString())
                .toString();

        MockResponse mockResponse = responseMap.get(key);
        if (mockResponse == null) {
            //additional method for debugging
            logRequestData();
            throw new IOException("MockRestClient not configured for key: " + key);
        }

        // Check the status code and throw exception if it differs
        if (mockResponse.getStatusCode() != expectedResponseCode) {
            throw new UnexpectedResponseCodeException(String.format("Expected status %s but got %s", expectedResponseCode, mockResponse.getStatusCode()));
        }
        setMockResponse(mockResponse);
        return this;
    }

    private void setMockResponse(MockResponse response) throws IOException {
        switch (getResponseType()) {
            case STRING:
                setResponse(response.getContent());
                break;
            case STREAM:
                handleStreamResponse(response.getContent());
                break;

        }
    }

    private void handleStreamResponse(String content) {
        try (InputStream stream = this.getClass().getResourceAsStream(content)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + content);
            }
            Path path = Files.createTempFile(
                    Path.of("/tmp"),
                    "mock",
                    "bin",
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")));
            digest(stream, path);
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to handle stream", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void logRequestData() {
        if (getRequestData() != null) {
            System.out.println("\n*\n* DUMP REQUEST DATA\n*");
            System.out.println(getRequestData());
            System.out.println("\n*\n* \n*");
        }
    }
}
