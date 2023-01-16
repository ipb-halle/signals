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
 * The UserManager can fetch users from Signals Notebook and 
 * store them in an SQL database. This class provides some 
 * convenience Methods for the AccessManager class.
 */

@Stateless
public class UserManager {

    @Inject
    private UserDbService dbService;

    @Inject
    private UserRestService restService;

    public User getDbUser(String id) {
        return dbService.loadById(id);
    }

    public User getSnbUser(String id) {
        return restService.doGetUser(id);
    }

    public User getUser(String id) {
        User u = dbService.loadById(id);
        if (u == null) {
            u = restService.doGetUser(id);
        }
        return u;
    }

    public List<User> getSnbUsers(String query, boolean enabled) {
        return restService.doGetUsers(query, enabled);
    }

    public void save(List<User> users) {
        for (User u : users) {
            dbService.save(u);
        }
    }

    public void save(User user) {
        dbService.save(user);
    }
}
