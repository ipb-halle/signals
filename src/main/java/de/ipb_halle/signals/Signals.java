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

import de.ipb_halle.signals.users.LdapClient;
import de.ipb_halle.signals.users.User;

import java.util.Iterator;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.api.LocalClient;

/** 
 * IPB Signals client is a tool for data import and export
 * into PerkinElmer (R) Signals (TM) Notebook. 
 */

@LocalClient
public class Signals {

    @Resource
    private SignalsConfig signalsConfig;

    @Inject 
    private SignalsEntityManager signalsMgr;

    @Inject
    private LdapClient ldapClient;

    public void doIt() {

//          signalsMgr.doGet("location");

/*
            System.out.println("Users\n=====");
            Set<String> users = ldapClient.getUsers(null);
            dumpSet(users);

            Set<String> groups = ldapClient.getMemberships("SOME USER DN");
            System.out.println("Group memberships\n=================");
            dumpSet(ldapClient.filterDNs(groups, LdapClient.FilterType.GROUP));
            System.out.println("Role memberships\n================");
            dumpSet(ldapClient.filterDNs(groups, LdapClient.FilterType.ROLE));

            User u = ldapClient.getUser("SOME USER DN");
            u.dump();

            System.out.println("User by name\n============");
            users = ldapClient.getUsers("SOME EMAIL ADDRESS");
            dumpSet(users);

            System.out.println("Members of Group\n================");
            groups = ldapClient.getMembers("SOME GROUP DN");
            System.out.println("Members");
            dumpSet(groups);
*/
    }

    private void dumpSet(Set<String> set) {
        Iterator<String> iter = set.iterator();
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    public static void runClient(String fname) {
        try {

            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.put("openejb.configuration", fname);

            EJBContainer container = EJBContainer.createEJBContainer(properties);
            Context ctx = container.getContext();

            Signals signals = new Signals();
            ctx.bind("inject", signals);

            signals.doIt();

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


