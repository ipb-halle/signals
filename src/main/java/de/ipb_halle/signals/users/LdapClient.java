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

import jakarta.annotation.Resource;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
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
public class LdapClient {

    // offset in 100 nanosecond intervals for 1601-01-01
    private final long TIME_OFFSET_AD = 116444768080000000L;

    @Resource
    SignalsConfig signalsConfig;

    @Inject
    private LdapAdapterFactory  ldapAdapterFactory;

    private Logger logger = LoggerFactory.getLogger(LdapClient.class);


    /**
     * Tries to parse the user or group creation date.
     * Defaults to current time. Possibly an AD specific implementation.
     * @param attrs the attribute set
     * @return the account creation date
     */
    private Date getCreatedAt(Attributes attrs) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat(signalsConfig.getLdapDateFormatString());
        try {
            return dateFormat.parse(
                    getAttribute(attrs, signalsConfig.getLdapAttrCreatedAt()));
        } catch(ParseException pe) {
            logger.warn("getCreatedAt() date parsing error: {}", pe.getMessage());
        }
        return new Date(0);
    }

    /**
     * @param groupDN a distinguished group name
     * @return a corresponding Group object
     */
    public Group getGroup(String groupDN) {
        try (LdapAdapter adapter = ldapAdapterFactory.getAdapter(signalsConfig)) {
            Attributes attrs = adapter.getAttributes(groupDN);

            Group group = new Group();

            group.setName(getAttribute(attrs, signalsConfig.getLdapAttrGroupName()));
            group.setDescription(signalsConfig.getGroupAttrDescription());
            group.setSystem(true);

            return group;
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
    public void getMembers(Set<String> users,
                Set<String> groups,
                String groupDN,
                boolean nesting) {

        try (LdapAdapter adapter = ldapAdapterFactory.getAdapter(signalsConfig)) {
            getMembers(adapter, users, groups, groupDN, nesting);
        } catch(Exception e) {
            logger.warn("getMembers() caught an Exception for DN {}: ", groupDN, e);
        }
    }


    private void getMembers(LdapAdapter adapter,
                    Set<String> users,
                    Set<String> groups,
                    String groupDN,
                    boolean nesting) throws Exception {

        BasicAttribute membersAttr = (BasicAttribute) adapter
                .getAttributes(groupDN)
                .get(signalsConfig.getLdapAttrMembers());

        if (membersAttr != null) {
            NamingEnumeration<?> membersEnumeration = membersAttr.getAll();
            while (membersEnumeration.hasMore()) {
                String dn = membersEnumeration.next().toString();
                if (isGroup(adapter, dn)) {
                    if (groups.add(dn) && nesting) {
                        getMembers(adapter, users, groups, dn, nesting);
                    }
                } else {
                    users.add(dn);
                }
            }
        }
    }

    /**
     * @param objDN a distinguished object name
     * @param nesting true if nested memberships should be resolved
     * @return the list of (nested) group memberships for the given user
     */
    public Set<String> getMemberships(String objDN, boolean nesting) {
        Set<String> groups = new HashSet<> ();
        try (LdapAdapter adapter = ldapAdapterFactory.getAdapter(signalsConfig)) {
            getMemberships(adapter, groups, objDN, nesting);
        } catch(Exception e) {
            logger.warn("getMemberships() caught an Exception: ", (Throwable) e);
        }
        return groups;
    }

    /**
     * recursively resolve memberships
     * @param adapter the LDAP service adapter
     * @param groups the set to hold discovered distinguished group names
     * @param objDN the distinguished object, for which memberships are to be discovered
     * @param nesting true if nested memberships should be resolved
     */
    private void getMemberships(LdapAdapter adapter,
                Set<String> groups,
                String objDN,
                boolean nesting) throws Exception {

        BasicAttribute memberOfAttr = (BasicAttribute) adapter
                .getAttributes(objDN)
                .get(signalsConfig.getLdapAttrMemberOf());

        if (memberOfAttr != null) {
            NamingEnumeration<?> memberOfEnumeration = memberOfAttr.getAll();

            while (memberOfEnumeration.hasMore()) {
                String dn = memberOfEnumeration.next().toString();
                if (groups.add(dn) && nesting) {
                    getMemberships(adapter, groups, dn, nesting);
                }
            }
        }
    }

    /**
     * @param roleDN a distinguished LDAP group name for that role
     * @return a corresponding Role object
     */
    public Role getRole(String roleDN) {
        try (LdapAdapter adapter = ldapAdapterFactory.getAdapter(signalsConfig)) {
            Attributes attrs = adapter.getAttributes(roleDN);

            Role role = new Role();
            role.setName(getAttribute(attrs, signalsConfig.getLdapAttrGroupName()));
            role.setDescription(signalsConfig.getGroupAttrDescription());

            return role;
        } catch(Exception e) {
            logger.warn("getRole() caught an Exception: ", (Throwable) e);
        }
        return null;
    }

    /**
     * @param userDN a distinguished user name
     * @return a corresponding User object
     */
    public User getUser(String userDN) throws Exception {
        try (LdapAdapter adapter = ldapAdapterFactory.getAdapter(signalsConfig)) {
            Attributes attrs = adapter.getAttributes(userDN);

            if (! attrs.get(signalsConfig.getLdapAttrObjectClass())
                        .contains(signalsConfig.getLdapAttrObjectClassUser())) {
                throw new Exception("DN " + userDN +  " is not a person");
            }

            User user = new User();
            user.setAlias(getAttribute(attrs, signalsConfig.getLdapAttrAlias()).toUpperCase());
            user.setCountry(signalsConfig.getUserAttrCountry());
            user.setCreatedAt(getCreatedAt(attrs));
            user.setEmail(getAttribute(attrs, signalsConfig.getLdapAttrEmail()).toLowerCase());
            user.setEnabled(getUserExpiration(attrs));
            user.setFirstName(getAttribute(attrs, signalsConfig.getLdapAttrFirstName()));
            user.setMutable(true);
            user.setLastName(getAttribute(attrs, signalsConfig.getLdapAttrLastName()));
            user.setOrganization(signalsConfig.getUserAttrOrganization());
            user.setUserName(getAttribute(attrs, signalsConfig.getLdapAttrUserName()).toLowerCase());

            return user;
        } catch(Exception e) {
            logger.warn("getUser({}) caught an exception", userDN);
            throw new Exception(e.getMessage() + " for user " + userDN);
        }
    }

    private String getAttribute(Attributes attrs, String attrName) throws Exception {
        BasicAttribute attribute = (BasicAttribute) attrs.get(attrName);
        if (attribute != null) {
            return attribute.get().toString();
        }
        logger.warn("Missing mandatory attribute {}", attrName);
        throw new Exception("Missing mandatory attribute");
    }

    /**
     * Determine expiration status of account according to account expiration date.
     *
     * NOTE: This is an AD specific implementation, "expires never" can obviously
     * be represented by two values: either 2^63-1 or 0.
     *
     * @param attr LDAP attribute set
     * @return enabled state
     */
    private boolean getUserExpiration(Attributes attrs) throws Exception {
        String value = getAttribute(attrs,
                signalsConfig.getLdapAttrAccountExpirationDate());
        try {
            long nanos = Long.parseLong(value);
            if (nanos == 0) {
                return true;
            }
            long millis = (nanos - TIME_OFFSET_AD) / 10000;
            if (millis < new Date().getTime()) {
                return false;
            }
        } catch(NumberFormatException nfe) {
            logger.warn("Evaluation of expiration date '{}' failed.", value);
            logger.warn("stack trace:", (Throwable) nfe);
        }
        return true;
    }

    private boolean isGroup(LdapAdapter adapter, String dn) {
        try {
            BasicAttribute objectClassAttr = (BasicAttribute) adapter
                    .getAttributes(dn)
                    .get(signalsConfig.getLdapAttrObjectClass());

            return objectClassAttr.contains(signalsConfig.getLdapAttrObjectClassGroup());
        } catch(Exception e) {
            logger.warn("isGroup() caught an Exception: ", (Throwable) e);
        }
        return false;
    }
}
