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
import de.ipb_halle.signals.RuntimeConfig;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The UserManager can fetch users from Signals Notebook and
 * store them in an SQL database. This class provides some
 * convenience Methods for the AccessManager class.
 */

@Stateless
public class UserManager {

    @Resource
    private SignalsConfig config;

    @Inject
    private LdapClient ldapClient;

    @Inject
    private GroupManager groupManager;

    @Inject
    private RoleManager roleManager;

    @Inject
    private UserDbService userDbService;

    @Inject
    private UserRestService userRestService;

    private Logger logger = LoggerFactory.getLogger(UserManager.class);

    public User getUser(String id) {
        return userDbService.loadById(id);
    }

    private String[] getLicenses() {
        return config.getUserAttrLicenses().split(",");
    }

    public boolean doActivateUser(UserSynchronizationContext context, User user) {
        if (context.runtimeConfig.updateSNB) {
            if (userRestService.doActivateUser(user, getLicenses())) {
                user.setEnabled(true);
                save(context.runtimeConfig, user);
            } else {
                return false;
            }
        } else {
            logger.trace("DRY RUN: skipped SNB ACTIVATE for user: {}", user.getUserName());
        }
        context.report.addContent(AccessManager.SECTION_NEW_USERS, user.getUserName());
        context.reportRecords++;
        return true;
    }

    public User doCreateUser(UserSynchronizationContext context, User user) {
        if (context.runtimeConfig.updateSNB) {
            logger.info("Creating new SNB user: {}", user.getUserName());
            User snbUser = userRestService.doCreateUser(user, getLicenses());
            snbUser.setMutable(true);
            save(context.runtimeConfig, snbUser);
            user = userDbService.loadById(snbUser.getId());
        } else {
            logger.trace("DRY RUN: skipped SNB CREATE for user: {}", user.getUserName());
        }
        context.report.addContent(AccessManager.SECTION_NEW_USERS, user.getUserName());
        context.reportRecords++;
        return user;
    }

    public void doDisableUser(UserSynchronizationContext context, User user) {
        if (context.runtimeConfig.updateSNB) {
            userRestService.doDisableUser(user, getLicenses());
            user.setEnabled(false);
            user.setRoles(new HashSet<> ());
            user.setSystemGroups(new HashSet<> ());
            save(context.runtimeConfig, user);
        } else {
            logger.trace("DRY RUN: skipped SNB DISABLE for user: {}", user.getUserName());
        }
        context.report.addContent(AccessManager.SECTION_DISABLED_USERS, user.getUserName());
        context.reportRecords++;
    }

    public void doUpdateUser(UserSynchronizationContext context, User user) {
        if (context.runtimeConfig.updateSNB) {
            User restUser = userRestService.doUpdateUser(user,
                        context.groupsToAdd,
                        context.groupsToRemove);
            if (restUser != null) {
                save(context.runtimeConfig, user);
            }
        } else {
            logger.trace("DRY RUN: skipped SNB UPDATE for user: {}", user.getUserName());
        }
    }

    public void save(RuntimeConfig runtimeConfig, User user) {
        if (runtimeConfig.updateDb) {
            userDbService.save(user);
        } else {
            logger.trace("DRY RUN: skipped DB update for user: {}", user.getUserName());
        }
    }

    /**
     * synchronize Db from SNB
     */
    public void syncDbUsersFromSnb(RuntimeConfig runtimeConfig) {
        // enabled users
        for( User snbUser : userRestService.doGetUsers(null, true)) {
            syncDbUserFromSnb(runtimeConfig, snbUser);
        }

        // disabled users
        for (User snbUser : userRestService.doGetUsers(null, false)) {
            syncDbUserFromSnb(runtimeConfig, snbUser);
        }
    }

    private void syncDbUserFromSnb(RuntimeConfig runtimeConfig, User snbUser) {
        logger.trace("Processing SNB user: {}", snbUser.getUserName());
        User dbUser = userDbService.loadById(snbUser.getId());
        if (dbUser == null) {
            logger.info("SNB user is NEW: {}", snbUser.getUserName());
            snbUser.setMutable(false);
            save(runtimeConfig, snbUser);
        } else {
            roleManager.resolveRoleReferences(snbUser);
            groupManager.resolveGroupReferences(snbUser);
            if (snbUser.isModified(CompareType.SNB, dbUser)) {
                logger.debug("SNB user is modified: {}", snbUser.getUserName());
                dbUser.applyChangesFromSnb(snbUser);
                save(runtimeConfig, dbUser);
            }
        }
    }

    /**
     * syncronize multiple users from LDAP
     */
    public void syncUsersFromLdap(UserSynchronizationContext context) {
        try {
            Set<String> userDNs = new HashSet<> ();
            Set<String> deniedUsers = new HashSet<> ();
            ldapClient.getMembers(deniedUsers, new HashSet<> (), config.getLdapDeniedUsers(), true);

            Map<String, Object> cmap = new HashMap<> ();
            cmap.put(User.USER_MUTABLE, Boolean.TRUE);
            cmap.put(User.USER_ENABLED, Boolean.TRUE);
            Map<String, User> removeMap = userDbService.loadMappedById(cmap);

            // nesting allowed here, i.e. we can assign entire groups to SNB
            ldapClient.getMembers(userDNs, new HashSet<> (), config.getLdapManagedUsers(), true);
            for (String dn : userDNs) {
                if (! deniedUsers.contains(dn)) {
                    logger.trace("Processing LDAP user DN: {}", dn);
                    syncUserFromLdap(context, removeMap, dn);
                } else {
                    logger.trace("Skipping denied user: {}", dn);
                }
            }
            // disable remaining mutable users
            disableMutableUsers(context, removeMap.values());
        } catch (Exception e) {
            logger.warn("syncUsersFromLdap() caught an exception: ", (Throwable) e);
            context.report.addContent(AccessManager.SECTION_ERRORS, "LDAP user synchronization failed: " + e.getMessage());
            context.reportRecords++;
            context.reportAlert = true;
        }
    }

    private void disableMutableUsers(UserSynchronizationContext context, Collection<User> mutableUsers) {
        for (User user : mutableUsers) {
            logger.info("Disabling mutable user not found in LDAP: {}", user.getUserName());
            doDisableUser(context, user);
        }
    }

    /**
     * synchronize a single user from LDAP
     */
    private void syncUserFromLdap(
            UserSynchronizationContext context,
            Map<String, User> removeMap,
            String userDN) throws Exception {

        User ldapUser = loadUserFromLdap(context, userDN);
        if (ldapUser.isEnabled()) {
            User dbUser = loadOrCreateDbUser(context, ldapUser, removeMap);
            if (dbUser.isMutable()) {
                if (! dbUser.isEnabled()) {
                    if (! doActivateUser(context, dbUser)) {
                        logger.info("Activation failed for user: {}", dbUser.getUserName());
                        return;
                    }
                }
                syncUserLdapChanges(context, userDN, ldapUser, dbUser);
            } else {
                logger.debug("Cannot update immutable user: {}", dbUser.getUserName());
            }
        } else {
            logger.debug("Refusing to operate on expired LDAP user: {}", ldapUser.getUserName());
        }
    }

    private User loadUserFromLdap(
                UserSynchronizationContext context,
                String userDN) throws Exception {

        User ldapUser = ldapClient.getUser(userDN);
        if (context.standardUserRole != null) {
            ldapUser.addRole(context.standardUserRole);
        }
        return ldapUser;
    }

    private User loadOrCreateDbUser(
                UserSynchronizationContext context,
                User ldapUser,
                Map<String, User> removeMap) {

        User dbUser = userDbService.loadByUserName(ldapUser.getUserName());
        if (dbUser == null) {
            logger.info("Discovered new user in LDAP: {}", ldapUser.getUserName());
            dbUser = doCreateUser(context, ldapUser);
        } else {
            removeMap.remove(dbUser.getId());
        }
        return dbUser;
    }

    /**
     * compute necessary changes in user roles and group
     * memberships; save them to DB and propagate to SNB
     */
    private void syncUserLdapChanges(
                UserSynchronizationContext context,
                String userDN,
                User ldapUser,
                User dbUser) {

        logger.trace("Checking LDAP user for changes: {}", dbUser.getUserName());
        ldapUser.setId(dbUser.getId());
        ldapUser.getRoles().addAll(dbUser.getRoles());
        ldapUser.getSystemGroups().addAll(dbUser.getSystemGroups());
        cleanUserLdapRoles(ldapUser);

        context.groupsToRemove = getUserLdapGroups(ldapUser);
        context.groupsToAdd = new HashSet<> ();

        syncUserLdapGroupsAndRoles(context, userDN, ldapUser);
        ldapUser.getSystemGroups()
                .removeAll(context.groupsToRemove);

        if (dbUser.isModified(CompareType.LDAP, ldapUser)) {
            logger.debug("Updating user from LDAP: {}", dbUser.getUserName());
            if ((context.groupsToAdd.size() > 0)
                     || (context.groupsToRemove.size() > 0)) {
                reportGroupChanges(context, ldapUser);
            }
            dbUser.applyChangesFromLdap(ldapUser);
            doUpdateUser(context, dbUser);
        }
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

    private void reportGroupChanges(UserSynchronizationContext context, User user) {
        StringBuilder sb = new StringBuilder(user.getUserName());
        sb.append(" - ");
        AtomicReference<String> sep = new AtomicReference<> ("JOIN: ");
        reportGroupList(context.groupsToAdd, sb, sep);
        if (context.groupsToAdd.size() > 0) {
            sb.append("; ");
        }
        sep.set("LEAVE: ");
        reportGroupList(context.groupsToRemove, sb, sep);
        context.report.addContent(AccessManager.SECTION_GROUP_INFO, sb.toString());
        context.reportRecords++;
    }

    private void reportGroupList(Set<Group> groups, StringBuilder sb, AtomicReference<String> separator) {
        for (Group group : groups) {
            sb.append(separator.getAndSet(", "));
            sb.append(group.getName());
        }
    }

    private void syncUserLdapGroupsAndRoles(
            UserSynchronizationContext context,
            String userDN,
            User ldapUser) {

        Set<String> memberships = ldapClient.getMemberships(userDN, true);
        for (String membershipDN : memberships) {
            syncUserLdapGroups(context, membershipDN, ldapUser);
            syncUserLdapRoles(context, membershipDN, ldapUser);
        }
    }

    private void syncUserLdapGroups(
            UserSynchronizationContext context,
            String membershipDN,
            User ldapUser) {

        Group dbGroup = context.groupsByDN.get(membershipDN);
        if (dbGroup != null) {
            context.groupsToRemove.remove(dbGroup);
            if (ldapUser.addSystemGroup(dbGroup)) {
                context.groupsToAdd.add(dbGroup);
            }
        }
    }

    private void syncUserLdapRoles(UserSynchronizationContext context, String membershipDN, User ldapUser) {
        Role dbRole = context.rolesByDN.get(membershipDN);
        if (dbRole != null) {
            ldapUser.addRole(dbRole);
        }
    }
}
