package com.a9ski.um.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4265892542568338231L;

	private final String dn;
	
	private final String fullName;
	
	private final String firstName;
	
	private final String lastName;
	
	private final String displayName;
	
	private final String email;
	
	private final String uid;
	
	private final Set<String> groups; 
	
	public User(String dn, String uid, String firstName, String lastName, String fullName, String displayName, String email, Set<String> groups) {
		super();
		this.dn = dn;
		this.uid = uid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.fullName = fullName;
		this.displayName = displayName;
		this.email = email;
		this.groups = groups;
	}

	public String getFullName() {
		return fullName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmail() {
		return email;
	}

	public String getUid() {
		return uid;
	}
	
	public String getDn() {
		return dn;
	}
	
	public Set<String> getGroups() {
		return groups;
	}
	
	public JSONObject toJSON() {
		final JSONObject json = new JSONObject();
		json.put("uid", uid);
		json.put("firstName", firstName);
		json.put("lastName", lastName);
		json.put("fullName", fullName);
		json.put("displayName", displayName);
		json.put("email", email);
		if (groups != null) {
			json.put("groups", new JSONArray(groups));
		}
		return json;
	}
	
	public static User fromJSON(JSONObject json, String userDnPattern) {		
		final String uid = json.getString("uid");
		final String firstName = json.getString("firstName");
		final String lastName = json.getString("lastName");
		final String fullName = json.getString("fullName");
		final String displayName = json.getString("displayName");
		final String email = json.getString("email");
		final String dn = String.format(userDnPattern, uid);
		return new User(dn, uid, firstName, lastName, fullName, displayName, email, null);
	}

	@Override
	public String toString() {
		return String.format("User [dn=%s, uid=%s, firstName=%s, lastName=%s, fullName=%s, displayName=%s, email=%s, groups=%s]",
				dn, uid, firstName, lastName, fullName, displayName, email, groups);
	}

	
}
