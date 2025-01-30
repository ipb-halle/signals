/*
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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
 */
package de.ipb_halle.tda;

import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Custom class loader for use during annotation processing.
 * The classpath is obtained from the DeploymentProcessor via 
 * maven-dependency-plugin and the -A compiler option in the pom file.
 *
 * @author fblocal
 */

public class DeploymentLoader extends ClassLoader {

    private Logger logger = LoggerFactory.getLogger(DeploymentProcessor.class);
    private Map<String, Class<?>> classMap;
    private URLClassLoader loader;

    public DeploymentLoader() {
        super();
        classMap = new HashMap<> ();
    }

    public void setClassPath(String classpath) {
        List<URL> urls = new ArrayList<> ();
        try {
            for (String src : classpath.split(":")) {
                // logger.warn("ADD URL: {}", src);
                urls.add(new URL("file://" + src));
            }
            String src = Path.of("target", "classes").toAbsolutePath().toString();
            // logger.warn("ADD URL: {}", src);
            urls.add(new URL("file://" + Path.of("target", "classes").toAbsolutePath().toString()));
            loader = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
        } catch(Exception e) {
            logger.warn("caught an exception", e);
        }
    }

    private Class<?> doLoad(String name) {
        String path = name.replace(".", "/");
        try (FileInputStream stream = new FileInputStream(String.format("target/classes/%s.class", path))) {
            List<byte[]> bytes = new ArrayList<> ();
            int len = 0;
            int chunkLen = 0;
            do {
                byte[] chunk = new byte[4096];
                chunkLen = stream.read(chunk);
                if (chunkLen > 0) {
                    bytes.add(chunk);
                    len += chunkLen;
                }
            } while(chunkLen > -1);
            byte[] classBytes = new byte[len + 4096];
            int i = 0;
            for (byte[] chunk : bytes) {
                for(byte b : chunk) {
                    classBytes[i++] = b;
                }
            }
            return defineClass(classBytes, 0, len);
        } catch(Exception e) {
            logger.warn("doLoad({}) {}", name, e.getMessage(), e);
            // ignore
        }
        throw new RuntimeException("Could not load " + name);
    }
    
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return loader.loadClass(name);
        } catch(ClassNotFoundException e) {
            logger.warn("Failed to load class {} with default loader", name);
        }
        /*
         * Cannot use computeIfAbsent(name, this::doLoad) because of concurrent modification exception
         * Means: we introduce a race condition?
         */
        if (classMap.containsKey(name)) {
            return classMap.get(name);
        }
        Class<?> c = doLoad(name);
        classMap.put(name, c);
        return c;
    }
}
