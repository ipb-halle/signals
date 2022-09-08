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
import javax.naming.ldap.LdapName;

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
 */
@Local
public class LdapClientImpl implements LdapClient {

    // offset in 100 nanosecond intervals for 1601-01-01
    private final long TIME_OFFSET_AD = 116444768080000000L;

    @Resource
    SignalsConfig signalsConfig;

    private Hashtable<String, String> ldapEnv;

    private Logger logger = LoggerFactory.getLogger(LdapClientImpl.class.getName());

    @PostConstruct
    private void initialize() {
        ldapEnv = new Hashtable<>();
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put(Context.PROVIDER_URL, signalsConfig.getLdapContextProviderURL());
        ldapEnv.put(Context.REFERRAL, signalsConfig.getLdapContextReferral());
        ldapEnv.put(Context.SECURITY_AUTHENTICATION, signalsConfig.getLdapSecurityAuthentication());
        ldapEnv.put(Context.SECURITY_PRINCIPAL, signalsConfig.getLdapSecurityPrincipal());
        ldapEnv.put(Context.SECURITY_CREDENTIALS, signalsConfig.getLdapSecurityCredentials());
    }

    /**
     * Filter a set of distinguished names. Only DNs matching the filter pattern
     * will be kept. This prevents inflation of groups in the dependend system 
     * (SNB) and to leaking of internal information.
     *
     * @param distinguishedNames a set of distinguished names
     * @param filterType 
     * @return a filtered set of distinguished names according to the ldapFilterGroupDN setting
     */
    public Set<String> filterDNs(Set<String> distinguishedNames, FilterType type) {
        Set<String> results = new HashSet<> ();
        String[] filters = getDnFilters(type);

        for (String filter : filters) {
            try {
                LdapName filterName = new LdapName(filter);
                Iterator<String> dnIter = distinguishedNames.iterator();
                while(dnIter.hasNext()) {
                    String dn = dnIter.next();
                    try {
                        LdapName name = new LdapName(dn);

                        if (name.startsWith(filterName)) {
                            results.add(dn);
                        }
                     } catch (InvalidNameException f) {
                         logger.warn("filterGroup() invalid name: '{}'", dn);
                     }
                }
            } catch (InvalidNameException e) {
                logger.warn("filterGroup() invalid filter expression '{}'", filter);
            }
        }
        return results;
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
        return new Date();
    }

    private String[] getDnFilters(FilterType type) {
        switch(type) {
            case GROUP:
                return new String[] { signalsConfig.getLdapFilterGroupDN() };
            case ROLE:
                return new String[] { signalsConfig.getLdapFilterRoleDN() };
            case USER:
                return signalsConfig.getLdapBaseDNs().split(";");
        }
        throw new IllegalArgumentException("unrecognized type");
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
                group.setCreatedAt(getCreatedAt(attrs));
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
     * @return the list of users, who are members of that group, including nested memberships
     */
    public Set<String> getMembers(String groupDN) {
        Set<String> groupCache = new HashSet<> ();
        Set<String> users = new HashSet<> ();
        getMembers(users, groupCache, groupDN);
        return users;
    }

    private void getMembers(Set<String> users, Set<String> groupCache, String groupDN) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv);
            try {
                BasicAttribute membersAttr = (BasicAttribute) ctx
                        .getAttributes(groupDN)
                        .get(signalsConfig.getLdapAttrMembers());

                if (membersAttr != null) {
                    NamingEnumeration<?> membersEnumeration = membersAttr.getAll();
                    while (membersEnumeration.hasMore()) {
                        String dn = membersEnumeration.next().toString();
                        if (isGroup(dn)) {
                            if (groupCache.add(dn)) {
                                getMembers(users, groupCache, dn);
                            }
                        } else {
                            users.add(dn);
                        }
                    }
                }
            } finally {
                ctx.close();
            }
        } catch(Exception e) {
            logger.warn("getMembers() caught an Exception: ", (Throwable) e);
        }
    }

    /**
     * @param userDN a distinguished user name
     * @return the list of (nested) group memberships for the given user
     */
    public Set<String> getMemberships(String objDN) {
        Set<String> groups = new HashSet<> ();
        getMemberships(groups, objDN);
        return groups;
    }

    /**
     * recursively resolve memberships
     * @param groups the set to hold discovered distinguished group names
     * @param objDN the distinguished object, for which memberships are to be discovered
     */
    private void getMemberships(Set<String> groups, String objDN) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv);
            try {
                BasicAttribute memberOfAttr = (BasicAttribute) ctx
                        .getAttributes(objDN)
                        .get(signalsConfig.getLdapAttrMemberOf());

                if (memberOfAttr != null) {
                    NamingEnumeration<?> memberOfEnumeration = memberOfAttr.getAll();

                    while (memberOfEnumeration.hasMore()) {
                        String dn = memberOfEnumeration.next().toString();
                        if (groups.add(dn)) {
                            getMemberships(groups, dn); 
                        }
                    }
                }
            } finally {
                ctx.close();
            }
        } catch(Exception e) {
            logger.warn("getMemberships() caught an Exception: ", (Throwable) e);
        }
    }

    /**
     * @param userDN a distinguished user name
     * @return a corresponding User object
     */
    public User getUser(String userDN) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv); 
            try {
                Attributes attrs = ctx.getAttributes(userDN);

                User user = new User();
                user.setAlias(attrs.get(signalsConfig.getLdapAttrAlias()).get().toString());
                user.setCountry(signalsConfig.getUserAttrCountry());
                user.setCreatedAt(getCreatedAt(attrs));
                user.setEmail(attrs.get(signalsConfig.getLdapAttrEmail()).get().toString());
                user.setEnabled(getUserExpiration(attrs));
                user.setFirstName(attrs.get(signalsConfig.getLdapAttrFirstName()).get().toString());
                user.setLastName(attrs.get(signalsConfig.getLdapAttrLastName()).get().toString());
                user.setOrganization(signalsConfig.getUserAttrOrganization());
                user.setUserName(attrs.get(signalsConfig.getLdapAttrUserName()).get().toString());

                return user;
            } finally {
                ctx.close();
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

    /**
     * @param filterValue the value for the filter to search 
     * for specific users. Usually a email address. Can be null
     * to search for all users.
     * @return a set of distinguished user names.
     */
    public Set<String> getUsers(String filterValue) {
        Set<String> users = new HashSet<> ();
        String[] baseDNs = signalsConfig.getLdapBaseDNs().split(";");
        for(String baseDN : baseDNs) {
            getUsers(users, baseDN, filterValue);
        }
        return users;
    }

    private void getUsers(Set<String> users, String baseDN, String filterValue) {
        String filter = signalsConfig.getLdapFilterUsers();
        if (filterValue != null) {
            // select a specific user
            filter = signalsConfig.getLdapFilterUser().replaceAll("@", filterValue);
        }
        try {
            DirContext ctx = new InitialDirContext(ldapEnv); 
            try {
                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> search = ctx.search(baseDN, 
                        filter,
                        searchControls);

                while (search.hasMore()) {
                    String dn = search.next().getNameInNamespace();
                    users.add(dn);
                }
                search.close();
            } finally {
                ctx.close();
            }
        } catch(Exception e) {
            logger.warn("getUsers() caught an Exception: ", (Throwable) e);
        }
    }

    private boolean isGroup(String dn) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv);
            try {
                BasicAttribute objectClassAttr = (BasicAttribute) ctx
                        .getAttributes(dn)
                        .get(signalsConfig.getLdapAttrObjectClass());

                return objectClassAttr.contains(signalsConfig.getLdapAttrObjectClassGroup());
            } finally {
                ctx.close();
            }
        } catch(Exception e) {
            logger.warn("isGroup() caught an Exception: ", (Throwable) e);
        }
        return false;
    }
}
