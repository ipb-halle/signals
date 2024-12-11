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
import java.net.URISyntaxException;
import jakarta.ejb.Local;

/**
 * Http client reader for Signals tool
 */
@Local
public interface RestClient {

    public enum RestType {
        STRING,
        PATH;
    }

    /**
     * Name of the staging directory for attachment downloads
     */
    public final static String STAGING = "staging";

    public final int HTTP_OK = 200;
    public final int HTTP_CREATED = 201;
    public final int HTTP_ACCEPTED = 202;
    public final int HTTP_NO_CONTENT = 204;

    public final String APPLICATION_VND_JSON = "application/vnd.api+json";
    public final String APPLICATION_SCIM_JSON = "application/scim+json";
    public final String APPLICATION_OCTET = "application/octet-stream";
    public final String CHEMICAL_SVG = "image/svg+xml";
    public final String CHEMICAL_CDXML = "chemical/x-cdxml";
    public final String CHEMICAL_MOL3000 = "chemical/x-mdl-molfile-v3000";
    public final String CHEMICAL_SMILES = "chemical/x-daylight-smiles";
    public final String CHEMICAL_SDFILE = "chemical/x-mdl-sdfile";
    public final String SEQUENCE_FASTA = "biosequence/fasta";
    public final String SEQUENCE_GENBANK = "biosequence/genbank";
    public final String TEXT_CSV = "text/csv";

    public RestClient execute() throws IOException, URISyntaxException, UnexpectedResponseCodeException ;

    public RestClient execute(int expectedResponseCode) throws IOException, URISyntaxException, UnexpectedResponseCodeException ;

    public String getResponse();

    public int getResponseCode();

    public RestClient putUriParameter(String key, String value);

    public RestClient reset();

    public RestClient setContentType(String type);

    public RestClient setEndpoint(String path);

    public RestClient setHeader(String key, String value);

    public RestClient setMethod(Method m);

    public RestClient setRequestData(String data);

    public RestClient setURI(String u) throws URISyntaxException;
}
