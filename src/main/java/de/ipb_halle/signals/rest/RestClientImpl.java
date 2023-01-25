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
import java.net.MalformedURLException;
import java.net.URL;
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

    private String endpoint;
    private Method method;
    private String requestData;
    private String response;
    private int responseCode;
    private URL url;
    private Map<String, String> urlParameterMap;

    private Logger logger;

    /**
     * constructor
     */
    public RestClientImpl() {
        urlParameterMap = new HashMap<> ();
        logger = LoggerFactory.getLogger(RestResultIterator.class);
        method = Method.GET;
    }

    public RestClient execute() throws IOException, MalformedURLException, UnexpectedResponseCodeException {
        return execute(HttpURLConnection.HTTP_OK);
    }

    public RestClient execute(int expectedResponseCode) throws IOException, MalformedURLException, UnexpectedResponseCodeException {
        HttpURLConnection urlConn = (HttpURLConnection) getURL().openConnection();
/*
        if ((method == Method.PATCH) && (! RestClientImpl.patchAllowed)) {
                throw new IllegalStateException("fix to HttpURLConnection failed - method PATCH is not allowed");
        }
*/
        urlConn.setRequestMethod(method.toString());
        urlConn.setRequestProperty("Accept", "application/vnd.api+json");
        urlConn.setRequestProperty("X-API-KEY", signalsConfig.getApiKey());

        if (requestData != null) {
            if ((method == Method.GET) || (method == Method.DELETE)) {
                logger.warn("Unexpected write request for UrlConnection in {} request", method.toString());
            }
            logger.trace("***** Dump of request *****\n{}\n***** End of request dump  *****", requestData);
            urlConn.setRequestProperty("Content-Type", "application/vnd.api+json");
            urlConn.setDoOutput(true);
            urlConn.getOutputStream().write(requestData.getBytes(UTF8));
        }

        responseCode = urlConn.getResponseCode();
        try {
            obtainResponse(urlConn);
        } finally {
            if (responseCode != expectedResponseCode) {
                this.logger.debug("Obtained unexpected response code ({} vs {})", responseCode, expectedResponseCode);
                this.logger.debug("Connection {} {}", method.toString(), getURL().toString());
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

    protected URL getURL() throws MalformedURLException {
        if (url != null) {
            return url;
        }
        StringBuilder sb = new StringBuilder(signalsConfig.getBaseUrl());
        sb.append(endpoint);
        if (! urlParameterMap.isEmpty()) {
            AtomicReference<String> sep = new AtomicReference<> ("");
            sb.append("?");
            urlParameterMap.forEach((key, value) -> {
                        sb.append(sep.getAndSet("&"));
                        try {
                            sb.append(URLEncoder.encode(key, UTF8));
                            sb.append("=");
                            sb.append(URLEncoder.encode(value, UTF8));
                        } catch(UnsupportedEncodingException uee) {
                            // ignored
                        }   
                    });
        }

        url = new URL(sb.toString());
        return url;
    }

    private void obtainResponse(HttpURLConnection urlConn) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), UTF8))) {
            StringBuilder sb = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                sb.append(responseLine.trim());
            }
            setResponse(sb.toString());
        }
    }

    public RestClient putUrlParameter(String key, String value) {
        url = null;
        this.urlParameterMap.put(key, value);
        return this;
    }

    public RestClient reset() {
        method = Method.GET;
        requestData = null;
        response = null;
        urlParameterMap = new HashMap<> ();
        return this;
    }

    public RestClient setEndpoint(String path) {
        url = null;
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

    public RestClient setURL(String u) throws MalformedURLException {
        endpoint = null;
        urlParameterMap.clear();
        url = new URL(u);
        return this;
    }
}
