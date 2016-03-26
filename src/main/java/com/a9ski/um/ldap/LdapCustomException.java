package com.a9ski.um.ldap;

import com.unboundid.util.LDAPSDKException;

public class LdapCustomException extends LDAPSDKException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8540147683187966932L;

	public LdapCustomException(String message, Throwable cause) {
		super(message, cause);
	}

	public LdapCustomException(String message) {
		super(message);
	}

}
