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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Ldap client reader for Signals tool.
 * The current implementation was created to fit IPB needs. It therefore 
 * depends on AD peculiarities and is not platform neutral.  It should be 
 * straightforward however, to adjust it to other flavours of LDAP. 
 * As IPB does not possess other LDAP instances for testing and has 
 * no use case, the introduction of an abstraction layer (LdapClientImplAD, etc.)
 * is deemed unnecessary.
 * This implementation supports (and currently requires) STARTTLS. It might be 
 * necessary to provide a custom truststore, in case the LDAP server does not 
 * use a certificate from an officially recognized CA.
 */
@Local
public class LdapClientImpl implements LdapClient {

    // offset in 100 nanosecond intervals for 1601-01-01
    private final long TIME_OFFSET_AD = 116444768080000000L;

    private final String startTlsEnvKey = "StartTlsResponseEnvKey";

    @Resource
    SignalsConfig signalsConfig;

    private Hashtable<String, String> ldapEnv;

    private Logger logger = LoggerFactory.getLogger(LdapClientImpl.class);

    @PostConstruct
    private void initialize() {
        ldapEnv = new Hashtable<>();
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put(Context.PROVIDER_URL, signalsConfig.getLdapContextProviderURL());
        ldapEnv.put(Context.REFERRAL, signalsConfig.getLdapContextReferral());
    }

    /**
     * close a Context and the StartTlsResponse
     */
    private void closeContext(Context ctx) throws NamingException, IOException {
        ((StartTlsResponse) ctx.getEnvironment().get(startTlsEnvKey)).close();
        ctx.removeFromEnvironment(startTlsEnvKey);
        ctx.close();
    }

    private LdapContext getContext() throws Exception {
        LdapContext ctx = new InitialLdapContext(ldapEnv, null);

        // Start TLS
        StartTlsResponse tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
        tls.negotiate();

        ldapEnv.put(Context.SECURITY_AUTHENTICATION, signalsConfig.getLdapSecurityAuthentication());
        ldapEnv.put(Context.SECURITY_PRINCIPAL, signalsConfig.getLdapSecurityPrincipal());
        ldapEnv.put(Context.SECURITY_CREDENTIALS, signalsConfig.getLdapSecurityCredentials());


        // Perform simple client authentication
        ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, signalsConfig.getLdapSecurityAuthentication());
        ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, signalsConfig.getLdapSecurityPrincipal());
        ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, signalsConfig.getLdapSecurityCredentials());

        ctx.addToEnvironment(startTlsEnvKey, tls);
        return ctx;
    }

    /**
     * Tries to parse the user or group creation date.
     * Defaults to current time. Possibly an AD specific implementation.
     * @param attrs the attribute set
     * @return the account creation date
     */
    private Date getCreatedAt(Attributes attrs) throws NamingException {
        DateFormat dateFormat = new SimpleDateFormat(signalsConfig.getLdapDateFormatString());
        try {
            return dateFormat.parse(attrs
                    .get(signalsConfig.getLdapAttrCreatedAt())
                    .get()
                    .toString());
        } catch(ParseException pe) {
            // silently ignore date
        }
        return new Date(0);
    }

    /**
     * @param groupDN a distinguished group name
     * @return a corresponding Group object
     */
    public Group getGroup(String groupDN) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv);
            try {
                Attributes attrs = ctx.getAttributes(groupDN);

                Group group = new Group();

                group.setName(attrs.get(signalsConfig.getLdapAttrGroupName()).get().toString());
                group.setDescription(signalsConfig.getGroupAttrDescription());
                group.setSystem(true);

                return group;
            } finally {
                ctx.close();
            }
        } catch(Exception e) {
            logger.warn("getGroup() caught an Exception: ", (Throwable) e);
        }                                                
        return null;
    }

    /**
     * @param groupDN a distinguished group name
     * @param nesting true if nested memberships should be resolved
     * @return the list of users, who are members of that group, including nested memberships
     */
    public Set<String> getMembers(String groupDN, boolean nesting) {
        Set<String> groupCache = new HashSet<> ();
        Set<String> users = new HashSet<> ();
        getMembers(users, groupCache, groupDN, nesting);
        return users;
    }

    /**
     * @param users a Set to collect DNs of discovered member users
     * @param groups a Set to collect DNs of discovered member groups
     * @param groupDN a distinguished group name
     * @param nesting indicate whether nested memberships should be resolved
     */
    public void getMembers(Set<String> users, Set<String> groups, String groupDN, boolean nesting) {
        try {
            LdapContext ctx = getContext();
            try {
                BasicAttribute membersAttr = (BasicAttribute) ctx
                        .getAttributes(groupDN)
                        .get(signalsConfig.getLdapAttrMembers());

                if (membersAttr != null) {
                    NamingEnumeration<?> membersEnumeration = membersAttr.getAll();
                    while (membersEnumeration.hasMore()) {
                        String dn = membersEnumeration.next().toString();
                        if (isGroup(ctx, dn)) {
                            if (groups.add(dn) && nesting) {
                                getMembers(users, groups, dn, nesting);
                            }
                        } else {
                            users.add(dn);
                        }
                    }
                }
            } finally {
                closeContext(ctx);
            }
        } catch(Exception e) {
            logger.warn("getMembers() caught an Exception for DN {}: ", groupDN, e);
        }
    }

    /**
     * @param objDN a distinguished object name
     * @param nesting true if nested memberships should be resolved
     * @return the list of (nested) group memberships for the given user
     */
    public Set<String> getMemberships(String objDN, boolean nesting) {
        Set<String> groups = new HashSet<> ();
        getMemberships(groups, objDN, nesting);
        return groups;
    }

    /**
     * recursively resolve memberships
     * @param groups the set to hold discovered distinguished group names
     * @param objDN the distinguished object, for which memberships are to be discovered
     * @param nesting true if nested memberships should be resolved
     */
    private void getMemberships(Set<String> groups, String objDN, boolean nesting) {
        try {
            LdapContext ctx = getContext();
            try {
                BasicAttribute memberOfAttr = (BasicAttribute) ctx
                        .getAttributes(objDN)
                        .get(signalsConfig.getLdapAttrMemberOf());

                if (memberOfAttr != null) {
                    NamingEnumeration<?> memberOfEnumeration = memberOfAttr.getAll();

                    while (memberOfEnumeration.hasMore()) {
                        String dn = memberOfEnumeration.next().toString();
                        if (groups.add(dn) && nesting) {
                            getMemberships(groups, dn, nesting);
                        }
                    }
                }
            } finally {
                closeContext(ctx);
            }
        } catch(Exception e) {
            logger.warn("getMemberships() caught an Exception: ", (Throwable) e);
        }
    }

    /**
     * @param roleDN a distinguished LDAP group name for that role
     * @return a corresponding Role object
     */
    public Role getRole(String roleDN) {
        try {
            LdapContext ctx = getContext();
            try {
                Attributes attrs = ctx.getAttributes(roleDN);

                Role role = new Role();
                role.setName(attrs.get(signalsConfig.getLdapAttrGroupName()).get().toString());
                role.setDescription(signalsConfig.getGroupAttrDescription());

                return role;
            } finally {
                closeContext(ctx);
            }
        } catch(Exception e) {
            logger.warn("getRole() caught an Exception: ", (Throwable) e);
        }
        return null;
    }

    /**
     * @param userDN a distinguished user name
     * @return a corresponding User object
     */
    public User getUser(String userDN) {
        try {
            LdapContext ctx = getContext();
            try {
                Attributes attrs = ctx.getAttributes(userDN);

                if (! attrs.get(signalsConfig.getLdapAttrObjectClass())
                            .contains(signalsConfig.getLdapAttrObjectClassUser())) {
                    logger.warn("DN {} is not a person", userDN);
                    return null;
                }

                User user = new User();
                user.setAlias(attrs.get(signalsConfig.getLdapAttrAlias()).get().toString().toUpperCase());
                user.setCountry(signalsConfig.getUserAttrCountry());
                user.setCreatedAt(getCreatedAt(attrs));
                user.setEmail(attrs.get(signalsConfig.getLdapAttrEmail()).get().toString().toLowerCase());
                user.setEnabled(getUserExpiration(attrs));
                user.setFirstName(attrs.get(signalsConfig.getLdapAttrFirstName()).get().toString());
                user.setMutable(true);
                user.setLastName(attrs.get(signalsConfig.getLdapAttrLastName()).get().toString());
                user.setOrganization(signalsConfig.getUserAttrOrganization());
                user.setUserName(attrs.get(signalsConfig.getLdapAttrUserName()).get().toString().toLowerCase());

                return user;
            } finally {
                closeContext(ctx);
            }
        } catch(Exception e) {
            logger.warn("getUser() caught an Exception: ", (Throwable) e);
        } 
        return null;
    }

    /**
     * determine expiration status of account according to account expiration date
     * 
     * NOTE: This is an AD specific implementation!
     *
     * @param attr LDAP attribute set
     * @return enabled state 
     */
    private boolean getUserExpiration(Attributes attrs) throws NamingException {
        String value = attrs.get(signalsConfig
                .getLdapAttrAccountExpirationDate()).get().toString();
        try {
            long millis = (Long.parseLong(value) - TIME_OFFSET_AD) / 10000;
            if (millis < new Date().getTime()) {
                return false;
            }
        } catch(NumberFormatException nfe) {
            logger.warn("Evaluation of expiration date '{}' failed.", value);
            logger.warn("stack trace:", (Throwable) nfe);
        }
        return true;
    }

    private boolean isGroup(LdapContext ctx, String dn) {
        try {
            BasicAttribute objectClassAttr = (BasicAttribute) ctx
                    .getAttributes(dn)
                    .get(signalsConfig.getLdapAttrObjectClass());

            return objectClassAttr.contains(signalsConfig.getLdapAttrObjectClassGroup());
        } catch(Exception e) {
            logger.warn("isGroup() caught an Exception: ", (Throwable) e);
        }
        return false;
    }
}
