/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import com.google.gson.JsonParser;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author fblocal
 */
public class Config extends ConfigElement {

    private boolean fullScan = false;

    // for testing purposes
    private String globalPrefix;

    public boolean isFullScan() {
        return fullScan;
    }

    void setFullScan(boolean f) {
        fullScan = f;
    }

    public String getGlobalPrefix() {
        return globalPrefix;
    }

    public void setGlobalPrefix(String globalPrefix) {
        this.globalPrefix = globalPrefix;
    }

    private boolean parseConfig(Reader reader) {
        try {
             setJson(JsonParser.parseReader(reader));
        } catch(JsonIOException | JsonSyntaxException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public boolean setConfigFile(String name) {
        try (Reader reader = new FileReader(name)) {
            return parseConfig(reader);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    // mainly for tests
    public boolean setConfigStream(InputStream is) {
        try (Reader reader = new InputStreamReader(is)) {
            return parseConfig(reader);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }
}
