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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


/** 
 * SNB user entity
 */
@Entity
@Table(name="users")
public class UserEntity {

    @Id
    private String id;

    @Column
    private String alias;

    @Column
    private String country;

    @Column(name="created_at")
    private Date createdAt;

    @Column
    private String email;

    @Column(name="is_enabled")
    @NotNull
    private Boolean enabled;

    @Column(name="first_name")
    private String firstName;

    @Column
    private boolean mutable;

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

    /**
     * default constructor
     */
    public UserEntity() {
        createdAt = new Date(0);
        lastLoginAt = new Date(0);
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

    public String getUserName() {
        return userName;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public boolean isMutable() {
        return mutable;
    }

    public UserEntity setId(String i) {
        id = i;
        return this;
    }

    public UserEntity setAlias(String a) { 
        alias = a;
        return this;
    }

    public UserEntity setCountry(String c) {
        country = c;
        return this;
    }

    public UserEntity setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public UserEntity setEmail(String e) {
        email = e;
        return this;
    }

    public UserEntity setEnabled(Boolean e) {
        enabled = e;
        return this;
    }

    public UserEntity setFirstName(String f) {
        firstName = f;
        return this;
    }

    public UserEntity setMutable(boolean i) {
        mutable = i;
        return this;
    }

    public UserEntity setJsonString(String j) {
        jsonString = j;
        return this;
    }

    public UserEntity setLastLoginAt(Date d) {
        lastLoginAt = d;
        return this;
    }

    public UserEntity setLastName(String l) {
        lastName = l;
        return this;
    }

    public UserEntity setOrganization(String o) {
        organization = o;
        return this;
    }

    public UserEntity setUserName(String u) {
        userName = u;
        return this;
    }
}
