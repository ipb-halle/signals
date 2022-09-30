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

import de.ipb_halle.signals.rest.RestHelper;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * SNB user 
 */

@Entity
@Table(name="users")
public class UserEntity implements User {

    public final static String ATTR_ALIAS = "alias";
    public final static String ATTR_COUNTRY = "country";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_EMAIL = "email";
    public final static String ATTR_ENABLED = "isEnabled";
    public final static String ATTR_FIRST_NAME = "firstName";
    public final static String ATTR_LAST_LOGIN = "lastLoginAt";
    public final static String ATTR_LAST_NAME = "lastName";
    public final static String ATTR_ORGANIZATION = "organization";
    public final static String ATTR_RELATIONSHIP_ROLES = "relationships.roles.data";
    public final static String ATTR_ROLES = "roles";
    public final static String ATTR_SYSTEM_GROUPS = "systemGroups";
    public final static String ATTR_USER_ID = "userId";
    public final static String ATTR_USER_NAME = "userName";
    
    
    @Id
    private Integer id;

    @Column
    private String alias;

    @Column
    private String country;

    @Column(name="created_at")
    private Date createdAt;

    @Column
    private String email;

    @Column(name="is_enabled")
    private Boolean enabled;

    @Column(name="first_name")
    private String firstName;

    @Column
    private boolean immutable;

    @Column(name="last_login_at")
    private Date lastLoginAt;

    @Column(name="last_name")
    private String lastName;

    @Column
    private String organization;

    @Column(name="user_name")
    private String userName;

    @Column(name="json_string")
    private String jsonString;

    private transient Set<IRole> roles;
    private transient Set<IGroup> systemGroups;

    /**
     * default constructor
     */
    public UserEntity() {
        createdAt = new Date(0);
        lastLoginAt = new Date(0);
        roles = new HashSet<> ();
        systemGroups = new HashSet<> ();
    }

    public void addRole(IRole role) {
        roles.add(role);
    }

    public void addSystemGroup(IGroup group) {
        systemGroups.add(group);
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("User(%d): %s, %s\n", id, lastName, firstName));
        sb.append(String.format("  Alias: %s    User name: %s  %s\n", alias, userName, immutable ? "immutable" : "managed"));
        sb.append(String.format("  Email: %s    Country: %s\n", email, country));
        sb.append(String.format("  Organization: %s   Enabled: %s\n", organization, enabled ? "True" : "False"));
        sb.append(String.format("  Created at: %s\n", RestHelper.formatDate(createdAt)));
        sb.append(String.format("  Last login: %s\n", RestHelper.formatDate(lastLoginAt)));
        sb.append((jsonString != null) ? jsonString : "");
         return sb.toString();
    }

    public Integer getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public String getCountry() {
        return country;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getJsonString() {
        return jsonString;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOrganization() {
        return organization;
    }

    public Set<IRole> getRoles() {
        return roles;
    }

    public Set<IGroup> getSystemGroups() {
        return systemGroups;
    }

    public String getUserName() {
        return userName;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public User setId(Integer i) {
        id = i;
        return this;
    }

    public void setAlias(String a) { 
        alias = a;
    }

    public void setCountry(String c) {
        country = c;
    }

    public void setCreatedAt(Date d) {
        createdAt = d;
    }

    public void setEmail(String e) {
        email = e;
    }

    public void setEnabled(Boolean e) {
        enabled = e;
    }

    public void setFirstName(String f) {
        firstName = f;
    }

    public void setImmutable(boolean i) {
        immutable = i;
    }

    public void setJsonString(String j) {
        jsonString = j;
    }

    public void setLastLoginAt(Date d) {
        lastLoginAt = d;
    }

    public void setLastName(String l) {
        lastName = l;
    }

    public void setOrganization(String o) {
        organization = o;
    }

    public void setRoles(Set<IRole> r) {
        roles = r;
    }

    public void setSystemGroups(Set<IGroup> g) {
        systemGroups = g;
    }

    public void setUserName(String u) {
        userName = u;
    }
}
