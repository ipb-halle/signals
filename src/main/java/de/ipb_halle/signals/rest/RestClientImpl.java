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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.signals.SignalsConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.ProxySelector;
import java.time.Duration;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Resource;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http client reader for Signals tool
 */
@Local
public class RestClientImpl implements RestClient {

    private final static String UTF8 = "UTF-8";

    @Resource
    SignalsConfig signalsConfig;

    private String contentType;
    private String endpoint;
    private Method method;
    private String requestData;
    private String response;
    private int responseCode;
    private URI uri;
    private Map<String, String> uriParameterMap;

    private Logger logger;

    /**
     * constructor
     */
    public RestClientImpl() {
        uriParameterMap = new HashMap<>();
        logger = LoggerFactory.getLogger(RestResultIterator.class);
        contentType = APPLICATION_VND_JSON;
        method = Method.GET;
    }

    public RestClient execute() throws IOException, URISyntaxException, UnexpectedResponseCodeException {
        return execute(HttpURLConnection.HTTP_OK);
    }

    public RestClient execute(int expectedResponseCode) throws IOException, URISyntaxException, UnexpectedResponseCodeException {
        BodyPublisher requestBody = BodyPublishers.noBody();

        HttpRequest.Builder builder = HttpRequest.newBuilder(getURI())
                .header("Accept", contentType)
                .header("X-API-KEY", signalsConfig.getApiKey());

        if (requestData != null) {
            if ((method == Method.GET) || (method == Method.DELETE)) {
                logger.warn("Unexpected request for write operation in HttpRequest: {}", method.toString());
            }
            logger.trace("***** Dump of request *****\n{}\n***** End of request dump  *****", requestData);

            requestBody = BodyPublishers.ofString(requestData);
            builder = builder.header("Content-Type", contentType);
        }

        HttpRequest request = builder
                .method(method.toString(), requestBody)
                .build();

        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .proxy(ProxySelector.getDefault())
                .build();

        try {
            HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());
            responseCode = httpResponse.statusCode();
            setResponse(httpResponse.body());
        } catch (InterruptedException ie) {
            this.logger.warn("HTTP request got interrupted");
            response = null;
            responseCode = 0;
        } finally {
            if (responseCode != expectedResponseCode) {
                this.logger.debug("Obtained unexpected response code ({} vs {})", responseCode, expectedResponseCode);
                this.logger.debug("Connection {} {}", method.toString(), getURI().toString());
                if (response != null) {
                    this.logger.debug("***** Dump of response *****\n{}\n***** End of response dump *****", response);
                }
                throw new UnexpectedResponseCodeException(String.format("expected %d, got %d", expectedResponseCode, responseCode));
            }
        }
        return this;
    }

    protected Method getMethod() {
        return method;
    }

    protected String getRequestData() {
        return requestData;
    }

    public String getResponse() {
        return response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    protected URI getURI() throws URISyntaxException {
        if (uri != null) {
            return uri;
        }
        StringBuilder sb = new StringBuilder(signalsConfig.getBaseUrl());
        sb.append(endpoint);
        if (!uriParameterMap.isEmpty()) {
            AtomicReference<String> sep = new AtomicReference<>("");
            sb.append("?");
            uriParameterMap.forEach((key, value) -> {
                sb.append(sep.getAndSet("&"));
                try {
                    sb.append(URLEncoder.encode(key, UTF8));
                    sb.append("=");
                    sb.append(URLEncoder.encode(value, UTF8));
                } catch (UnsupportedEncodingException uee) {
                    // ignored
                }
            });
        }

        uri = new URI(sb.toString());
        return uri;
    }

    public RestClient putUriParameter(String key, String value) {
        uri = null;
        this.uriParameterMap.put(key, value);
        return this;
    }

    public RestClient reset() {
        contentType = APPLICATION_VND_JSON;
        method = Method.GET;
        requestData = null;
        response = null;
        uriParameterMap = new HashMap<>();
        return this;
    }

    public RestClient setContentType(String type) {
        contentType = type;
        return this;
    }

    public RestClient setEndpoint(String path) {
        uri = null;
        endpoint = path;
        return this;
    }

    public RestClient setHeader(String key, String value) {
        return this;
    }

    public RestClient setMethod(Method m) {
        this.method = m;
        return this;
    }

    public RestClient setRequestData(String data) {
        requestData = data;
        return this;
    }

    protected void setResponse(String r) {
        response = r;
    }

    public RestClient setURI(String u) throws URISyntaxException {
        endpoint = null;
        uriParameterMap.clear();
        uri = new URI(u);
        return this;
    }

    @Override
    public String toString() {
        return "RestClientImpl{" +
                "signalsConfig=" + signalsConfig +
                ", contentType='" + contentType + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method=" + method +
                ", requestData='" + requestData + '\'' +
                ", response='" + response + '\'' +
                ", responseCode=" + responseCode +
                ", uri=" + uri +
                ", uriParameterMap=" + uriParameterMap +
                '}';
    }
}
