package com.a9ski.um.model;

import java.io.Serializable;

public class LdapEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5256266949046191214L;

	private final String dn;
	
	private final String cn;
	
	public LdapEntity(String dn, String cn) {
		super();
		this.dn = dn;
		this.cn = cn;
	}

	public String getDn() {
		return dn;
	}

	public String getCn() {
		return cn;
	}
	
	
	
}
