/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 *
 * @author fblocal
 */
public class ConfigElement {

    private JsonElement json;

    protected ConfigElement() {
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

    private JsonPrimitive getConfigPrimitive(String path) {
        JsonElement element = getFromPath(json, path);
        if ((element != null) && element.isJsonPrimitive()) {
            return element.getAsJsonPrimitive();
        }
        return null;
    }

    private JsonElement getFromPath(JsonElement json, String path) {
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

    protected void setJson(JsonElement json) {
        this.json = json;
    }
}
