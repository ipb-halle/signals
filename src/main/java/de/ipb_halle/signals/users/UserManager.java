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
import javax.ejb.Stateless;
import javax.inject.Inject;


/** 
 * The UserManager can fetch users from Signals Notebook and from LDAP,
 * store them in an SQL databas and synchronize among the various sources.
 */

@Stateless
public class UserManager {

    @Inject
    private UserDbService dbService;

    @Inject
    private UserRestService restService;

    @Inject
    private LdapClient ldapClient;

    public UserEntity getDbUser(int id) {
        return dbService.loadById(id);
    }

    public UserEntity getSnbUser(int id) {
        return restService.doGetUser(id);
    }

    public List<UserEntity> getSnbUsers(String query, boolean enabled) {
        return restService.doGetUsers(query, enabled);
    }

    public void save(List<UserEntity> users) {
        for (UserEntity u : users) {
            dbService.save(u);
        }
    }
}
