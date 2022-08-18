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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.api.LocalClient;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.core.ivm.naming.IvmContext;

/** 
 * IPB Signals client is a tool for data import and export
 * into Perkin Elmer Signals Notebook (R). 
 */

@LocalClient
public class Signals {

    @Inject
    private Config config;

    @Inject 
    private SignalsEntityManager signalsMgr;

    public static void runClient(String fname) {
        try {

            Properties properties = new Properties();
            properties.put(DeploymentsResolver.CLASSPATH_INCLUDE, ".*");
            properties.put(DeploymentsResolver.CLASSPATH_EXCLUDE, "");
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

            properties.put("openejb.configuration", "conf/openejb.xml");

/*
            properties.setProperty(EJBContainer.APP_NAME, "signalsApp");
            properties.setProperty(EJBContainer.PROVIDER, OpenEjbContainer.class.getName());
            properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "false");
            properties.setProperty("ejbd.disabled", "true");
            properties.setProperty("ejbds.disabled", "true");
            properties.setProperty("admin.disabled", "true");
            properties.setProperty("openejb.jaxrs.application", "false");
*/

/*
            properties.put("signalsDB", "new://Resource?type=DataSource"); 
            properties.put("signalsDB.JdbcDriver", "org.hsqldb.jdbcDriver"); 
            properties.put("signalsDB.JdbcUrl", "jdbc:hsqldb:mem:signals");
*/



            EJBContainer container = EJBContainer.createEJBContainer(properties);
            Context ctx = container.getContext();

            Signals signals = new Signals();
            ctx.bind("inject", signals);
            signals.config.readConfig(fname);
            signals.signalsMgr.fetchSignalsEntities("location");


        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        if (argv.length != 1) {
            System.out.println("Usage: java -jar signals-with-dependencies.jar CONFIGFILE");
            return;
        } 

        runClient(argv[0]);
    }
}


