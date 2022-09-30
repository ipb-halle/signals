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

import java.util.Set;
import javax.ejb.Local;

/** 
 * Ldap client reader for Signals tool 
 */
@Local
public interface LdapClient {

    public enum FilterType {
        GROUP,
        ROLE,
        USER
    };

    /*
     * Filter a set of distinguished names. Only DNs matching the filter pattern
     * will be kept. This prevents inflation of groups in the dependend system 
     * (SNB) and to leaking of internal information.
     *
     * @param distinguishedNames a set of distinguished names
     * @param filterType 
     * @return a filtered set of distinguished names according to the ldapFilterGroupDN setting
     */
    public Set<String> filterDNs(Set<String> distinguishedNames, FilterType type);

    /**
     * @param groupDN a distinguished group name
     * @return a corresponding Group object
     */
    public Group getGroup(String groupDN);

    /**
     * @param groupDN a distinguished group name
     * @param nesting indicate whether nested memberships should be resolved
     * @return the list of users, who are members of that group, including nested memberships
     */
    public Set<String> getMembers(String groupDN, boolean nesting);

    /**
     * @param users a Set to collect DNs of discovered member users
     * @param groups a Set to collect DNs of discovered member groups
     * @param groupDN a distinguished group name
     * @param nesting indicate whether nested memberships should be resolved
     */
    public void getMembers(Set<String> users, Set<String> groups, String groupDN, boolean nesting);


    /**
     * @param userDN a distinguished user name
     * @param nesting indicate whether nested memberships should be resolved
     * @return a list of (nested) group memberships for the given user
     */
    public Set<String> getMemberships(String userDN, boolean nesting);


    /**
     * @param roleDN a distinguished LDAP group name for that role
     * @return a corresponding Role object
     */
    public Role getRole(String roleDN);

    /**
     * @param userDN a distinguished user name
     * @return a corresponding UserEntity object
     */
    public UserEntity getUserEntity(String userDN);

    /**
     * @param filter the filter for searching users or null to 
     * obtain all users. The value is usually an email address.
     * @return a set of (matching) distinguished user names
     */
    public Set<String> getUsers(String filter);

}
