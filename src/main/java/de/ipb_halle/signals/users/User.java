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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SNB user DTO
 */
public class User implements IUser {

    // UserEntity field names (NOT column names from SQL table)
    public final static String USER_USERNAME = "userName";
    public final static String USER_MUTABLE = "mutable";
    public final static String USER_ENABLED = "enabled";

    // JSON attributes
    public final static String ATTR_ALIAS = "alias";
    public final static String ATTR_COUNTRY = "country";
    public final static String ATTR_CREATED_AT = "createdAt";
    public final static String ATTR_EMAIL = "email";
    public final static String ATTR_EMAIL_ADDRESS = "emailAddress";  // used during creation of user
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

    private String id;

    private String alias;

    private String country;

    private Date createdAt;

    private String email;

    private Boolean enabled;

    private String firstName;

    private boolean mutable;

    private Date lastLoginAt;

    private String lastName;

    private String organization;

    private String userName;

    private Set<IRole> roles;

    private Set<IGroup> systemGroups;

    private Logger logger;

    /**
     * default constructor
     */
    public User() {
        createdAt = new Date(0);
        lastLoginAt = new Date(0);
        roles = new HashSet<> ();
        systemGroups = new HashSet<> ();
        logger = LoggerFactory.getLogger(User.class);
    }

    public User(UserEntity entity) {
        roles = new HashSet<> ();
        systemGroups = new HashSet<> ();
        id = entity.getId();
        alias = entity.getAlias();
        country = entity.getCountry();
        createdAt = entity.getCreatedAt();
        email = entity.getEmail();
        enabled = entity.isEnabled();
        firstName = entity.getFirstName();
        mutable = entity.isMutable();
        lastLoginAt = entity.getLastLoginAt();
        lastName = entity.getLastName();
        organization = entity.getOrganization();
        userName = entity.getUserName();
        logger = LoggerFactory.getLogger(User.class);
    }

    public boolean addRole(IRole role) {
        return roles.add(role);
    }

    public boolean addSystemGroup(IGroup group) {
        return systemGroups.add(group);
    }

    /**
     * Apply changes from a reference user. Does NOT overwrite the
     * createdAt, lastLoginAt and mutable attributes.
     * Expects roles and system groups to be database entities and
     * not just role / group references.
     */
    public void applyChanges(User user) {
        alias = user.getAlias();
        country = user.getCountry();
        email = user.getEmail();
        enabled = user.isEnabled();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        organization = user.getOrganization();
        userName = user.getUserName();
        setRoles(user.getRoles());
        setSystemGroups(user.getSystemGroups());
    }

    /**
     * specifically apply changes from LDAP
     */
    public void applyChangesFromLdap(User ldapUser) {
        applyChanges(ldapUser);
    }

    /**
     * specifically apply changes from SNB (include
     * also createdAt and lastLoginAt attributes).
     */
    public void applyChangesFromSnb(User snbUser) {
        createdAt = snbUser.getCreatedAt();
        lastLoginAt = snbUser.getLastLoginAt();
        applyChanges(snbUser);
    }

    public void clearRoles() {
        this.roles.clear();
    }

    public UserEntity createEntity() {
        return new UserEntity()
            .setId(id)
            .setAlias(alias)
            .setCountry(country)
            .setCreatedAt(createdAt)
            .setEmail(email)
            .setEnabled(enabled)
            .setFirstName(firstName)
            .setMutable(mutable)
            .setLastLoginAt(lastLoginAt)
            .setLastName(lastName)
            .setOrganization(organization)
            .setUserName(userName);
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        AtomicReference<String> sep = new AtomicReference<> ("");
        sb.append(String.format("User(%s): %s, %s\n", id, lastName, firstName));
        sb.append(String.format("  Alias: %s    User name: %s  %s\n", alias, userName, mutable ? "managed": "immutable"));
        sb.append(String.format("  Email: %s    Country: %s\n", email, country));
        sb.append(String.format("  Organization: %s   Enabled: %s\n", organization, enabled ? "True" : "False"));
        sb.append(String.format("  Created at: %s\n", RestHelper.formatDate(createdAt)));
        sb.append(String.format("  Last login: %s\n", RestHelper.formatDate(lastLoginAt)));
        sb.append("  Roles: ");
        for (IRole iRole : roles) {
            sb.append(sep.getAndSet(", "));
            sb.append(iRole.getId());
        }
        sep.getAndSet("");
        sb.append("\n  Groups: ");
        for (IGroup iGroup : systemGroups) {
            sb.append(sep.getAndSet(", "));
            sb.append(iGroup.getId());
        }
        sb.append("\n");
         return sb.toString();
    }

    public String getId() {
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

    /**
     * Compare this user to a reference user.
     *
     * NOTE: It is not possible to update email or userName via SNB REST API
     * NOTE: times must be rounded to full seconds
     *
     * @param context the compare context: either SNB or LDAP. Creation and last login
     * timestamps are ignored in LDAP compare type mode.
     * @param user the reference user
     * @return true if current user is modified
     */
    public boolean isModified(CompareType context, User user) {
        return !( loggingEquals(alias, user.getAlias(), "alias")
            && loggingEquals(country, user.getCountry(), "country")
            && ((context == CompareType.SNB)
                ? loggingEquals(createdAt.getTime() / 1000L, user.getCreatedAt().getTime() / 1000L, "createdAt")
                : true)
            && loggingEquals(email, user.getEmail(), "email")
            && loggingEquals(enabled, user.isEnabled(), "enabled")
            && loggingEquals(firstName, user.getFirstName(), "firstName")
            && ((context == CompareType.SNB)
                ? loggingEquals(lastLoginAt.getTime() / 1000L, user.getLastLoginAt().getTime() / 1000L, "lastLoginAt")
                : true)
            && loggingEquals(lastName, user.getLastName(), "lastName")
            && loggingEquals(organization, user.getOrganization(),  "organization")
            && loggingEquals(userName, user.getUserName(), "userName")
            && loggingEquals(roles, user.getRoles(), "roles")
            && loggingEquals(systemGroups, user.getSystemGroups(), "systemGroups"));
    }

    public boolean isMutable() {
        return mutable;
    }

    private boolean loggingEquals(Object a, Object b, String message) {
        if (Objects.equals(a, b)) {
            return true;
        }
        logger.trace("{} NOT equal", message);
        return false;
    }

    public IUser setId(String i) {
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

    public void setMutable(boolean i) {
        mutable = i;
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
