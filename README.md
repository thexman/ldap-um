Info
=================================================================================

This is minimalistic web application for managing users in LDAP server.
It consist of three modules:
1. User management (accessed only by the Administrators)
2. Groups management (accessed only by the Administrators)
3. Current user profile. (accessd by Administrators, Users)



Creating eclipse project
=================================================================================

Create eclipse project
----------------------------
`mvn -DdownloadSources=true eclipse:eclipse`


Build eclipse project
----------------------------
`mvn clean install`

Configuring tomcat standalone server
=================================================================================

Application realm
----------------------------
Configure users access to the application by adding a new realm to tomcat **"server.xml"** file. 
 

`<Realm className="org.apache.catalina.realm.JNDIRealm" connectionURL="ldap://myserver:389" connectionName="uid=john.doe,ou=users,dc=a9ski,dc=com" connectionPassword="secret" userPattern="uid={0},ou=users,dc=a9ski,dc=com" roleBase="ou=groups,dc=a9ski,dc=com" roleName="cn" roleSearch="(uniqueMember={0})" debug="0" />`



LDAP connection
----------------------------
The application requires several system properties in order to specify the LDAP connection:

| Property name | Default value | Description |
| ------------- | ------------- | :---------: |
| ldap-host | 10.1.52.2 | The IP address / host name of the LDAP server  |
| ldap-port | 10389 | The LDAP port (usually it is 389) |
| ldap-bind-dn | uid=john.doe,ou=users,dc=a9ski,dc=com | The "login" for accessing the LDAP server |
| ldap-password | secret | The password for accessing the LDAP server |
| ldap-user-base-dn | ou=users,dc=a9ski,dc=com | The root node of the users manageable by the application |
| ldap-user-dn-pattern | `uid=<user-id>,<user-base-dn>` | The pattern used for constructing the user DN |
| ldap-user-object-classes | inetOrgPerson,organizationalPerson,person,top | The LDAP object classes assigned to a newly created user |
| ldap-user-search-filter | (objectClass=inetOrgPerson) | Filter that should return all users in the LDAP server manageable by the application |
| ldap-group-base-dn | ou=groups,dc=a9ski,dc=com | The root node of the groups manageable by the application |
| ldap-group-dn-pattern | `uid=<group-id>,<group-base-dn>` | The pattern used for constructing the group DN |
| ldap-group-search-filter | (objectClass=groupOfUniqueNames) | Filter that should return all groups in the LDAP server manageable by the application |
| ldap-group-attribute | uniquemember | The attribute in the group entry that defines the users assigned to the group |
| ldap-group-membership-value | DN | The value that is stored in the *ldap-group-attribute*. Either it is the full *DN* of the user, or it is only the *UID* of the user. Possible values are: **DN** or **UID** |
| ldap-group-object-classes | groupOfUniqueNames,top | The LDAP object classes assigned to a newly created group |


Release
=================================================================================

0. Change maven settings.xml and add account for OSSRH
```<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
</settings>``` 
More information can be obtain from [OSSRH guide](http://central.sonatype.org/pages/ossrh-guide.html) and [Maven configuration](http://central.sonatype.org/pages/apache-maven.html)
1. `mvn clean install`
2. `mvn release:prepare`
3. checkout the newly created tag
4. `mvn -Prelease clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase=mysecret-password-for-gpg install org.sonatype.plugins:nexus-staging-maven-plugin:deploy` 
OR just execute
`release.sh mysecret-password-for-gpg`

Step 2 can be done manually: a) remove -SNAPSHOT from the version in all pom.xml files (the parent pom.xml and all module's pom.xml) b) commit the changes and create new tag with the version c) add -SNAPSHOT to all pom.xml files and increase the version (e.g. 1.0.0 to 1.0.1-SNAPSHOT)