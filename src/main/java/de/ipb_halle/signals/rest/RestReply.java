/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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

import java.nio.file.Path;

public class RestReply {

    private String string;
    private Path path;
    private String digest;
    private String mimeType;
    private Long fileSize;
    private RestClient.RestType type;

    public RestReply(String st, String m) {
        string = st;
        mimeType = m;
        type = RestClient.RestType.STRING;
    }

    public RestReply(Path p, String d, String m) {
        path = p;
        digest = d;
        mimeType = m;
        type = RestClient.RestType.STREAM;
    }

    public String getString() {
        return string;
    }

    public String getDigest() {
        return digest;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Path getPath() {
        return path;
    }

    public RestClient.RestType getType() {
        return type;
    }

    public void setMimeType(String m) {
        mimeType = m;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
