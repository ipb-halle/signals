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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manager for signals roles
 */

@Stateless
public class AccessManager {

    @Resource
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

    private Map<String, Group> groupsByDN;
    private Map<String, Role> rolesByDN;

    private boolean dryRun;

    /**
     * default constructor
     */
    public AccessManager() {
        dryRun = false;
    }


    /**
     * clean group memberships for a mutable user: remove all
     * LDAP managed groups from user
     */
    private void cleanUserLdapGroups(User dbUser) {
        Iterator<IGroup> iter = dbUser.getSystemGroups().iterator();
        while (iter.hasNext()) {
            IGroup iGroup = iter.next();
            if ((iGroup instanceof Group) && ((Group) iGroup).isLdapGroup()) {
                iter.remove();
            }
        }
    }

    /**
     * clean user roles for a mutable user: remove all roles
     * which are managed by LDAP
     */
    private void cleanUserLdapRoles(User dbUser) {
        Iterator<IRole> iter = dbUser.getRoles().iterator();
        while (iter.hasNext()) {
            IRole iRole = iter.next();
            if ((iRole instanceof Role) && ((Role) iRole).isLdapRole()) {
                iter.remove();
            }
        }
    }

    /**
     * create user in SNB, synchronize Db with new SNB user
     */
    private User createUserFromLdap(User ldapUser) {
        // ToDo: handle this use case
        return null;
    }

    /**
     * manage users, groups and roles
     */
    public void manageAccess() {
        syncDbFromSnb();
        syncSnbFromLdap();
    }

    /**
     * NOTE: currently we CANNOT manage group shares or group
     * associations. We therefore refrain from creating or updating
     * groups via LDAP as manual intervention would be required
     * anyway.
     */
    private void obtainLdapGroups() {
        groupsByDN = new HashMap<> ();
        Set<String> groupDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> (); // should remain empty; content will be ignored
        ldapClient.getMembers(userDNs, groupDNs, config.getLdapManagedGroups(), false);
        for (String dn : groupDNs) {
            Group ldapGroup = ldapClient.getGroup(dn);
            Group dbGroup = groupDbService.loadByName(ldapGroup.getName());
            if (dbGroup == null) {
                logger.warn("LDAP group {} has no SNB / DB equivalient.", ldapGroup.getName());
            } else {
                if (dbGroup.isDeleted()) {
                    logger.warn("Deleted group cannot be managed via LDAP: {}", ldapGroup.getName());
                } else {
                    logger.debug("Group {} is managed via LDAP", ldapGroup.getName());
                    dbGroup.setLdapGroup(true);
                    groupDbService.save(dbGroup);
                    groupsByDN.put(dn, dbGroup);
                }
            }
        }
    }

    /**
     * NOTE: one cannot create or modify roles with this tool. Only
     * assigning roles to users (or removing from) is supported.
     */
    private void obtainLdapRoles() {
        rolesByDN = new HashMap<> ();
        Set<String> roleDNs = new HashSet<> ();
        Set<String> userDNs = new HashSet<> (); // should remain empty; content will be ignored
        ldapClient.getMembers(userDNs, roleDNs, config.getLdapManagedRoles(), false);
        for (String dn : roleDNs) {
            Role ldapRole = ldapClient.getRole(dn);
            Role dbRole = roleDbService.loadByName(ldapRole.getName());
            if (dbRole == null) {
                logger.warn("LDAP role {} has no SNB / DB equivalent.", ldapRole.getName());
            } else {
                if (dbRole.isDeleted()) {
                    logger.warn("Deleted role cannot be managed via LDAP: {}", ldapRole.getName());
                } else {
                    logger.debug("Role {} is managed via LDAP", ldapRole.getName());
                    dbRole.setLdapRole(true);
                    roleDbService.save(dbRole);
                    rolesByDN.put(dn, dbRole);
                }
            }
        }
    }

    public void setDryRun(boolean d) {
        dryRun = d;
    }

    private void syncDbFromSnb() {
        syncDbRolesFromSnb();
        syncDbGroupsFromSnb();
        syncDbUsersFromSnb();
    }

    private void syncSnbFromLdap() {
        obtainLdapRoles();
        obtainLdapGroups();
        syncUsersFromLdap();
    }

    private void syncDbGroupsFromSnb() {
        Map<String, Group> groupsFromDb = groupDbService.loadMappedById(new HashMap<> ());

        for(Group snbGroup : groupRestService.doGetGroups()) {
            logger.debug("Discovered SNB group: {}", snbGroup.getName());
            Group dbGroup = groupsFromDb.remove(snbGroup.getId());
            if (dbGroup == null) {
                if (! dryRun) {
                    groupDbService.save(snbGroup);
                }
            } else {
                if (snbGroup.isModified(CompareType.SNB, dbGroup)) {
                    if (! dryRun) {
                        dbGroup.applyChangesFromSnb(snbGroup);
                        dbGroup.setDeleted(false);
                        groupDbService.save(dbGroup);
                    }
                }
            }
        }

        // mark groups not found in SNB as deleted
        for (Group group : groupsFromDb.values()) {
            group.setDeleted(true);     
            groupDbService.save(group);
        }
    }

    private void syncDbRolesFromSnb() {
        Map<String, Role> rolesFromDb = roleDbService.loadMappedById(new HashMap<> ());

        for(Role snbRole : roleRestService.doGetRoles()) {
            logger.debug("Discovered SNB role: {}", snbRole.getName());
            Role dbRole = rolesFromDb.remove(snbRole.getId());
            if (dbRole == null) {
                if (! dryRun) {
                    roleDbService.save(snbRole);
                }
            } else {
                if (snbRole.isModified(dbRole)) {
                    if (! dryRun) {
                        dbRole.applyChangesFromSnb(snbRole);
                        dbRole.setDeleted(false);
                        roleDbService.save(dbRole);
                    }
                }
            }
        }

        // mark roles not found in SNB as deleted
        for (Role dbRole : rolesFromDb.values()) {
            dbRole.setDeleted(true);
            roleDbService.save(dbRole);
        }
    }

    private void syncDbUsersFromSnb() {
        for(User snbUser : userRestService.doGetUsers(null, true)) {
            logger.debug("Discovered SNB user: {}", snbUser.getEmail());
            userRestService.doGetSystemGroupMemberships(snbUser);
            User dbUser = userDbService.loadById(snbUser.getId());
            if (dbUser == null) {
                if (! dryRun) {
                    snbUser.setMutable(false);
                    userDbService.save(snbUser);
                }
            } else {
                if (snbUser.isModified(CompareType.SNB, dbUser)) {       
                    if (! dryRun) {
                        dbUser.applyChangesFromSnb(snbUser);
                        userDbService.save(dbUser);
                    }
                }
            }
        }
    }

    /**
     * synchronize a single user from LDAP
     */
    private void syncUserFromLdap(String userDN) {
        User ldapUser = ldapClient.getUser(userDN);
        User dbUser = userDbService.loadByUserName(ldapUser.getUserName());
        if (dbUser == null) {
            logger.debug("Discovered new user in LDAP: {}", ldapUser.getEmail());
            dbUser = createUserFromLdap(ldapUser);
        }

        if (dbUser.isMutable()) {
            cleanUserLdapRoles(dbUser);
            cleanUserLdapGroups(dbUser);
            Set<String> memberships = ldapClient.getMemberships(userDN, true);
            for (String membershipDN : memberships) {
                Role dbRole = rolesByDN.get(membershipDN);
                Group dbGroup = groupsByDN.get(membershipDN);
                if (dbRole != null) {
                    dbUser.addRole(dbRole);
                }
                if (dbGroup != null) {
                    dbUser.addSystemGroup(dbGroup);
                }
            }
            userDbService.save(dbUser);
            userRestService.doUpdateUser(dbUser);
        } else {
            logger.info("Leaving immutable user {} untouched", dbUser.getEmail());
        }
    }

    /**
     * syncronize multiple users from LDAP
     */
    private void syncUsersFromLdap() {
        Set<String> groupDNs = new HashSet<> (); // should remain empty; content will be ignored
        Set<String> userDNs = new HashSet<> ();

        Set<User> ldapUsers = new HashSet<> ();

        ldapClient.getMembers(userDNs, groupDNs, config.getLdapManagedUsers(), false);
        for (String dn : userDNs) {
            syncUserFromLdap(dn);
        }
    }

}
