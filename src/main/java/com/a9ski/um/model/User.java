package com.a9ski.um.model;

import java.io.Serializable;

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
	
	private String password;
	
	public User(String dn, String uid, String firstName, String lastName, String fullName, String displayName, String email) {
		super();
		this.dn = dn;
		this.uid = uid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.fullName = fullName;
		this.displayName = displayName;
		this.email = email;		
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

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDn() {
		return dn;
	}
	
	public JSONObject toJSON() {
		final JSONObject j = new JSONObject();
		j.put("uid", uid);
		j.put("firstName", firstName);
		j.put("lastName", lastName);
		j.put("fullName", fullName);
		j.put("displayName", displayName);
		j.put("email", email);		
		return j;
	}
}
