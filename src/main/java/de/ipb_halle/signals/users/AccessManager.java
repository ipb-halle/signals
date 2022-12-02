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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private Logger logger = LoggerFactory.getLogger(AccessManager.class);

    private boolean dryRun;

    private Map<String, Group> dbGroups;
    private Map<String, Role> dbRoles;
    private Map<String, User> dbUsers;
    private Map<String, Group> groupsByDN;
    private Map<Integer, Group> groupsById;
    private Map<String, Group> ldapGroups;
    private Map<String, Role> ldapRoles;
    private Map<String, User> ldapUsers;
    private Map<String, Role> rolesByDN;
    private Map<Integer, Role> rolesById;
    private Map<String, Group> snbGroups;
    private Map<String, Role> snbRoles;
    private Map<String, User> snbUsers;

    /**
     * default constructor
     */
    public AccessManager() {
        dryRun = false;
    }

    /**
     * Look up the roles or role references of srcUser in the 
     * snbRoles map and assign the SNB roles to the destUser. 
     * @param srcUser the user object having role refreences
     * @param destUser the user object to which roles shall be added
     */
    private void addRoles(User srcUser, User destUser) {
        for (IRole role : srcUser.getRoles()) {
            Role dbRole = rolesById.get(role.getId());
            if (dbRole != null) {
                destUser.addRole(dbRole);
            }
        }
    }

    /**
     * Look up the groups or group references of srcUser in the 
     * snbGroups map and assign the SNB groups to the destUser.
     * @param srcUser the user object having group references
     * @param destUser the user object to which the groups shall be added
     */
    private void addSystemGroups(User srcUser, User destUser) {
        // add system group memberships...
        for (IGroup group : srcUser.getSystemGroups()) {
            Group dbGroup = groupsById.get(group.getId());
            if (dbGroup != null) {
                destUser.addSystemGroup(dbGroup);
            }
        }
    }

    /**
     * - fetch all users, groups and roles from SNB (map by name / email)
     * - fetch all users, groups and roles from DB (map by name / email and id)
     * - Synchronize SNB and DB objects. Users and groups, which have been newly
     *   discovered in SNB will be flagged immutable in the database, i.e.  their
     *   records will not be updated from LDAP by subsequent calls to the SNB tool.
     *   This is to protect admin accounts etc.
     * - obtain lists of managed users, groups and roles from LDAP (map by name / email and DN)
     * - match all LDAP objects (users, groups, roles) against their SNB counterparts,
     *   create or update the SNB and DB records along. Newly discovered LDAP objects
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
        syncSnbWithLdap();
    }

    private void ldapAssignGroup(User user, String groupDN) {
        Group group = groupsByDN.get(groupDN);
        if (group != null) {
            user.addSystemGroup(group);
        }
    }

    /**
     */
    private void ldapAssignRole(User user, String roleDN) {
        Role role = rolesByDN.get(roleDN);
        if (role != null) {
            role = snbRoles.get(role.getName());
            if (role != null) {
                user.addRole(role);
            } else {
                logger.info("Could not find SNB role for DN: {}", roleDN);
            }
        }
    }

    private void obtainDbObjects() {
        dbGroups = new HashMap<> ();
        groupsById = new HashMap<> ();
        for (Group group : groupDbService.load()) {
            logger.debug("Discovered DB group: {}", group.getName());
            dbGroups.put(group.getName(), group);
            groupsById.put(group.getId(), group);
        }

        dbRoles = new HashMap<> ();
        rolesById = new HashMap<> ();
        for (Role role : roleDbService.load()) {
            logger.debug("Discovered DB role: {}", role.getName());
            dbRoles.put(role.getName(), role);
            rolesById.put(role.getId(), role);
        }

        dbUsers = new HashMap<> ();
        for (User user : userDbService.load()) {
            logger.debug("Discovered DB user: {}", user.getEmail());
            dbUsers.put(user.getEmail(), user);
        }
    }

    private void obtainLdapGroups() {
        groupsByDN = new HashMap<> ();
        Set<String> groupDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> (); // should remain empty; content will be ignored
        ldapGroups = new HashMap<> ();
        ldapClient.getMembers(userDNs, groupDNs, config.getLdapManagedGroups(), false);
        for (String dn : groupDNs) {
            Group group = ldapClient.getGroup(dn);
            logger.debug("Discovered LDAP group: {}", group.getName());
            ldapGroups.put(group.getName(), group);
            groupsByDN.put(dn, group);
        }
    }

    private void obtainLdapRoles() {
        rolesByDN = new HashMap<> ();
        Set<String> roleDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> (); // should remain empty; content will be ignored
        ldapRoles = new HashMap<> ();
        ldapClient.getMembers(userDNs, roleDNs, config.getLdapManagedRoles(), false);
        for (String dn : roleDNs) {
            Role role = ldapClient.getRole(dn);
            logger.debug("Discovered LDAP role: {}", role.getName());
            ldapRoles.put(role.getName(), role);
            rolesByDN.put(dn, role);
        }
    }

    private void obtainLdapUsers() {
        Set<String> groupDNs = new HashSet<> (); // should remain empty; content will be ignored
        Set<String> userDNs = new HashSet<> ();
        ldapUsers = new HashMap<> ();
        ldapClient.getMembers(userDNs, groupDNs, config.getLdapManagedUsers(), false);
        for (String dn : userDNs) {
            User user = ldapClient.getUser(dn);
            logger.debug("Discovered LDAP user: {}", user.getEmail());
            Set<String> memberships = ldapClient.getMemberships(dn, true);
            for (String membershipDN : memberships) {
                ldapAssignRole(user, membershipDN);
                ldapAssignGroup(user,  membershipDN);
            }
            ldapUsers.put(user.getEmail(), user);
        }
    }

    private void obtainLdapObjects() {
        obtainLdapGroups();
        obtainLdapRoles();
        obtainLdapUsers();
    }

    private void obtainSnbObjects() {
        snbGroups = new HashMap<> ();
        for(Group group : groupRestService.doGetGroups()) {
            logger.debug("Discovered SNB group: {}", group.getName());
            snbGroups.put(group.getName(), group);
        }

        snbRoles = new HashMap<> ();
        for(Role role : roleRestService.doGetRoles()) {
            logger.debug("Discovered SNB role: {}", role.getName());
            snbRoles.put(role.getName(), role);
        }

        snbUsers = new HashMap<> ();
        for(User user : userRestService.doGetUsers(null, true)) {
            userRestService.doGetSystemGroupMemberships(user);
            logger.debug("Discovered SNB user: {}", user.getEmail());
            snbUsers.put(user.getEmail(), user);
        }
    }

    public void setDryRun(boolean d) {
        dryRun = d;
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
                if (! dryRun) {
                    groupDbService.save(snbGroup);
                }
            } else {
                snbGroup.setImmutable(false);
            }
        }

        for (String roleName : snbRoles.keySet()) {
            Role snbRole = snbRoles.get(roleName);
            Role dbRole = dbRoles.remove(roleName);
            if (! dryRun) {
                roleDbService.save(snbRole);
            }
        }

        for (String userName : snbUsers.keySet())  {
            User snbUser = snbUsers.get(userName);
            User dbUser = dbUsers.remove(userName);
            if ((dbUser == null) || dbUser.isImmutable()) {
                if (! dryRun) {
                    userDbService.save(snbUser);
                }
            } else {
                snbUser.setImmutable(false);
            }
        }
    }

    /**
     * synchronize SNB with LDAP; add new groups or update
     * group description. Other group attributes cannot be
     * changed.
     * @param ldapGroup the group as discovered in LDAP
     * @param snbGroup the group as discovered in SNB or null
     */
    private void syncLdapGroup(Group ldapGroup, Group snbGroup) {
        if (snbGroup == null) {
            if (! dryRun) {
                // save new group
                Group group = groupRestService.doCreateGroup(ldapGroup);
                group.setImmutable(false);
                snbGroups.put(group.getName(), group);
                groupDbService.save(group);
                groupsById.put(group.getId(), group);
            }
        } else {
            if (! snbGroup.isImmutable()) {
                if (! dryRun) {
                    snbGroup.setDescription(ldapGroup.getDescription());
                    groupRestService.doUpdateGroup(snbGroup);
                    snbGroups.put(snbGroup.getName(), snbGroup);
                    groupDbService.save(snbGroup);
                    groupsById.put(snbGroup.getId(), snbGroup);
                }
            }
        }
    }

    /**
     * synchronize SNB with LDAP; add new users or update
     * existing users. Only the attributes alias, country, email,
     * enabled, firstName, lastName and organization can be
     * changed.
     * @param ldapUser the user as discovered in LDAP
     * @param snbUser the user as discovered in SNB or null for new user. The immutable
     * flag of snbUser may be cleared (false), if the user has been found mutable in the
     * database (@see <code>syncDbWithSnb</code>)
     */
    private void syncLdapUser(User ldapUser, User snbUser) {
        if (snbUser == null) {
            if (! dryRun) {
                User user = userRestService.doCreateUser(ldapUser);
                addRoles(ldapUser, user);
                addSystemGroups(ldapUser, user);
                user.setImmutable(false);
                userDbService.save(user);
            }
        } else {
            if (! snbUser.isImmutable()) {
                if (! dryRun) {
                    snbUser.setAlias(ldapUser.getAlias());
                    snbUser.setCountry(ldapUser.getCountry());
                    snbUser.setEmail(ldapUser.getEmail());
                    snbUser.setEnabled(true);
                    snbUser.setFirstName(ldapUser.getFirstName());
                    snbUser.setLastName(ldapUser.getLastName());
                    snbUser.setOrganization(ldapUser.getOrganization());
                    snbUser.clearRoles();
                    addRoles(ldapUser, snbUser);
                    snbUser.clearSystemGroups();
                    addSystemGroups(ldapUser, snbUser);
                    User user = userRestService.doUpdateUser(snbUser);
                    user.setImmutable(false);
                    snbUsers.put(user.getEmail(), user);
                    userDbService.save(user);
                }
            }
        }
    }

    private void syncSnbWithLdap() {
        for(String groupName : ldapGroups.keySet()) {
            Group ldapGroup = ldapGroups.get(groupName);
            Group snbGroup = snbGroups.get(groupName);
            syncLdapGroup(ldapGroup, snbGroup);
        }

        /* new roles can not be created from LDAP */

        for(String userName : ldapUsers.keySet()) {
            User ldapUser = ldapUsers.get(userName);
            User snbUser = snbUsers.get(userName);
            syncLdapUser(ldapUser, snbUser);
        }
    }
}
