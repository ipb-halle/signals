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

import java.util.HashSet;
import java.util.Hashtable;
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

/** 
 * Ldap client reader for Signals tool 
 */
@Local
public class LdapClientImpl implements LdapClient {

    @Resource
    SignalsConfig signalsConfig;

    private Hashtable<String, String> ldapEnv;

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
     * Filter the group memberships. Only matching groups kept to
     * prevent inflation of groups and to leaking of internal information.
     *
     * @param dn the distinguished name of the group
     * @return true if the group dn passes the filter
     */
    private boolean filterGroup(String dn) {
        String filter = signalsConfig.getLdapFilterGroupDN();

        try {
            LdapName name = new LdapName(dn);

            try {
                LdapName filterName = new LdapName(filter);

                return name.startsWith(filterName);
            } catch (InvalidNameException f) {
                // this.logger.warn("filterGroup() invalid filter expression:" + filter);
                f.printStackTrace();
                return false;
            }
        } catch (InvalidNameException e) {
            // this.logger.warn("filterGroup() invalid name: " + dn);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param groupDN a distinguished group name
     * @return a corresponding Group object
     */
    public Group getGroup(String groupDN) {
        return null;
    }

    /**
     * @param groupDN a distinguished group name
     * @return the list of users, who are members of that group, including nested memberships
     */
    public Set<String> getMembers(String groupDN) {
        return null;
    }

    /**
     * @param userDN a distinguished user name
     * @return a list of (nested) group memberships for the given user
     */
    public Set<String> getMemberships(String objDN) {
        Set<String> groups = new HashSet<> ();
        getMemberships(groups, objDN);
        return groups;
    }

    private void getMemberships(Set<String> groups, String objDN) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv);
            try {
                BasicAttribute memberOfAttr =  (BasicAttribute) ctx
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
            e.printStackTrace();
        }
    }

    /**
     * @param userDN a distinguished user name
     * @return a corresponding User object
     */
    public User getUser(String userDN) {
        return null;
    }

    /**
     * @param baseDN the base DN for searching users
     * @return a list of distinguished user names
     */
    public Set<String> getUsers(String baseDN) {
        try {
            DirContext ctx = new InitialDirContext(ldapEnv);
            try {
                Set<String> users = new HashSet<> ();
                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> search = ctx.search(baseDN, 
                        signalsConfig.getLdapFilterUsers(),
                        searchControls);

                while (search.hasMore()) {
                    String dn = search.next().getNameInNamespace();
                    users.add(dn);
                    System.out.println(dn);
//                  users.add(search.next().getNameInNamespace());
                }
                search.close();
                return users;
            } finally {
                ctx.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
