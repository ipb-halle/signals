/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author fblocal
 */
public class CrawlJobFactoryImpl implements CrawlJobFactory {

    private final static String ALGORITHM = "digestAlgorithm";
    private final static String PATHS_CONFIG = "paths";
    private final static String DIRECTORY = "directory";
    private final static String STRIP_PREFIX = "stripPrefix";
    private final static String NAMESPACE = "namespace";
    private final static String THRESHOLD = "subtreeScanThreshold";

    @Override
    public ConcurrentLinkedQueue<CrawlPath> buildJobs() {
        Config config = Config.getInstance();
        ConcurrentLinkedQueue<CrawlPath> queue = new ConcurrentLinkedQueue<> ();
        if (config.isArray(PATHS_CONFIG)) {
            int size = config.getArraySize(PATHS_CONFIG);
            for (int i = 0; i < size; i++) {
                ConfigElement pathConfig = config.getArrayElement(PATHS_CONFIG, i);
                queue.add(setupPath(pathConfig));
            }
        }
        return queue;
    }

    private CrawlPath setupPath(ConfigElement element) {
        Config config = Config.getInstance();
        String globalPrefix = config.getGlobalPrefix();

        String algorithm = element.getConfigString(ALGORITHM, null);
        String dir = element.getConfigString(DIRECTORY, null);
        String namespace = element.getConfigString(NAMESPACE, null);
        String prefix = element.getConfigString(STRIP_PREFIX, null);
        if ((dir == null) || (prefix == null) || (namespace == null)) {
            throw new RuntimeException("invalid configuration: directory or prefix missing");
        }
        PathParameters parameters = new PathParameters(
                globalPrefix + dir,
                globalPrefix + prefix,
                namespace);
        parameters.setAlgorithm(DigestAlgorithm.byName(algorithm));
        parameters.setFullScan(config.isFullScan());
        parameters.setScanThreshold(element.getConfigLong(THRESHOLD, 0L));
        return new CrawlPathImpl(parameters);
    }
}
