package com.a9ski.um.config;

import org.apache.commons.lang3.StringUtils;

import com.a9ski.um.ldap.GroupMembershipValue;

public class SystemPropertiesConfigurationProvider implements ConfigurationProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8862778137967571643L;

	@Override
	public String getUserDnPattern() {
		return System.getProperty("ldap-user-dn-pattern", "uid=<user-id>,<user-base-dn>");		
	}

	@Override
	public String getLdapHost() {
		return System.getProperty("ldap-host", "10.1.52.2");
	}

	@Override
	public int getLdapPort() {
		return Integer.parseInt(System.getProperty("ldap-port", "10389"));
	}

	@Override
	public String getLdapBindDn() {
		return System.getProperty("ldap-bind-dn", "uid=john.doe,ou=users,dc=a9ski,dc=com");		
	}

	@Override
	public String getLdapPassword() {
		return System.getProperty("ldap-password", "secret");
	}

	@Override
	public String getUserBaseDn() {
		return System.getProperty("ldap-user-base-dn", "ou=users,dc=a9ski,dc=com");
	}

	@Override
	public String[] getNewUserObjectClasses() {
		final String objectClasses = System.getProperty("ldap-user-object-classes", "inetOrgPerson,organizationalPerson,person,top");
		return StringUtils.stripAll(objectClasses.split(","));
	}
	
	@Override
	public String getUserSearchFilter() {
		return System.getProperty("ldap-user-search-filter", "(objectClass=inetOrgPerson)");
	}

	@Override
	public String getGroupBaseDn() {
		return System.getProperty("ldap-group-base-dn", "ou=groups,dc=a9ski,dc=com");
	}

	@Override
	public String getGroupSearchFilter() {
		return System.getProperty("ldap-group-search-filter", "(objectClass=groupOfUniqueNames)");
	}

	@Override
	public String getGroupAttribute() {
		return System.getProperty("ldap-group-attribute", "uniquemember");
	}

	@Override
	public GroupMembershipValue getGroupMembershipValue() {
		final String value = System.getProperty("ldap-group-membership-value", GroupMembershipValue.DN.name());
		return GroupMembershipValue.valueOf(value);
	}

}
