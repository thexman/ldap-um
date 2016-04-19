package com.a9ski.um.model;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Group extends LdapEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5682900963256747035L;

	private final List<User> users;
	
	public Group(String dn, String groupName) {
		this(dn, groupName, null);
	}
	
	public Group(String dn, String groupName, List<User> users) {
		super(dn, groupName);
		this.users = users;
	}
	
	public String getGroupName() {
		return getCn();
	}
	
	public List<User> getUsers() {
		return users;
	}
	
	public JSONObject toJSON() {
		final JSONObject json = new JSONObject();
		json.put("dn", getDn());
		json.put("name", getGroupName());
		if (users != null) {
			final JSONArray jarr = new JSONArray();
			users.forEach(u -> jarr.put(u.toJSON()));
			json.put("users", jarr);
		}
		return json;
	}

}
