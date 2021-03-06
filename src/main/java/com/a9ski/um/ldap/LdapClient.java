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
package com.a9ski.um.ldap;

import static com.a9ski.um.utils.DeltaUtils.calculateDeltaAdded;
import static com.a9ski.um.utils.DeltaUtils.calculateDeltaDeleted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.a9ski.um.ldap.exceptions.LdapCustomException;
import com.a9ski.um.ldap.exceptions.LdapGroupExistsException;
import com.a9ski.um.ldap.exceptions.LdapUserExistsException;
import com.a9ski.um.model.Group;
import com.a9ski.um.model.User;
import com.a9ski.um.utils.DeltaUtils;
import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ReadOnlyEntry;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.SingleServerSet;
import com.unboundid.util.LDAPSDKException;

public class LdapClient {
	
	private static final String USER_ATTRIBUTE_LAST_NAME = "sn";

	private static final String USER_ATTRIBUTE_FIRST_NAME = "givenname";

	private static final String USER_ATTRIBUTE_UID = "uid";

	private static final String USER_ATTRIBUTE_EMAIL = "mail";

	private static final String USER_ATTRIBUTE_DISPLAY_NAME = "displayName";

	private static final String USER_ATTRIBUTE_FULL_NAME = "cn";
	
	private static final String USER_ATTRIBUTE_PASSWORD = "userpassword";
	
	private static final String ATTRIBUTE_OBJECT_CLASS = "objectclass";
	
	private static final String GROUP_ATTRIBUTE_GROUP_NAME = "cn";
	
	private final static String SPECIAL_CHARS = ",+\"\\<>;\r\n=/";

	protected final Logger logger = LoggerFactory.getLogger(LdapClient.class);
	
	
	private final String host;
	private final int port;
	private final String bindDn;
	private final String password;
	private final String userBaseDn;
	private final String userSearchFilter;
	private final String[] userObjectClasses;	
	private final String groupBaseDn;
	private final String groupSearchFilter;
	private final String groupAttribute;
	private final String[] groupObjectClasses;
	private final GroupMembershipValue groupMembershipValue;
	private final SingleServerSet serverSet;
	
	
	public static String escapeDnLiteral(String uid) {
		if (StringUtils.isNotBlank(uid)) {
			for(int i = 0; i < SPECIAL_CHARS.length(); i++) {
				uid = uid.replace(SPECIAL_CHARS.charAt(i), '.');
			}
		}
		return uid;
	}
	
	public LdapClient(String host, int port, String bindDn, String password, String userBaseDn, String userSearchFilter, String[] newUserObjectClasses, String groupBaseDn, String groupSearchFilter, String groupAttribute, String[] newGroupObjectClasses, GroupMembershipValue groupMembershipValue) {
		super();
		this.host = host;
		this.port = port;
		this.bindDn = bindDn;
		this.password = password;
		this.userBaseDn = userBaseDn;
		this.userSearchFilter = userSearchFilter;
		this.userObjectClasses = newUserObjectClasses;
		this.groupBaseDn = groupBaseDn;
		this.groupSearchFilter = groupSearchFilter;
		this.groupAttribute = groupAttribute;
		this.groupObjectClasses = newGroupObjectClasses;
		this.groupMembershipValue = groupMembershipValue;
		this.serverSet = new SingleServerSet(host, port);
	}
	
	
	
	public boolean checkPassword(String bindDn, String password) throws LDAPException {
		final BindRequest bindRequest = new SimpleBindRequest(bindDn, password);
		final LDAPConnection ldapConnection = serverSet.getConnection();		
				
		try {			
			ldapConnection.bind(bindRequest);
			return true;
		} catch (final LDAPException ex) {
			return false;
		} finally {
			ldapConnection.close();
		}		
	}
	
	public void changePassword(String uid, String newPassword) throws LDAPSDKException {
		changeUserAttribute(uid, USER_ATTRIBUTE_PASSWORD, newPassword);
	}
	
	public User findUserByUid(String uid) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			return findUserByUid(ldapConnection, uid, true);
		} finally {
			ldapConnection.close();
		}
	}
	
	public List<User> searchUsers(String search) throws LDAPSDKException {
		final List<User> users = new ArrayList<>();
		if (StringUtils.isNotBlank(search)) {
			final String uid = "(" + USER_ATTRIBUTE_UID + "=*" + Filter.encodeValue(search) + "*)";
			final String displayName = "(" + USER_ATTRIBUTE_DISPLAY_NAME + "=*" + Filter.encodeValue(search) + "*)";
			final String fullName = "(" + USER_ATTRIBUTE_FULL_NAME + "=*" + Filter.encodeValue(search) + "*)";
			final String or = "(|" + uid + displayName + fullName + ")";
			final String query = "(&" + or + userSearchFilter + ")";
			users.addAll(findUsers(Filter.create(query)));
		}
		return users;
	}

	public List<User> listAllUsers() throws LDAPException, LdapCustomException {		
		return findUsers(Filter.create(userSearchFilter));		
	}

	private List<User> findUsers(final Filter filter ) throws LDAPException, LDAPSearchException, LdapCustomException {
		logger.debug("Searching for LDAP users with filter: " + filter.toString());
		final List<User> users = new ArrayList<>();
		final LDAPConnection ldapConnection = getBindedConnection();		
		
		try {			
			final SearchRequest searchRequest = new SearchRequest(userBaseDn, SearchScope.SUB, filter);
			final SearchResult r = ldapConnection.search(searchRequest);			
			for(SearchResultEntry e : r.getSearchEntries()) {
				final User user = toUser(ldapConnection, e, true);
				users.add(user);
			}
			
			for(final User u : users) {
				u.getGroups().addAll(findUserGroups(ldapConnection, u));
			}
			
			return users;
		} finally {
			ldapConnection.close();
		}
	}
	
	public User updateUser(User u) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final User existingUser = findUserByUid(ldapConnection, u.getUid(), true);
			if (existingUser == null) {
				throw new LdapCustomException("User doesn't exists");
			}
			final String dn = existingUser.getDn(); 
			changeAttribute(ldapConnection, dn, USER_ATTRIBUTE_FULL_NAME, u.getFullName());
			changeAttribute(ldapConnection, dn, USER_ATTRIBUTE_DISPLAY_NAME, u.getDisplayName());
			changeAttribute(ldapConnection, dn, USER_ATTRIBUTE_EMAIL, u.getEmail());
			changeAttribute(ldapConnection, dn, USER_ATTRIBUTE_FIRST_NAME, u.getFirstName());
			changeAttribute(ldapConnection, dn, USER_ATTRIBUTE_LAST_NAME, u.getLastName());
			
			final Set<String> groupsAdded = calculateDeltaAdded(existingUser.getGroups(), u.getGroups());
			addUserToGroups(ldapConnection, u, groupsAdded);
			
			final Set<String> groupsDeleted = calculateDeltaDeleted(existingUser.getGroups(), u.getGroups());
			deletedUserFromGroups(ldapConnection, u, groupsDeleted);
			
			return findUserByUid(ldapConnection, u.getUid(), true);
		} finally {
			ldapConnection.close();
		}
	}

	
	public User createUser(final User u) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final User existingUser = findUserByUid(ldapConnection, u.getUid(), true);
			if (existingUser != null) {				
				throw new LdapUserExistsException(String.format("User with uuid '%s' already exists", u.getUid()));
			}
			createUser(ldapConnection, u);
			return findUserByUid(ldapConnection, u.getUid(), true);
		} finally {
			ldapConnection.close();
		}
	}
	
	public Group createGroup(final Group g) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final Group existingGroup = findGroup(ldapConnection, g.getGroupName(), false);
			if (existingGroup != null) {				
				throw new LdapGroupExistsException(String.format("Group with name '%s' already exists", g.getGroupName()));
			}
			createGroup(ldapConnection, g);
			return findGroup(ldapConnection, g.getGroupName(), false);
		} finally {
			ldapConnection.close();
		}
	}
	
	public Group updateGroup(Group g) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final Group existingGroup = findGroup(ldapConnection, g.getGroupName(), true);
			if (existingGroup == null) {				
				throw new LdapCustomException("Group doesn't exists");
			}
			updateGroup(ldapConnection, g, existingGroup);
			return findGroup(ldapConnection, g.getGroupName(), false);
		} finally {
			ldapConnection.close();
		}

	}
	
	
	private void createGroup(final LDAPConnection ldapConnection, final Group g) throws LDAPSDKException {		
		if (CollectionUtils.isNotEmpty(g.getUsers())) {
			final Set<User> users = new HashSet<User>();
			for(final User u : g.getUsers()) {
				if (StringUtils.isNotBlank(u.getUid())) {
					final User existingUser = findUserByUid(u.getUid());
					if (existingUser != null) {
						users.add(existingUser);
					}
				}
			}
			final String dn = String.format("cn=%s,%s", g.getGroupName(), groupBaseDn);
			final List<Attribute> attributes = new ArrayList<>();
			attributes.add(new Attribute(GROUP_ATTRIBUTE_GROUP_NAME, g.getGroupName()));
			attributes.add(new Attribute(ATTRIBUTE_OBJECT_CLASS, groupObjectClasses));
			for(final User u : users) {
				attributes.add(new Attribute(groupAttribute, getGroupMembershipValue(u)));
			}
			final AddRequest addRequest = new AddRequest(dn, attributes);  
			ldapConnection.add(addRequest);
		} else {
			throw new LdapCustomException("Empty users");
		}
	}
	
	private void updateGroup(final LDAPConnection ldapConnection, final Group g, final Group existingGroup) throws LDAPSDKException {		
		if (CollectionUtils.isNotEmpty(g.getUsers())) {
			final Set<User> users = new HashSet<User>();
			for(final User u : g.getUsers()) {
				if (StringUtils.isNotBlank(u.getUid())) {
					final User existingUser = findUserByUid(u.getUid());
					if (existingUser != null) {
						users.add(existingUser);
					}
				}
			}
			
			final Set<String> oldUsers = getGroupMembershipValues(existingGroup.getUsers());
			final Set<String> newUsers = getGroupMembershipValues(users);
			
			final String dn = String.format("cn=%s,%s", g.getGroupName(), groupBaseDn);
			final List<Modification> modifications = new ArrayList<>();			
			for(final String val : DeltaUtils.calculateDeltaAdded(oldUsers, newUsers)) {								
				modifications.add(new Modification(ModificationType.ADD, groupAttribute, val));
			}
			for(final String val : DeltaUtils.calculateDeltaDeleted(oldUsers, newUsers)) {
				modifications.add(new Modification(ModificationType.DELETE, groupAttribute, val));
			}
			final ModifyRequest request = new ModifyRequest(dn, modifications);  
			ldapConnection.modify(request);
		} else {
			throw new LdapCustomException("Empty users");
		}
	}

	public Set<String> searchGroupNames(String search) throws LDAPException, LdapCustomException {
		return getGroupNames(searchGroups(search));
	}
		
	
	public List<Group> searchGroups(String search) throws LDAPException, LdapCustomException {
		final List<Group> groups = new ArrayList<>();
		if (StringUtils.isNotBlank(search)) {
			final LDAPConnection ldapConnection = getBindedConnection();
			try {
				final Filter f = Filter.create(createGroupSearchQuery(search, false));
				logger.debug("Searching for LDAP groups with filter: " + f.toString());
				groups.addAll(findGroups(ldapConnection, f, false));
			} finally {
				ldapConnection.close();
			}
		}
		return groups;
	}
	
	public List<Group> listAllGroups() throws LDAPException, LdapCustomException {
		final List<Group> groups = new ArrayList<>();
		
		final LDAPConnection ldapConnection = getBindedConnection();		
				
		final Filter f = Filter.create(groupSearchFilter);
		try {			
			final SearchRequest searchRequest = new SearchRequest(groupBaseDn, SearchScope.SUB, f);
			final SearchResult r = ldapConnection.search(searchRequest);			
			for(SearchResultEntry e : r.getSearchEntries()) {
				final Group group = toGroup(ldapConnection, e);
				groups.add(group);
			}
			
			return groups;
		} finally {
			ldapConnection.close();
		}		
	}
	
	public Group findGroup(String groupName) throws LDAPException, LdapCustomException {
		final LDAPConnection ldapConnection = getBindedConnection();
		try {
			return findGroup(ldapConnection, groupName, false);
		} finally {
			ldapConnection.close();
		}
	}

	private Group findGroup(final LDAPConnection ldapConnection, String groupName, boolean resolveUsers)
			throws LDAPException, LDAPSearchException, LdapCustomException {
		final Filter f = Filter.create(createGroupSearchQuery(groupName, true));
		final List<Group> groups = findGroups(ldapConnection, f, resolveUsers);
		if (CollectionUtils.isNotEmpty(groups)) {
			return groups.get(0);
		} else {
			return null;
		}
	}
	
	private void addUserToGroups(final LDAPConnection ldapConnection, User u, final Set<String> groups)	throws LDAPException, LdapCustomException {
		if (CollectionUtils.isNotEmpty(groups)) {
			for(final String groupName : groups) {
				final Group g = findGroup(groupName);
				addAttribute(ldapConnection, g.getDn(), groupAttribute, getGroupMembershipValue(u));
			}
		}
	}
	
	private void deletedUserFromGroups(final LDAPConnection ldapConnection, User u, final Set<String> groups)	throws LDAPException, LdapCustomException {
		if (CollectionUtils.isNotEmpty(groups)) {
			for(final String groupName : groups) {
				final Group g = findGroup(groupName);
				deleteAttribute(ldapConnection, g.getDn(), groupAttribute, getGroupMembershipValue(u));
			}
		}
	}

	
	private void createUser(final LDAPConnection ldapConnection, User u) throws LDAPException, LdapCustomException {
		final String dn = String.format("uid=%s,%s", u.getUid(), userBaseDn);
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(USER_ATTRIBUTE_FULL_NAME, u.getFullName()));
		attributes.add(new Attribute(USER_ATTRIBUTE_DISPLAY_NAME, u.getDisplayName()));
		attributes.add(new Attribute(USER_ATTRIBUTE_EMAIL, u.getEmail()));
		attributes.add(new Attribute(USER_ATTRIBUTE_FIRST_NAME, u.getFirstName()));
		attributes.add(new Attribute(USER_ATTRIBUTE_LAST_NAME, u.getLastName()));
		attributes.add(new Attribute(USER_ATTRIBUTE_LAST_NAME, u.getLastName()));
		attributes.add(new Attribute(USER_ATTRIBUTE_PASSWORD, UUID.randomUUID().toString()));
		attributes.add(new Attribute(ATTRIBUTE_OBJECT_CLASS, userObjectClasses));
		final AddRequest addRequest = new AddRequest(dn, attributes);  
		ldapConnection.add(addRequest);
		
		addUserToGroups(ldapConnection, u, u.getGroups());
	}
	
	private void changeUserAttribute(String uid, String attribute, String value) throws LDAPSDKException {		
		final LDAPConnection ldapConnection = getBindedConnection();
		try {
			changeUserAttribute(ldapConnection, uid, attribute, value);
		} finally {
			ldapConnection.close();
		}
	}

	private void changeUserAttribute(final LDAPConnection ldapConnection, String uid, String attribute, String value) throws LDAPException, LDAPSearchException, LdapCustomException {
		final User user = findUserByUid(ldapConnection, uid, true);
		if (user != null) {
			final String dn = user.getDn();
			changeAttribute(ldapConnection, dn, attribute, value);
		}
	}

	private void changeAttribute(final LDAPConnection ldapConnection, final String dn, String attribute, String value) 	throws LDAPException {
		final Modification mod = new Modification(ModificationType.REPLACE, attribute, value); 
		final ModifyRequest modifyRequest = new ModifyRequest(dn, mod);
		ldapConnection.modify(modifyRequest);
	}
	
	private void addAttribute(final LDAPConnection ldapConnection, final String dn, String attribute, String value) 	throws LDAPException {
		final Modification mod = new Modification(ModificationType.ADD, attribute, value); 
		final ModifyRequest modifyRequest = new ModifyRequest(dn, mod);
		ldapConnection.modify(modifyRequest);
	}
	
	private void deleteAttribute(final LDAPConnection ldapConnection, final String dn, String attribute, String value) 	throws LDAPException {
		final Modification mod = new Modification(ModificationType.DELETE, attribute, value); 
		final ModifyRequest modifyRequest = new ModifyRequest(dn, mod);
		ldapConnection.modify(modifyRequest);
	}
	
	private User findUserByUid(final LDAPConnection ldapConnection, String uid, boolean loadUserGroups) throws LDAPException, LDAPSearchException, LdapCustomException {
		final Filter f = Filter.create("(&(uid=" + Filter.encodeValue(uid) + ")" + userSearchFilter + ")");
		final SearchRequest searchRequest = new SearchRequest(userBaseDn, SearchScope.SUB, f);
		final SearchResult r = ldapConnection.search(searchRequest);
		final List<SearchResultEntry> results = r.getSearchEntries();
		if (results.size() > 1) {
			throw new LdapCustomException("Too many users with given uid");
		}
		User u = null;
		if (results.size() > 0) {
			u = toUser(ldapConnection, results.get(0), loadUserGroups);
		}
		return u;
	}
	
	private User findUserByDn(final LDAPConnection ldapConnection, String dn, boolean loadUserGroups) throws LDAPException, LDAPSearchException, LdapCustomException {
		final SearchResultEntry e = ldapConnection.getEntry(dn);
		final User u = toUser(ldapConnection, e, loadUserGroups);
		return u;
	}

	private User toUser(LDAPConnection ldapConnection, final ReadOnlyEntry e, boolean loadUserGroups) throws LDAPException, LdapCustomException {
		final String fullName = e.getAttributeValue(USER_ATTRIBUTE_FULL_NAME);
		final String displayName = e.getAttributeValue(USER_ATTRIBUTE_DISPLAY_NAME);
		final String email = e.getAttributeValue(USER_ATTRIBUTE_EMAIL);
		final String uid = e.getAttributeValue(USER_ATTRIBUTE_UID);
		final String firstName = e.getAttributeValue(USER_ATTRIBUTE_FIRST_NAME);
		final String lastName = e.getAttributeValue(USER_ATTRIBUTE_LAST_NAME);
		
		final User user = new User(e.getDN(), uid, firstName, lastName, fullName, displayName, email, new TreeSet<String>());
		if (loadUserGroups) {
			user.getGroups().addAll(findUserGroups(ldapConnection, user));
		}
		return user;
	}
	
	private Group toGroup(LDAPConnection ldapConnection, final ReadOnlyEntry e) throws LDAPException, LdapCustomException {
		final List<User> users = new ArrayList<>();
		final String[] members = e.getAttributeValues(groupAttribute);
		for (final String member : members) {
			final User u;
			switch (groupMembershipValue) {
				case DN: u = findUserByDn(ldapConnection, member, false); break;
				case UID: u = findUserByUid(ldapConnection, member, false); break;
				default: u = null;
			}
			if (u != null) {
				users.add(u);
			}
		}
		
		final String name = e.getAttributeValue(GROUP_ATTRIBUTE_GROUP_NAME);
		final Group group = new Group(e.getDN(), StringUtils.defaultString(name, ""), users);

		return group;
	}
	
	private Group toGroup(ReadOnlyEntry e) {
		final String name = e.getAttributeValue(GROUP_ATTRIBUTE_GROUP_NAME);
		final Group g = new Group(e.getDN(), StringUtils.defaultString(name, ""));
		return g;
	}
	
	private LDAPConnection getBindedConnection() throws LDAPException {
		final BindRequest bindRequest = new SimpleBindRequest(bindDn, password);
		final LDAPConnection ldapConnection = serverSet.getConnection();
		
		try {
			ldapConnection.bind(bindRequest);
			ldapConnection.setConnectionName(String.format("connection-to-%s:%d", host, port));
		} catch(LDAPException ex) {
			ldapConnection.close();
			throw ex;
		}
		return ldapConnection;
	}
	
	private Set<String> findUserGroups(LDAPConnection ldapConnection, User u) throws LDAPException, LdapCustomException {
		final Filter f = Filter.create(createGroupSearchQuery(u));
		final List<Group> userGroups = findGroups(ldapConnection, f, false);
		return getGroupNames(userGroups);
	}

	private Set<String> getGroupNames(final List<Group> groups) {
		final Set<String> groupNames = new TreeSet<String>();
		groups.stream().forEach(g -> groupNames.add(g.getGroupName()));
		return groupNames;
	}

	private List<Group> findGroups(LDAPConnection ldapConnection, final Filter f, boolean resolveUsers) throws LDAPException, LdapCustomException {
		final SearchRequest searchRequest = new SearchRequest(groupBaseDn, SearchScope.SUB, f);
		final SearchResult r = ldapConnection.search(searchRequest);
		final List<Group> groups = new ArrayList<>();
		for(SearchResultEntry e : r.getSearchEntries()) {
			final Group g;
			if (resolveUsers) {
				g = toGroup(ldapConnection, e);
			} else {
				g = toGroup(e);
			}
			groups.add(g);
		}
		return groups;
	}

	private String createGroupSearchQuery(User u) {
		final StringBuilder sb = new StringBuilder();
		boolean hasAdditionalSearchCriteria = StringUtils.isNotEmpty(groupSearchFilter);
		if (hasAdditionalSearchCriteria) {
			sb.append("(&").append(groupSearchFilter);			
		}
		sb.append("(").append(groupAttribute).append("=").append(getGroupMembershipValue(u)).append(")");
		if (hasAdditionalSearchCriteria) {
			sb.append(")");
		}
		return sb.toString();
	}
	
	private String createGroupSearchQuery(String groupName, boolean exactMatch) {
		final StringBuilder sb = new StringBuilder();
		final boolean hasAdditionalSearchCriteria = StringUtils.isNotEmpty(groupSearchFilter);
		String prefix = "";
		String suffix = "";
		if (hasAdditionalSearchCriteria) {
			prefix = "(&" + groupSearchFilter;
			suffix = ")";
		}
		String like = "";
		if (!exactMatch) {
			like = "*";
		}
		sb.append(prefix).append("(cn=").append(like).append(Filter.encodeValue(groupName)).append(like).append(")").append(suffix);
		return sb.toString();
	}
	
	private Set<String> getGroupMembershipValues(Collection<User> users) {
		final Set<String> values = new HashSet<>();
		if (CollectionUtils.isNotEmpty(users)) {
			users.stream().forEach(u -> values.add(getGroupMembershipValue(u)));
		}
		return values;
	}

	private String getGroupMembershipValue(User u) {
		switch(groupMembershipValue) {
			case DN: return u.getDn();
			case UID: return u.getUid();			
		}
		return u.getDn();
	}
}
