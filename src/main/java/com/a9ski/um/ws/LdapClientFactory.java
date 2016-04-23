package com.a9ski.um.ws;

import java.io.Serializable;

import com.a9ski.um.config.ConfigurationProvider;
import com.a9ski.um.ldap.GroupMembershipValue;
import com.a9ski.um.ldap.LdapClient;

public class LdapClientFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -463574864517017618L;
	
	private final ConfigurationProvider configProvider;

	public LdapClientFactory(ConfigurationProvider configProvider) {
		super();
		this.configProvider = configProvider;
	}
	
	public LdapClient createLdapClient() {
		final String host = configProvider.getLdapHost();
		final int port = configProvider.getLdapPort();
		final String bindDn = configProvider.getLdapBindDn();
		final String password = configProvider.getLdapPassword();
		final String userBaseDn = configProvider.getUserBaseDn();
		final String userSearchFilter = configProvider.getUserSearchFilter();
		final String[] userObjectClasses = configProvider.getNewUserObjectClasses();
		final String groupBaseDn = configProvider.getGroupBaseDn();
		final String groupSearchFilter = configProvider.getGroupSearchFilter();
		final String groupAttribute = configProvider.getGroupAttribute();
		final String[] groupObjectClasses = configProvider.getNewGroupObjectClasses();
		final GroupMembershipValue groupMembershipValue = configProvider.getGroupMembershipValue(); 
		
		return new LdapClient(host, port, bindDn, password, userBaseDn, userSearchFilter, userObjectClasses, groupBaseDn, groupSearchFilter, groupAttribute, groupObjectClasses, groupMembershipValue);
	}
	

}
