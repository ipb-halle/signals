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

import java.util.HashSet;
import java.util.Set;

/**
 * Mock LDAP client (currently without any function!)
 */
public class MockLdapClient implements LdapClient {

    public Set<String> filterDNs(Set<String> distinguishedNames, FilterType type) {
        return new HashSet<String> ();
    }

    public Group getGroup(String groupDN) {
        return null;
    }

    public Set<String> getMembers(String groupDN) {
        return new HashSet<String> ();
    }

    public Set<String> getMemberships(String userDN) {
        return new HashSet<String> ();
    }

    public UserEntity getUserEntity(String userDN) {
        return null;
    }

    public Set<String> getUsers(String filter) {
        return new HashSet<String> ();
    }
}
