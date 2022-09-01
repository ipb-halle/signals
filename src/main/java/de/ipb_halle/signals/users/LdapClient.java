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

import java.util.List;
import javax.ejb.Local;

/** 
 * Ldap client reader for Signals tool 
 */
@Local
public interface LdapClient {

    /**
     * @param groupDN a distinguished group name
     * @return a corresponding Group object
     */
    public Group getGroup(String groupDN);


    /**
     * @param groupDN a distinguished group name
     * @return the list of users, who are members of that group, including nested memberships
     */
    public List<String> getMembers(String groupDN);

    /**
     * @param userDN a distinguished user name
     * @return a list of (nested) group memberships for the given user
     */
    public List<String> getMemberships(String userDN);

    /**
     * @param userDN a distinguished user name
     * @return a corresponding User object
     */
    public User getUser(String userDN);

    /**
     * @param baseDN the base DN for searching users
     * @return a list of distinguished user names
     */
    public List<String> getUsers(String baseDN);

}
