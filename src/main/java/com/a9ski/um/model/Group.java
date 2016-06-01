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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Group extends LdapEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5682900963256747035L;
	
	private static final Pattern USER_PATTERN = Pattern.compile(".*\\((.*)\\)");

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

	public static Group fromJSON(final JSONObject json, final String groupDnPattern) {
		final String name = json.getString("cn");
		final String dn = String.format(groupDnPattern, name);
		final String groupsStr = json.getString("users");
		final List<User> users;
		if (groupsStr != null) {
			final Set<String> userUids = new TreeSet<>();
			final String[] userNames = groupsStr.split(";");			
			for(final String userName : userNames) {
				if (StringUtils.isNotBlank(userName)) {
					final Matcher matcher = USER_PATTERN.matcher(userName.trim());
					if (matcher.matches()) {
						final String userUid = matcher.group(1);
						userUids.add(userUid.trim());
					}
				}
			}
			if (!userUids.isEmpty()) {
				users = new ArrayList<>();
				userUids.forEach(u -> users.add(new User("", u, "", "", "", "", "", null)));
			} else {
				users = null;
			}
		} else {
			users = null;
		}
		return new Group(dn, name, users);
	}

}
