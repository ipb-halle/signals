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
 * Manager for signals groups
 */

@Stateless
public class GroupManager {

    @Resource
    private SignalsConfig config;

    @Inject
    private LdapClient ldapClient;

    @Inject
    private GroupDbService groupDbService;

    @Inject
    private GroupRestService groupRestService;

    private Logger logger = LoggerFactory.getLogger(GroupManager.class);


    public void save(UpdateConfig updateConfig, Group group) {
        if (updateConfig.updateDb) {
            groupDbService.save(group);
        } else {
            this.logger.trace("DRY RUN: skipped DB UPDATE for group: {}", group.getName());
        }
    }

    public void resolveGroupReferences(User snbUser) {
        Set<IGroup> newGroups = new HashSet<> ();
        for (IGroup iGroup : snbUser.getSystemGroups()) {
            newGroups.add(groupDbService.loadById(iGroup.getId()));
        }
        snbUser.setSystemGroups(newGroups);
    }

    /**
     * NOTE: currently we CANNOT manage group shares or group
     * associations. We therefore refrain from creating or updating
     * groups via LDAP as manual intervention would be required
     * anyway.
     * 
     * NOTE: currently, the flag ldapGroup cannot be cleared
     */
    public void obtainLdapGroups(UserSynchronizationContext context) {
        Map<String, Group> groupsByDN = new HashMap<> ();
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
                    if (! dbGroup.isLdapGroup()) {
                        logger.info("Making group {} LDAP managed", ldapGroup.getName());
                        dbGroup.setLdapGroup(true);
                        save(context.updateConfig, dbGroup);
                    }
                    groupsByDN.put(dn, dbGroup);
                }
            }
        }
        context.groupsByDN = groupsByDN;
    }

    public void syncDbGroupsFromSnb(UpdateConfig updateConfig) {
        Map<String, Group> groupsFromDb = groupDbService.loadMappedById(new HashMap<> ());

        for(Group snbGroup : groupRestService.doGetGroups()) {
            logger.trace("Processing SNB group: {}", snbGroup.getName());
            Group dbGroup = groupsFromDb.remove(snbGroup.getId());
            if (dbGroup == null) {
                logger.info("SNB group is NEW: {}", snbGroup.getName());
                save(updateConfig, snbGroup);
            } else {
                if (snbGroup.isModified(CompareType.SNB, dbGroup)) {
                    logger.debug("SNB group is modified: {}", snbGroup.getName());
                    dbGroup.applyChangesFromSnb(snbGroup);
                    dbGroup.setDeleted(false);
                    save(updateConfig, dbGroup);
                }
            }
        }
        deleteMissingGroups(updateConfig, groupsFromDb.values());
    }

    public void deleteMissingGroups(UpdateConfig updateConfig, Collection<Group> missingGroups) {
        for (Group group : missingGroups) {
            logger.debug("Group {} not found in SNB - marking as deleted", group.getName());
            group.setDeleted(true);
            save(updateConfig, group);
        }
    }
}
