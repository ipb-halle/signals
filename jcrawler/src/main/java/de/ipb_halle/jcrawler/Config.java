/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fblocal
 */
public class Config {

    private JsonElement json;
    private boolean fullScan = false;

    public boolean isFullScan() {
        return fullScan;
    }

    void setFullScan(boolean f) {
        fullScan = f;
    }

    private boolean parseConfig(Reader reader) {
        try {
             json = JsonParser.parseReader(reader);
        } catch(JsonIOException | JsonSyntaxException e) {
            return true;
        }
        return false;
    }

    public boolean setConfigFile(String name) {
        try (Reader reader = new FileReader(name)) {
            return parseConfig(reader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    // mainly for tests
    public boolean setConfigStream(InputStream is) {
        try (Reader reader = new InputStreamReader(is)) {
            return parseConfig(reader);
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public Boolean getConfigBoolean(String path, Boolean dflt) {
        JsonPrimitive p = getConfigPrimitive(path);
        if ((p != null) && p.isBoolean()) {
            return p.getAsBoolean();
        }
        return dflt;
    }

    public Long getConfigLong(String path, Long dflt) {
        JsonPrimitive p = getConfigPrimitive(path);
        if ((p != null) && p.isNumber()) {
            return p.getAsLong();
        }
        return dflt;
    }

    public String getConfigString(String path, String dflt) {
        JsonPrimitive p = getConfigPrimitive(path);
        if ((p != null) && p.isString()) {
            return p.getAsString();
        }
        return dflt;
    }

    public JsonPrimitive getConfigPrimitive(String path) {
        JsonElement element = getFromPath(json, path);
        if ((element != null) && element.isJsonPrimitive()) {
            return element.getAsJsonPrimitive();
        }
        return null;
    }

    public JsonElement getFromPath(JsonElement json, String path) {
        String[] elements = path.split("\\.");
        return getFromPath(json, elements, 0, elements.length - 1);
    }

    private JsonElement getFromPath(JsonElement json, String[] paths, int index, int last) {
        String element = paths[index];
        if ((json != null) && json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has(element)) {
                if (index < last) {
                    return getFromPath(obj.get(element), paths, index + 1, last);
                } else {
                    return obj.get(element);
                }
            }
        }
        return null;
    }
}
