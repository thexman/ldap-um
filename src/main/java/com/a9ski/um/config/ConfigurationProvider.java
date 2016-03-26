package com.a9ski.um.config;

import java.io.Serializable;

public interface ConfigurationProvider extends Serializable {
	public String getUserDnPattern();
	public String getLdapHost();
	public int getLdapPort();
	public String getLdapBindDn();
	public String getLdapPassword();
	public String getUserBaseDn();
}
