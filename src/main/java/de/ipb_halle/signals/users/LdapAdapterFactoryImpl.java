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
package de.ipb_halle.signals.users;

import de.ipb_halle.signals.SignalsConfig;

import jakarta.ejb.Local;
import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Local
public class LdapAdapterFactoryImpl implements LdapAdapterFactory {

    private Logger logger = LoggerFactory.getLogger(LdapAdapterFactoryImpl.class);

    public LdapAdapter getAdapter(SignalsConfig cfg) throws LdapConnectionErrorException {
        LdapAdapterImpl adapter = new LdapAdapterImpl(cfg);
        adapter.initEnv();
        for (int maxRepeat = 2; maxRepeat > 0; maxRepeat--) {
            try {
                adapter.initContext();
                return adapter;
            } catch (IOException ioe) {
                logger.warn("initContext() caught an exception: {}", ioe.getMessage());
                logger.warn("initContext(): new attempt will be made in 200 ms");
            } catch (NamingException ne) {
                logger.warn("initContext() caught an exception: {}", ne.getMessage());
                logger.warn("initContext(): new attempt will be made in 200 ms");
            }
            /*
             * NOTE:
             * Thread.sleep() is obviously in violation of the EJB contract.
             * We do it in rare error cases only until we better understand
             * what causes this condition and how to avoid it.
             */
            try {
                Thread.sleep(200);
            } catch(InterruptedException ie) {
                // ignore
            }
        }
        throw new LdapConnectionErrorException("getAdapter() repeatedly failed to initialize context");
    }
}
