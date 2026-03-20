# Helper Tool for Signals Notebook

This project bundles code for migration of IPB data sources, for backup, maintenance and external services for the Revvity Signals Notebook. The command line tool relies on a single configuration file <code>openejb.xml</code>, which is passed via command line. The configuration file contains the <code>DataSource</code> definition(s), the base url of the Signals Notebook instance and the API key. Example content is as follows (please adjust for your instance):

    <?xml version="1.0"?>
    <openejb>
        <Resource id="signalsDS" type="javax.sql.DataSource">
            accessToUnderlyingConnectionAllowed = false
            alternateUsernameAllowed = false
            connectionProperties =
            defaultAutoCommit = true
            defaultReadOnly =
            definition =
            ignoreDefaultValues = false
            initialSize = 0
            jdbcDriver = org.postgresql.Driver
            jdbcUrl = jdbc:postgresql://localhost:5432/myDATABASE?charSet=UTF-8
            jtaManaged = true
            maxActive = 20
            maxIdle = 20
            maxOpenPreparedStatements = 0
            maxWaitTime = -1 millisecond
            minEvictableIdleTime = 30 minutes
            minIdle = 0
            numTestsPerEvictionRun = 3
            password = myPASSWORD
            passwordCipher = PlainText
            poolPreparedStatements = false
            serviceId =
            testOnBorrow = true
            testOnReturn = false
            testWhileIdle = false
            timeBetweenEvictionRuns = -1 millisecond
            userName = myUSER
            validationQuery = SELECT 1 AS Validation;
        </Resource>
        <Resource id="signalsConfig" class-name="de.ipb_halle.signals.SignalsConfig">
          apiKey = THIS+IS+THE+SUPER+SECRET+API+TOKEN+WHICH+NEVER+SHOULD+APPEAR+ON+GITHUB==
          baseUrl = https://YOUR-ORG-TRIAL-INSTANCE.signalsnotebook.SOMECLOUD.INVALID/api/rest/v1.0
          groupAttrDescription = LDAP managed group
          ldapAttrAccountExpirationDate = accountExpires
          ldapAttrAlias = initials
          ldapAttrCreatedAt = whenCreated
          ldapAttrGroupName = cn
          ldapAttrEmail = mail
          ldapAttrFirstName = givenName
          ldapAttrLastName = sn
          ldapAttrMemberOf = memberOf
          ldapAttrMembers = member
          ldapAttrObjectClass = objectClass
          ldapAttrObjectClassGroup = group
          ldapAttrObjectClassUser = person
          ldapAttrUserName = mail
          ldapContextProviderURL = ldap://ldap.somewhere.invalid:PORT
          ldapContextReferral = follow
          ldapDateFormatString = yyyyMMddHHmmss.S'Z'
          ldapDeniedUsers = cn=SnbDeniedUsers,ou=...,dc=DOMAIN,dc=COUNTRY
          ldapManagedGroups = cn=SnbManagedGroups,ou=...,dc=DOMAIN,dc=COUNTRY
          ldapManagedRoles = cn=SnbManagedRoles,ou=...,dc=DOMAIN,dc=COUNTRY
          ldapManagedUsers = cn=SnbManagedUsers,ou=...,dc=DOMAIN,dc=COUNTRY
          ldapSecurityPrincipal = test
          ldapSecurityCredentials = 0000
          ldapSecurityAuthentication = simple
          mailFrom = someone@somewhere.invalid
          mailTo = someone@somewhere.invalid
          snbInstanceName = TestOrg Trial
          standardUserRoleName = Standard User
          storagePath = /path/to/attachment/storage
          userAttrCountry = Germany
          userAttrOrganization = testOrg
        </Resource>
    </openejb>


To run the code, build the project

    mvn package

adjust the config files and start the tool with the following command line:

    java -jar target/signals-1.0.jar --config PATH/TO/openejb.xml --help

or e.g.

    java -jar target/signals-1.0.jar --config PATH/TO/openejb.xml \
        --trustStore PATH/TO/truststore.jks --manage-users


## Trademark Notice
Signals Notebook is a product of Revvity Signals Software Inc., Waltham, MA, USA.
The mention or use of brand names does not imply that IPB has any rights to these
products or names.
