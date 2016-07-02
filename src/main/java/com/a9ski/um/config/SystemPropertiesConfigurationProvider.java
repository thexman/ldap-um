/*
 * #%L
 * LDAP User Management
 * %%
 * Copyright (C) 2016 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.a9ski.um.config;

import org.apache.commons.lang3.StringUtils;

import com.a9ski.um.ldap.GroupMembershipValue;
import com.a9ski.um.security.RegexPasswordChecker;

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
	public String getGroupDnPattern() {
		return System.getProperty("ldap-group-dn-pattern", "uid=<group-id>,<group-base-dn>");		
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

	@Override
	public String[] getNewGroupObjectClasses() {
		final String objectClasses = System.getProperty("ldap-group-object-classes", "groupOfUniqueNames,top");
		return StringUtils.stripAll(objectClasses.split(","));
	}

	@Override
	public String getPasswordPolicyCheckerClass() {
		final String defaultChecker = RegexPasswordChecker.class.getName();
		return System.getProperty("password-policy-checker-class", defaultChecker);
	}

	@Override
	public String getPasswordPolicyCheckerParams() {
		/*	 	
	 	 * (?=.*[0-9])       # a digit must occur at least once
	 	 * (?=.*[a-z])       # a lower case letter must occur at least once
	 	 * (?=.*[A-Z])       # an upper case letter must occur at least once
	 	 * (?=.*[!@#$%^&+=]) # a special character must occur at least once
	 	 * (?=\S+$)          # no whitespace allowed in the entire string
	 	 * .{7,}             # anything, at least seven places though
		 */
		final String defaultRegEx = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\!\\@\\#\\$\\%\\^\\&\\+\\=\\`\\~\\*\\(\\)\\-\\_\\[\\]\\{\\}\\;\\:\\'\\\"\\\\\\|\\,\\<\\.\\>\\/\\?])(?=\\S+$).{7,}";
		return System.getProperty("password-policy-checker-param", defaultRegEx);
	}

}
