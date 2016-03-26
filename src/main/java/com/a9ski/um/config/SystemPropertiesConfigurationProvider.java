package com.a9ski.um.config;

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
		return System.getProperty("ldap-host", "localhost");
	}

	@Override
	public int getLdapPort() {
		return Integer.parseInt(System.getProperty("ldap-port", "10389"));
	}

	@Override
	public String getLdapBindDn() {
		return System.getProperty("ldap-bind-dn", "uid=john.doe,ou=Users,dc=mycompany,dc=com");		
	}

	@Override
	public String getLdapPassword() {
		return System.getProperty("ldap-password", "secret");
	}

	@Override
	public String getUserBaseDn() {
		return System.getProperty("ldap-user-base-dn", "ou=people,dc=mycompany,dc=com");
	}

}
