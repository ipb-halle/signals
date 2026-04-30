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
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author fblocal
 */
public class Config extends ConfigElement {

    private final static String PROJECT_VERSION = "projectVersion";
    private final static Config instance = new Config();
    private final Logger logger;
    private JobType jobType;
    private boolean fullScan;

    // for testing purposes
    private String globalPrefix = "";

    private Config() {
        fullScan = false;
        jobType = JobType.crawl;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public static Config getInstance() {
        return instance;
    }

    public static String getProjectVersion() {
        try {
            Manifest mf = new Manifest();
            mf.read(Main.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
            Attributes attributes = mf.getMainAttributes();
            return attributes.getValue(PROJECT_VERSION);
        } catch (IOException e) {
            return "unavail";
        }
    }

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

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    private boolean parseConfig(Reader reader) {
        try {
             setJson(JsonParser.parseReader(reader));
        } catch(JsonIOException | JsonSyntaxException e) {
            logger.error("Parsing error: {}", e.getMessage());
            return true;
        }
        return false;
    }

    public boolean setConfigFile(String name) {
        try (Reader reader = new FileReader(name)) {
            return parseConfig(reader);
        } catch (FileNotFoundException ex) {
            logger.error("Config file not found: {}", name);
        } catch (IOException ex) {
            logger.error("IO exception: {}", ex.getMessage());
        }
        return true;
    }

    // mainly for tests
    public boolean setConfigStream(InputStream is) {
        try (Reader reader = new InputStreamReader(is)) {
            return parseConfig(reader);
        } catch (IOException ex) {
            logger.error("IO exception: {}", ex.getMessage());
        }
        return true;
    }
}
