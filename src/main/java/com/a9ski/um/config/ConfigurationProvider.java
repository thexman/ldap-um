package com.a9ski.um.config;

import java.io.Serializable;

import com.a9ski.um.ldap.GroupMembershipValue;

public interface ConfigurationProvider extends Serializable {
	public String getUserDnPattern();
	public String getLdapHost();
	public int getLdapPort();
	public String getLdapBindDn();
	public String getLdapPassword();
	public String getUserBaseDn();
	public String getUserSearchFilter();
	public String[] getNewUserObjectClasses();
	public String getGroupBaseDn();
	public String getGroupSearchFilter();
	public String getGroupAttribute();
	public GroupMembershipValue getGroupMembershipValue();
	public String getGroupDnPattern();
	
}
