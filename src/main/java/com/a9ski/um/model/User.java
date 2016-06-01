/*
 * #%L
 * LDAP User Management
 * %%
 * Copyright (C) 2016 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.a9ski.um.model;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class User extends LdapEntity {

	private static final String JSON_GROUP_SEPARATOR = "; ";

	/**
	 * 
	 */
	private static final long serialVersionUID = -4265892542568338231L;

	private final String firstName;
	
	private final String lastName;
	
	private final String displayName;
	
	private final String email;
	
	private final String uid;
	
	private final Set<String> groups; 
	
	public User(String dn, String uid, String firstName, String lastName, String fullName, String displayName, String email, Set<String> groups) {
		super(dn, fullName);
		this.uid = uid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
		this.email = email;
		this.groups = groups;
	}

	public String getFullName() {
		return getCn();
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
	
	public Set<String> getGroups() {
		return groups;
	}
	
	public JSONObject toJSON() {
		final JSONObject json = new JSONObject();
		json.put("uid", uid);
		json.put("firstName", firstName);
		json.put("lastName", lastName);
		json.put("fullName", getFullName());
		json.put("displayName", displayName);
		json.put("email", email);
		if (groups != null) {
			String s = StringUtils.join(groups, JSON_GROUP_SEPARATOR);
			if (StringUtils.isNotBlank(s)) {
				s += JSON_GROUP_SEPARATOR;
			}
			json.put("groups", s);
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
		final String groupsStr = json.getString("groups");
		final Set<String> groups = new TreeSet<>(); 
		if (groupsStr != null) {
			final String[] groupNames = groupsStr.split(";");
			for(final String groupName : groupNames) {
				if (StringUtils.isNotBlank(groupName)) {
					groups.add(groupName.trim());
				}
			}
		}
		return new User(dn, uid, firstName, lastName, fullName, displayName, email, groups);
	}

	@Override
	public String toString() {
		return String.format("User [dn=%s, uid=%s, firstName=%s, lastName=%s, fullName=%s, displayName=%s, email=%s, groups=%s]",
				getDn(), uid, firstName, lastName, getCn(), displayName, email, groups);
	}

	
}
