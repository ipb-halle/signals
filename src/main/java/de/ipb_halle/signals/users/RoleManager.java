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
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Manager for signals roles
 */

@Stateless
public class RoleManager {

    @Resource
    private SignalsConfig config;

    @Inject
    private RoleDbService roleDbService;

    @Inject
    private RoleRestService roleRestService;

    @Inject
    private LdapClient ldapClient;

    private Logger logger = LoggerFactory.getLogger(RoleManager.class);

    public Role save(UpdateConfig updateConfig, Role role) {
        if (updateConfig.updateDb) {
            return roleDbService.save(role);
        } else {
            this.logger.trace("DRY RUN: skipped DB UPDATE for role: {} ", role.getName());
        }
        return role;
    }

    public void resolveRoleReferences(User snbUser) {
        Set<IRole> newRoles = new HashSet<> ();
        for (IRole iRole : snbUser.getRoles()) {
            newRoles.add(roleDbService.loadById(iRole.getId()));
        }
        snbUser.setRoles(newRoles);
    }

    /**
     * NOTE: one cannot create or modify roles with this tool. Only
     * assigning roles to users (or removing from) is supported.
     *
     * NOTE: it is currently not possible to remove the ldapFlag from
     * a role once it is no longer managed by LDAP
     *
     * @param context the UserSynchronizationContext for this operation
     * @throws de.ipb_halle.signals.users.LdapConnectionErrorException
     */
    public void obtainLdapRoles(UserSynchronizationContext context) throws LdapConnectionErrorException {
        Map<String, Role> rolesByDN = new HashMap<> ();
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
                    if (! dbRole.isLdapRole()) {
                        logger.info("Making role {} LDAP managed", ldapRole.getName());
                        dbRole.setLdapRole(true);
                        dbRole = save(context.updateConfig, dbRole);
                    }
                    rolesByDN.put(dn, dbRole);
                }
            }
        }
        context.rolesByDN = rolesByDN;
    }

    public void obtainStandardUserRole(UserSynchronizationContext context) {
        context.standardUserRole = roleDbService.loadByName(config.getStandardUserRoleName());
    }

    /**
     * Obtain roles configuration from SNB and store them in the DB.
     * @param context the UserSynchronizationContext for this operation
     */
    public void syncDbRolesFromSnb(UserSynchronizationContext context) {
        Map<String, Role> rolesFromDb = roleDbService.loadMappedById(new HashMap<> ());

        for(Role snbRole : roleRestService.doGetRoles()) {
            logger.trace("Processing SNB role: {}", snbRole.getName());
            Role dbRole = rolesFromDb.remove(snbRole.getId());
            if (dbRole == null) {
                logger.info("SNB role is NEW: {}", snbRole.getName());
                save(context.updateConfig, snbRole);
            } else {
                if (snbRole.isModified(dbRole)) {
                    logger.debug("SNB role is modified: {}", snbRole.getName());
                    dbRole.applyChangesFromSnb(snbRole);
                    dbRole.setDeleted(false);
                    save(context.updateConfig, dbRole);
                }
            }
        }
        deleteMissingRoles(context.updateConfig, rolesFromDb.values());
    }

    public void deleteMissingRoles(UpdateConfig updateConfig, Collection<Role> missingRoles) {
        for (Role dbRole : missingRoles) {
            logger.debug("Role {} not found in SNB - marking as deleted", dbRole.getName());
            dbRole.setDeleted(true);
            save(updateConfig, dbRole);
        }
    }
}
