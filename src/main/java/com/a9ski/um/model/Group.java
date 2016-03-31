package com.a9ski.um.model;

import org.json.JSONObject;

public class Group extends LdapEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5682900963256747035L;

	public Group(String dn, String groupName) {
		super(dn, groupName);
	}
	
	public String getGroupName() {
		return getCn();
	}
	
	public JSONObject toJSON() {
		final JSONObject json = new JSONObject();
		json.put("dn", getDn());
		json.put("name", getGroupName());
		return json;
	}

}
