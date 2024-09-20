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


/** 
 * Ldap Adapter
 * Encapsulates the concrete mechanics of connection to an 
 * LDAP / AD service.
 */
@Local
public class LdapAdapterImpl implements LdapAdapter, AutoCloseable {

    private final String startTlsEnvKey = "StartTlsResponseEnvKey";

    private SignalsConfig signalsConfig;
    private Hashtable<String, String> ldapEnv;
    private LdapContext context;
    private Logger logger;

    protected LdapAdapterImpl(SignalsConfig cfg) {
        signalsConfig = cfg;
        logger = LoggerFactory.getLogger(LdapAdapterImpl.class);
    }

    /**
     * close the  LDAP Context and the StartTlsResponse
     */
    public void close() throws IOException {
        try {
            ((StartTlsResponse) context.getEnvironment().get(startTlsEnvKey)).close();
            context.removeFromEnvironment(startTlsEnvKey);
            context.close();
        } catch(NamingException e) {
            throw new IOException("close() caught a NamingException: " + e.getMessage());
        }
    }


    public Attributes getAttributes(String dn) throws Exception {
        for (int maxRepeat = 2; maxRepeat > 0; maxRepeat--) {
            try {
                return context.getAttributes(dn);
            } catch (NamingException ex) {
                logger.warn("getAttributes() caught an exception: {}", ex.getMessage());
                logger.warn("getAttributes(): new attempt will be made in 200 ms");
            }
            /*
             * NOTE:
             * Thread.sleep() is obviously in violation of the EJB contract.
             * We do it in rare error cases only until we better understand
             * what causes this condition and how to avoid it.
             */
            Thread.sleep(200);
        }
        throw new Exception("getAttributes() repeatedly unable to obtain attributes");
    }

    protected void initEnv() {
        ldapEnv = new Hashtable<>();
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put(Context.PROVIDER_URL, signalsConfig.getLdapContextProviderURL());
        ldapEnv.put(Context.REFERRAL, signalsConfig.getLdapContextReferral());

        ldapEnv.put(Context.SECURITY_AUTHENTICATION, signalsConfig.getLdapSecurityAuthentication());
        ldapEnv.put(Context.SECURITY_PRINCIPAL, signalsConfig.getLdapSecurityPrincipal());
        ldapEnv.put(Context.SECURITY_CREDENTIALS, signalsConfig.getLdapSecurityCredentials());
    }

    protected void initContext() throws Exception {
        context = new InitialLdapContext(ldapEnv, null);

        // Start TLS
        StartTlsResponse tls = (StartTlsResponse) context.extendedOperation(new StartTlsRequest());
        tls.negotiate();

        // Perform simple client authentication
        context.addToEnvironment(Context.SECURITY_AUTHENTICATION, signalsConfig.getLdapSecurityAuthentication());
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, signalsConfig.getLdapSecurityPrincipal());
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, signalsConfig.getLdapSecurityCredentials());

        context.addToEnvironment(startTlsEnvKey, tls);
    }
}
