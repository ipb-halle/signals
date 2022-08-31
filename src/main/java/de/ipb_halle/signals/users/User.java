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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * SNB user 
 */

@Entity
@Table(name="users")
public class User {

    private final static String ATTR_ID = "userId";
    private final static String ATTR_ALIAS = "alias";
    private final static String ATTR_COUNTRY = "country";
    private final static String ATTR_CREATED_AT = "createdAt";
    private final static String ATTR_EMAIL = "email";
    private final static String ATTR_ENABLED = "isEnabled";
    private final static String ATTR_FIRST_NAME = "firstName";
    private final static String ATTR_LAST_LOGIN = "lastLoginAt";
    private final static String ATTR_LAST_NAME = "lastName";
    private final static String ATTR_ORGANIZATION = "organization";
    private final static String ATTR_USER_NAME = "userName";
    
    
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

    @Column(name="last_login_at")
    private Date lastLoginAt;

    @Column(name="last_name")
    private String lastName;

    @Column
    private String organization;

    @Column(name="user_name")
    private String userName;

    @Column
    private String json_string;

    private transient JsonElement json;
    private transient SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static User createUser(JsonElement j) {
        User user = new User();
        JsonObject attributes = j.getAsJsonObject().getAsJsonObject("attributes");

        user.id = j.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
        user.json = j;
        user.json_string = j.toString();

        user.alias = attributes.getAsJsonPrimitive(ATTR_ALIAS).getAsString();
        user.country = attributes.getAsJsonPrimitive(ATTR_COUNTRY).getAsString();
        user.createdAt = user.parseDate(attributes.getAsJsonPrimitive(ATTR_CREATED_AT).getAsString());
        user.email = attributes.getAsJsonPrimitive(ATTR_EMAIL).getAsString();
        user.enabled = attributes.getAsJsonPrimitive(ATTR_EMAIL).getAsBoolean();
        user.firstName = attributes.getAsJsonPrimitive(ATTR_FIRST_NAME).getAsString();
        user.lastLoginAt = user.parseDate(attributes.getAsJsonPrimitive(ATTR_LAST_LOGIN).getAsString());
        user.lastName = attributes.getAsJsonPrimitive(ATTR_LAST_NAME).getAsString();
        user.organization = attributes.getAsJsonPrimitive(ATTR_ORGANIZATION).getAsString();
        user.userName = attributes.getAsJsonPrimitive(ATTR_USER_NAME).getAsString();

        return user;
    }

    public void dump() {
        System.out.println("User" + id);
        System.out.println(json.toString());
        System.out.println("==============================================================");
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

    public DateFormat getDateFormat() {
        return dateFormat;
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

    public String getUserName() {
        return userName;
    }

    public String getJsonString() {
        return json_string;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Date parseDate(String ds) {
        try {
            return dateFormat.parse(ds);
        } catch(Exception e) {
        }
        return new Date();
    }

    public void setId(Integer id) {
        id = id;
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

    public void setLastLoginAt(Date d) {
        lastLoginAt = d;
    }

    public void setLastName(String l) {
        lastName = l;
    }

    public void setOrganization(String o) {
        organization = o;
    }

    public void setUserName(String u) {
        userName = u;
    }
}
