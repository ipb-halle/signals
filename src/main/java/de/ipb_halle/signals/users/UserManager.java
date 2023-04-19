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
import de.ipb_halle.signals.UpdateConfig;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

/*
    public User getDbUser(String id) {
        return userDbService.loadById(id);
    }

    public User getSnbUser(String id) {
        return userRestService.doGetUser(id);
    }
*/

    public User getUser(String id) {
        return userDbService.loadById(id);
    }

/*
    public List<User> getSnbUsers(String query, boolean enabled) {
        return userRestService.doGetUsers(query, enabled);
    }

    public void save(List<User> users) {
        for (User u : users) {
            userDbService.save(u);
        }
    }
*/

    public User doCreateUser(UserSynchronizationContext context, User user) {
        if (context.updateConfig.updateSNB) {
            logger.info("Creating new SNB user: {}", user.getUserName());
            User snbUser = userRestService.doCreateUser(user);
            snbUser.setMutable(true);
            save(context.updateConfig, snbUser);
            return userDbService.loadById(snbUser.getId());
        } else {
            logger.trace("DRY RUN: skipped SNB CREATE for user: {}", user.getUserName());
        }
        context.report.addContent(AccessManager.SECTION_NEW_USERS, user.getUserName());
        context.reportRecords++;
        return user;
    }

    public void doDisableUser(UserSynchronizationContext context, User user) {
        if (context.updateConfig.updateSNB) {
            userRestService.doDisableUser(user);
            user.setEnabled(false);
            user.setRoles(new HashSet<> ());
            user.setSystemGroups(new HashSet<> ());
            save(context.updateConfig, user);
        } else {
            logger.trace("DRY RUN: skipped SNB DISABLE for user: {}", user.getUserName());
        }
        context.report.addContent(AccessManager.SECTION_DISABLED_USERS, user.getUserName());
        context.reportRecords++;
    }

    public void doUpdateUser(UserSynchronizationContext context, User user) {

        if (context.updateConfig.updateSNB) {
            save(context.updateConfig, user);
            userRestService.doUpdateUser(user, context.groupsToAdd, context.groupsToRemove);
        } else {
            logger.trace("DRY RUN: skipped SNB UPDATE for user: {}", user.getUserName());
        }
    }

    public void save(UpdateConfig updateConfig, User user) {
        if (updateConfig.updateDb) {
            userDbService.save(user);
        } else {
            logger.trace("DRY RUN: skipped DB update for user: {}", user.getUserName());
        }
    }


    /**
     * synchronize Db from SNB
     */
    public void syncDbUsersFromSnb(UpdateConfig updateConfig) {
        // enabled users 
        for( User snbUser : userRestService.doGetUsers(null, true)) {
            syncDbUserFromSnb(updateConfig, snbUser);
        }

        // disabled users
        for (User snbUser : userRestService.doGetUsers(null, false)) {
            syncDbUserFromSnb(updateConfig, snbUser);
        }
    }

    private void syncDbUserFromSnb(UpdateConfig updateConfig, User snbUser) {
        logger.trace("Processing SNB user: {}", snbUser.getUserName());
        User dbUser = userDbService.loadById(snbUser.getId());
        if (dbUser == null) {
            logger.info("SNB user is NEW: {}", snbUser.getUserName());
            snbUser.setMutable(false);
            save(updateConfig, snbUser);
        } else {
            roleManager.resolveRoleReferences(snbUser);
            groupManager.resolveGroupReferences(snbUser);
            if (snbUser.isModified(CompareType.SNB, dbUser)) {
                logger.debug("SNB user is modified: {}", snbUser.getUserName());
                dbUser.applyChangesFromSnb(snbUser);
                save(updateConfig, dbUser);
            }
        }
    }

    /**
     * syncronize multiple users from LDAP
     */
    public void syncUsersFromLdap(UserSynchronizationContext context) {
        Set<String> userDNs = new HashSet<> ();
        Set<String> deniedUsers = new HashSet<> ();
        ldapClient.getMembers(deniedUsers, new HashSet<> (), config.getLdapDeniedUsers(), true);

        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(User.USER_MUTABLE, Boolean.TRUE);
        cmap.put(User.USER_ENABLED, Boolean.TRUE);
        Map<String, User> enabledMutableDbUsersById = userDbService.loadMappedById(cmap);

        // nesting allowed here, i.e. we can assign entire groups to SNB
        ldapClient.getMembers(userDNs, new HashSet<> (), config.getLdapManagedUsers(), true);
        for (String dn : userDNs) {
            if (! deniedUsers.contains(dn)) {
                logger.trace("Processing LDAP user DN: {}", dn);
                User dbUser = syncUserFromLdap(context, dn);
                enabledMutableDbUsersById.remove(dbUser.getId());
            } else {
                logger.trace("Skipping denied user: {}", dn);
            }
        }
        // disable remaining mutable users
        disableMutableUsers(context, enabledMutableDbUsersById.values());
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
    private User syncUserFromLdap(UserSynchronizationContext context, String userDN) {
        User ldapUser = ldapClient.getUser(userDN);
        if (context.standardUserRole != null) {
            ldapUser.addRole(context.standardUserRole);
        }
        User dbUser = userDbService.loadByUserName(ldapUser.getUserName());
        if (dbUser == null) {
            logger.info("Discovered new user in LDAP: {}", ldapUser.getUserName());
            dbUser = doCreateUser(context, ldapUser);
        }

        if (dbUser.isMutable() && dbUser.isEnabled()) {
            // check for changes and apply if necessary
            syncUserLdapChanges(context, userDN, ldapUser, dbUser);
        } else {
            logger.debug("Cannot update immutable or disabled user: {}", dbUser.getUserName());
        }
        return dbUser;
    }

    /**
     * compute necessary changes in user roles and group
     * memberships; save them to DB and SNB
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
