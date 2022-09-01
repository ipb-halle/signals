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
package de.ipb_halle.signals;


/** 
 * Configuration reader for Signals tool.
 * Configuration values to be provided by the openejb.conf file.
 */

public class SignalsConfig {

    private String apiKey;
    private String baseUrl;

    private String ldapAttrAlias;                       // ...
    private String ldapAttrGroupName;                   // cn
    private String ldapAttrEmail;                       // mail
    private String ldapAttrFirstName;                   // givenName
    private String ldapAttrLastName;                    // sn
    private String ldapAttrMemberOf;                    // memberOf
    private String ldapBaseDNs;                         // dc=<domain>,dc=<country>;...
    private String ldapContextProviderURL;              // ldap://<server>:<port>
    private String ldapContextReferral;                 // follow
    private String ldapFilterGroupDN;                   // ou=<SNB group OU>,...,dc=<domain>,dc=<country>
    private String ldapFilterRoleDN;                    // ou=<SNB role OU>,...,dc=<domain>,dc=<country>
    private String ldapFilterUser;                      // (&(objectClass=person)(mail=@))
    private String ldapFilterUsers;                     // (objectClass=person)
    private String ldapSecurityPrincipal;               //
    private String ldapSecurityCredentials;             //
    private String ldapSecurityAuthentication;          // simple


    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }

    public String getLdapAttrAlias() { return ldapAttrAlias; }
    public String getLdapAttrGroupName() { return ldapAttrGroupName; }
    public String getLdapAttrEmail() { return ldapAttrEmail; }
    public String getLdapAttrFirstName() { return ldapAttrFirstName; }
    public String getLdapAttrLastName() { return ldapAttrLastName; }
    public String getLdapAttrMemberOf() { return ldapAttrMemberOf; }
    public String getLdapBaseDNs() { return ldapBaseDNs; }
    public String getLdapContextProviderURL() { return ldapContextProviderURL; }
    public String getLdapContextReferral() { return ldapContextReferral; }
    public String getLdapFilterGroupDN() { return ldapFilterGroupDN; }
    public String getLdapFilterRoleDN() { return ldapFilterRoleDN; }
    public String getLdapFilterUser() { return ldapFilterUser; }
    public String getLdapFilterUsers() { return ldapFilterUsers; }
    public String getLdapSecurityPrincipal() { return ldapSecurityPrincipal; }
    public String getLdapSecurityCredentials() { return ldapSecurityCredentials; }
    public String getLdapSecurityAuthentication() { return ldapSecurityAuthentication; }

}


