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
import de.ipb_halle.signals.users.LdapClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;


/** 
 * Manager for signals roles
 */

@Stateless
public class AccessManager {

    @Inject
    private SignalsConfig config;

    @Inject
    private GroupDbService groupDbService;

    @Inject
    private GroupRestService groupRestService;

    @Inject
    private LdapClient ldapClient;

    @Inject
    private RoleDbService roleDbService;

    @Inject
    private RoleRestService roleRestService;

    @Inject
    private UserDbService userDbService;

    @Inject
    private UserRestService userRestService;


    private Map<String, Group> dbGroups;
    private Map<String, Role> dbRoles;
    private Map<String, UserEntity> dbUsers;
    private Map<String, Group> ldapGroups;
    private Map<String, Role> ldapRoles;
    private Map<String, UserEntity> ldapUsers;
    private Map<String, Group> snbGroups;
    private Map<String, Role> snbRoles;
    private Map<String, UserEntity> snbUsers;
    

    /**
     * - fetch all users, groups and roles from SNB 
     * - fetch all users, groups and roles from DB 
     * - Synchronize SNB and DB objects. Users and groups, which have been newly 
     *   discovered in SNB will be flagged immutable in the database, i.e.  their 
     *   records will not be updated from LDAP by subsequent calls to the SNB tool. 
     *   This is to protect admin accounts etc.
     * - obtain lists of managed users, groups and roles from LDAP
     * - both, SNB and LDAP objects are mapped by their name (roles, groups) or 
     *   email address (users) to prepare for the next step (matching)
     * - match all LDAP objects (users, groups, roles) against their DB counterparts,
     *   create or update the DB and SNB records along. Newly discovered LDAP objects 
     *   will be flagged as mutable - their records can be updated and deleted in 
     *   subsequent calls of the SNB tool.
     * - delete all obsolete users and group or role memberships (i.e. records from DB 
     *   which do not occur in LDAP and are not flagged as immutable)
     */
    public void manageAccess() {
        obtainSnbObjects();
        obtainDbObjects();
        syncDbWithSnb();

        obtainLdapObjects();
    }

    private void obtainDbObjects() {
        dbGroups = new HashMap<> ();
        for (Group group : groupDbService.load()) {
            dbGroups.put(group.getName(), group);
        }

        dbRoles = new HashMap<> ();
        for (Role role : roleDbService.load()) {
            dbRoles.put(role.getName(), role);
        }

        dbUsers = new HashMap<> ();
        for (UserEntity user : userDbService.load()) {
            dbUsers.put(user.getEmail(), user);
        }
    }

    private void obtainLdapGroups() {
        Set<String> groupDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> ();
        ldapGroups = new HashMap<> ();
        ldapClient.getMembers(userDNs, groupDNs, config.getLdapManagedGroups(), false);
        for (String dn : groupDNs) {
            Group group = ldapClient.getGroup(dn);
            ldapGroups.put(group.getName(), group); 
        }
    }

    private void obtainLdapRoles() {
        Set<String> roleDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> ();
        ldapRoles = new HashMap<> ();
        ldapClient.getMembers(userDNs, roleDNs, config.getLdapManagedRoles(), false);
        for (String dn : roleDNs) {
            Role role = ldapClient.getRole(dn);
            ldapRoles.put(role.getName(), role);
        }
    }

    private void obtainLdapUsers() {
        Set<String> groupDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> ();
        ldapUsers = new HashMap<> ();
        ldapClient.getMembers(userDNs, groupDNs, config.getLdapManagedUsers(), false);
        for (String dn : userDNs) {
            UserEntity user = ldapClient.getUserEntity(dn);
            ldapUsers.put(user.getEmail(), user);
        }
    }

    private void obtainLdapObjects() {
        obtainLdapGroups();
        obtainLdapRoles();
        obtainLdapUsers();
    }

    private void obtainSnbObjects() {
        snbRoles = new HashMap<> ();
        for(Role role : roleRestService.doGetRoles()) {
            snbRoles.put(role.getName(), role);
        }

        snbGroups = new HashMap<> ();
        for(Group group : groupRestService.doGetGroups()) {
            snbGroups.put(group.getName(), group);
        }

        snbUsers = new HashMap<> ();
        for(UserEntity user : userRestService.doGetUsers(null, true)) {
            userRestService.doGetSystemGroupMemberships(user);
            snbUsers.put(user.getEmail(), user);
        }
    }

    /**
     * Synchronized the snb objects with database objects, i.e.
     * storing new objects in the database. All entries from the SNB map 
     * will be persisted to the database and their corresponding entries 
     * removed from the DB map.  All remaining entries in the DB map are 
     * no longer present in Signals Notebook.
     */
    private void syncDbWithSnb() {
        for (String groupName : snbGroups.keySet()) {
            Group snbGroup = snbGroups.get(groupName);
            Group dbGroup =  dbGroups.remove(groupName);

            if ((dbGroup == null) || dbGroup.isImmutable()) {
                groupDbService.save(snbGroup);
            }
        }

        for (String roleName : snbRoles.keySet()) {
            Role snbRole = snbRoles.get(roleName);
            Role dbRole = dbRoles.remove(roleName);
            roleDbService.save(snbRole);
        }

        for (String userName : snbUsers.keySet())  {
            UserEntity snbUser = snbUsers.get(userName);
            UserEntity dbUser = dbUsers.remove(userName);
            if ((dbUser == null) || dbUser.isImmutable()) {
                userDbService.save(snbUser);
            }
        }
    }
}
