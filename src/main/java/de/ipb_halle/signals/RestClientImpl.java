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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;


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

    /**
     * constructor
     */
    public RestClientImpl() {
        urlParameterMap = new HashMap<> ();
        method = Method.GET;
    }

    public RestClient execute() throws IOException, MalformedURLException, UnexpectedResponseCodeException {
        return execute(HttpURLConnection.HTTP_OK);
    }

    public RestClient execute(int expectedResponseCode) throws IOException, MalformedURLException, UnexpectedResponseCodeException {
        HttpURLConnection urlConn = (HttpURLConnection) getURL().openConnection();
        urlConn.setRequestMethod(method.toString());
        urlConn.setRequestProperty("accept", "application/vnd.api+json");
        urlConn.setRequestProperty("X-API-KEY", signalsConfig.getApiKey());

        if (requestData != null) {
            urlConn.getOutputStream().write(requestData.getBytes(UTF8));
        }

        responseCode = urlConn.getResponseCode();

        try(BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), UTF8))) {
            StringBuilder sb = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                sb.append(responseLine.trim());
            }
            setResponse(sb.toString());
        } 

        if (responseCode != expectedResponseCode) {
            throw new UnexpectedResponseCodeException();
        }

        return this;
    }

    protected Method getMethod() {
        return method;
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

    public RestClient putUrlParameter(String key, String value) {
        url = null;
        this.urlParameterMap.put(key, value);
        return this;
    }

    public RestClient reset() {
        method = Method.GET;
        requestData = null;
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
