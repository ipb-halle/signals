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
    private Role standardUserRole;

    private boolean dryRun;

    /**
     * default constructor
     */
    public AccessManager() {
        dryRun = false;
    }

    /**
     * clean user roles for a mutable user: remove all roles
     * which are managed by LDAP
     */
    private void cleanUserLdapRoles(User user) {
        Iterator<IRole> iter = user.getRoles().iterator();
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
        if (! dryRun) {
            logger.debug("creating new user from LDAP: {}", ldapUser.getUserName());
            User snbUser = userRestService.doCreateUser(ldapUser);
            snbUser.setMutable(true);
            userDbService.save(snbUser);
            return userDbService.loadById(snbUser.getId());
        }
        logger.debug("DRY RUN: skipping user creation: {}", ldapUser.getUserName());
        return ldapUser;
    }

    /**
     * @param user
     * @return the set of LDAP managed groups for given user.
     */
    private Set<Group> getUserLdapGroups(User user) {
        Set<Group> ldapGroups = new HashSet<> ();
        Iterator<IGroup> iter = user.getSystemGroups().iterator();
        while (iter.hasNext()) {
            IGroup iGroup = iter.next();
            if ((iGroup instanceof Group) && ((Group) iGroup).isLdapGroup()) {
                ldapGroups.add((Group) iGroup);
            }
        }
        return ldapGroups;
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
        ldapClient.getMembers( new HashSet<> (), groupDNs, config.getLdapManagedGroups(), false);
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
                    if (! dryRun) {
                        groupDbService.save(dbGroup);
                    }
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
        ldapClient.getMembers(new HashSet<> (), roleDNs, config.getLdapManagedRoles(), false);
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
                    if (! dryRun) {
                        roleDbService.save(dbRole);
                    }
                    rolesByDN.put(dn, dbRole);
                }
            }
        }
    }

    private void resolveRoleReferences(User snbUser) {
        Set<IRole> newRoles = new HashSet<> ();
        for (IRole iRole : snbUser.getRoles()) {
            if (iRole instanceof RoleReference) {
                newRoles.add(roleDbService.loadById(iRole.getId()));
            } else {
                newRoles.add(iRole);
            }
        }
        snbUser.setRoles(newRoles);
    }

    private void resolveGroupReferences(User snbUser) {
        Set<IGroup> newGroups = new HashSet<> ();
        for (IGroup iGroup : snbUser.getSystemGroups()) {
            if (iGroup instanceof GroupReference) {
                newGroups.add(groupDbService.loadById(iGroup.getId()));
            } else {
                newGroups.add(iGroup);
            }
        }
        snbUser.setSystemGroups(newGroups);
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
                    } else {
                        logger.debug("DRY RUN: group {} not updated despite modification", dbGroup.getName());
                    }
                }
            }
        }

        // mark groups not found in SNB as deleted
        for (Group group : groupsFromDb.values()) {
            if (! dryRun) {
                logger.debug("group {} not found in SNB - marking as deleted", group.getName());
                group.setDeleted(true);
                groupDbService.save(group);
            } else {
                logger.debug("DRY RUN: group {} not found in SNB", group.getName());
            }
        }
    }

    private void syncDbRolesFromSnb() {
        Map<String, Role> rolesFromDb = roleDbService.loadMappedById(new HashMap<> ());

        for(Role snbRole : roleRestService.doGetRoles()) {
            logger.debug("Discovered SNB role: {}", snbRole.getName());
            Role dbRole = rolesFromDb.remove(snbRole.getId());
            if (snbRole.getName().equals(config.getStandardUserRoleName())) {
                standardUserRole = snbRole;
            }
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
            if (! dryRun) {
                logger.debug("role {} not found in SNB - marking as deleted", dbRole.getName());
                dbRole.setDeleted(true);
                roleDbService.save(dbRole);
            } else {
                logger.debug("DRY RUN: role {} not found in SNB", dbRole.getName());
            }
        }
    }

    private void syncDbUserFromSnb(User snbUser) {
        User dbUser = userDbService.loadById(snbUser.getId());
        if (dbUser == null) {
            logger.debug("Discovered new SNB user: {}", snbUser.getUserName());
            if (! dryRun) {
                snbUser.setMutable(false);
                userDbService.save(snbUser);
            }
        } else {
            resolveRoleReferences(snbUser);
            resolveGroupReferences(snbUser);
            if (snbUser.isModified(CompareType.SNB, dbUser)) {
                logger.debug("Discovered modified user {} in SNB", snbUser.getUserName());
                if (! dryRun) {
                    dbUser.applyChangesFromSnb(snbUser);
                    userDbService.save(dbUser);
                }
            }
        }
    }

    private void syncDbUsersFromSnb() {
        // enabled users
        for( User snbUser : userRestService.doGetUsers(null, true)) {
            syncDbUserFromSnb(snbUser);
        }

        // disabled users
        for (User snbUser : userRestService.doGetUsers(null, false)) {
            syncDbUserFromSnb(snbUser);
        }
    }

    /**
     * synchronize a single user from LDAP
     */
    private User syncUserFromLdap(String userDN) {
        User ldapUser = ldapClient.getUser(userDN);
        ldapUser.addRole(standardUserRole);
        User dbUser = userDbService.loadByUserName(ldapUser.getUserName());
        if (dbUser == null) {
            logger.debug("Discovered new user in LDAP: {}", ldapUser.getUserName());
            dbUser = createUserFromLdap(ldapUser);
        }

        if (dbUser.isMutable()) {
            // check for changes and apply if necessary
            syncUserLdapChanges(userDN, ldapUser, dbUser);
        } else {
            logger.info("Leaving immutable user {} untouched", dbUser.getUserName());
        }
        return dbUser;
    }

    /**
     * compute necessary changes in user roles and group
     * memberships; save them to DB and SNB
     */
    private void syncUserLdapChanges(String userDN, User ldapUser, User dbUser) {
        ldapUser.setId(dbUser.getId());
        ldapUser.getRoles().addAll(dbUser.getRoles());
        ldapUser.getSystemGroups().addAll(dbUser.getSystemGroups());
        cleanUserLdapRoles(ldapUser);
        Set<Group> groupsToRemove = getUserLdapGroups(ldapUser);
        Set<Group> groupsToAdd = new HashSet<> ();

        syncUserLdapGroupsAndRoles(userDN, ldapUser,
                groupsToAdd, groupsToRemove);
        ldapUser.getSystemGroups()
                .removeAll(groupsToRemove);

        if (dbUser.isModified(CompareType.LDAP, ldapUser)) {
            dbUser.applyChangesFromLdap(ldapUser);
            if (! dryRun) {
                logger.debug("Updating user from LDAP: {}", dbUser.getUserName());
                userDbService.save(dbUser);
                userRestService.doUpdateUser(dbUser, groupsToAdd, groupsToRemove);
            } else {
                logger.debug("DRY RUN: skipping update of LDAP user {}", dbUser.getUserName());
            }
        }
    }

    private void syncUserLdapGroupsAndRoles(String userDN, User ldapUser,
            Set<Group> groupsToAdd, Set<Group> groupsToRemove) {

        Set<String> memberships = ldapClient.getMemberships(userDN, true);
        for (String membershipDN : memberships) {
            Role dbRole = rolesByDN.get(membershipDN);
            Group dbGroup = groupsByDN.get(membershipDN);
            if (dbRole != null) {
                ldapUser.addRole(dbRole);
            }
            if (dbGroup != null) {
                groupsToRemove.remove(dbGroup);
                if (ldapUser.addSystemGroup(dbGroup)) {
                    groupsToAdd.add(dbGroup);
                }
            }
        }
    }

    /**
     * syncronize multiple users from LDAP
     */
    private void syncUsersFromLdap() {
        Set<String> userDNs = new HashSet<> ();

        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(User.USER_MUTABLE, Boolean.TRUE);
        cmap.put(User.USER_ENABLED, Boolean.TRUE);
        Map<String, User> ldapUsersById = userDbService.loadMappedById(cmap);

        // nesting allowed here, i.e. we can assign entire groups to SNB
        ldapClient.getMembers(userDNs, new HashSet<> (), config.getLdapManagedUsers(), true);
        for (String dn : userDNs) {
            User dbUser = syncUserFromLdap(dn);
            ldapUsersById.remove(dbUser.getId());
        }

        // disable mutable users not found in LDAP
        for (User user : ldapUsersById.values()) {
            if (user.isMutable()) {
                userRestService.doDisableUser(user);
                user.setEnabled(false);
                user.setRoles(new HashSet<> ());
                user.setSystemGroups(new HashSet<> ());
                userDbService.save(user);
            }
        }
    }
}
