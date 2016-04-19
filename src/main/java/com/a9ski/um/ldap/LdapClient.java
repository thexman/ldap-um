package com.a9ski.um.ldap;

import static com.a9ski.um.utils.DeltaUtils.calculateDeltaAdded;
import static com.a9ski.um.utils.DeltaUtils.calculateDeltaDeleted;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.a9ski.um.ldap.exceptions.LdapCustomException;
import com.a9ski.um.ldap.exceptions.LdapUserExistsException;
import com.a9ski.um.model.Group;
import com.a9ski.um.model.User;
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
	
	
	private static final String ATTRIBUTE_LAST_NAME = "sn";

	private static final String ATTRIBUTE_FIRST_NAME = "givenname";

	private static final String ATTRIBUTE_UID = "uid";

	private static final String ATTRIBUTE_EMAIL = "mail";

	private static final String ATTRIBUTE_DISPLAY_NAME = "displayName";

	private static final String ATTRIBUTE_FULL_NAME = "cn";
	
	private static final String ATTRIBUTE_PASSWORD = "userpassword";
	
	private static final String ATTRIBUTE_OBJECT_CLASS = "objectclass";
	
	private static final String ATTRIBUTE_GROUP_NAME = "cn";
	
	private final static String SPECIAL_CHARS = ",+\"\\<>;\r\n=/";
	
	
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
	
	public LdapClient(String host, int port, String bindDn, String password, String userBaseDn, String userSearchFilter, String[] newUserObjectClasses, String groupBaseDn, String groupSearchFilter, String groupAttribute, GroupMembershipValue groupMembershipValue) {
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
		changeUserAttribute(uid, ATTRIBUTE_PASSWORD, newPassword);
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
		final String uid = "(" + ATTRIBUTE_UID + "=*" + Filter.encodeValue(search) + "*)";
		final String displayName = "(" + ATTRIBUTE_DISPLAY_NAME + "=*" + Filter.encodeValue(search) + "*)";
		final String fullName = "(" + ATTRIBUTE_FULL_NAME + "=*" + Filter.encodeValue(search) + "*)";
		final String or = "(|" + uid + displayName + fullName + ")";
		final String query = "(&" + or + userSearchFilter + ")";
		return findUsers(Filter.create(query));
	}

	public List<User> listAllUsers() throws LDAPException {		
		return findUsers(Filter.create(userSearchFilter));		
	}

	private List<User> findUsers(final Filter filter ) throws LDAPException, LDAPSearchException {
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
			changeAttribute(ldapConnection, dn, ATTRIBUTE_FULL_NAME, u.getFullName());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_DISPLAY_NAME, u.getDisplayName());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_EMAIL, u.getEmail());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_FIRST_NAME, u.getFirstName());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_LAST_NAME, u.getLastName());
			
			final Set<String> groupsAdded = calculateDeltaAdded(existingUser.getGroups(), u.getGroups());
			addUserToGroups(ldapConnection, u, groupsAdded);
			
			final Set<String> groupsDeleted = calculateDeltaDeleted(existingUser.getGroups(), u.getGroups());
			deletedUserFromGroups(ldapConnection, u, groupsDeleted);
			
			return findUserByUid(ldapConnection, u.getUid(), true);
		} finally {
			ldapConnection.close();
		}
	}

	
	public User createUser(User u) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final User existingUser = findUserByUid(ldapConnection, u.getUid(), true);
			if (existingUser != null) {				
				throw new LdapUserExistsException(String.format("User with '%s' already exists", u.getUid()));
			}
			createUser(ldapConnection, u);
			return findUserByUid(ldapConnection, u.getUid(), true);
		} finally {
			ldapConnection.close();
		}
	}
	
	public Set<String> searchGroupNames(String search) throws LDAPException {
		return getGroupNames(searchGroups(search));
	}
		
	
	public List<Group> searchGroups(String search) throws LDAPException {
		final LDAPConnection ldapConnection = getBindedConnection();
		try {
			final Filter f = Filter.create(createGroupSearchQuery(groupSearchFilter));
			return findGroups(ldapConnection, f);
		} finally {
			ldapConnection.close();
		}
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
	
	public Group findGroup(String groupName) throws LDAPException {
		final LDAPConnection ldapConnection = getBindedConnection();
		try {
			final Filter f = Filter.create(createGroupExactMatchQuery(groupName));
			final List<Group> groups = findGroups(ldapConnection, f);
			if (CollectionUtils.isNotEmpty(groups)) {
				return groups.get(0);
			} else {
				return null;
			}
		} finally {
			ldapConnection.close();
		}
	}

	private void addUserToGroups(final LDAPConnection ldapConnection, User u, final Set<String> groups)	throws LDAPException {
		if (CollectionUtils.isNotEmpty(groups)) {
			for(final String groupName : groups) {
				final Group g = findGroup(groupName);
				addAttribute(ldapConnection, g.getDn(), groupAttribute, getGroupMembershipValue(u));
			}
		}
	}
	
	private void deletedUserFromGroups(final LDAPConnection ldapConnection, User u, final Set<String> groups)	throws LDAPException {
		if (CollectionUtils.isNotEmpty(groups)) {
			for(final String groupName : groups) {
				final Group g = findGroup(groupName);
				deleteAttribute(ldapConnection, g.getDn(), groupAttribute, getGroupMembershipValue(u));
			}
		}
	}

	
	private void createUser(final LDAPConnection ldapConnection, User u) throws LDAPException {
		final String dn = String.format("uid=%s,%s", u.getUid(), userBaseDn);
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(ATTRIBUTE_FULL_NAME, u.getFullName()));
		attributes.add(new Attribute(ATTRIBUTE_DISPLAY_NAME, u.getDisplayName()));
		attributes.add(new Attribute(ATTRIBUTE_EMAIL, u.getEmail()));
		attributes.add(new Attribute(ATTRIBUTE_FIRST_NAME, u.getFirstName()));
		attributes.add(new Attribute(ATTRIBUTE_LAST_NAME, u.getLastName()));
		attributes.add(new Attribute(ATTRIBUTE_LAST_NAME, u.getLastName()));
		attributes.add(new Attribute(ATTRIBUTE_PASSWORD, UUID.randomUUID().toString()));
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

	private User toUser(LDAPConnection ldapConnection, final ReadOnlyEntry e, boolean loadUserGroups) throws LDAPException {
		final String fullName = e.getAttributeValue(ATTRIBUTE_FULL_NAME);
		final String displayName = e.getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
		final String email = e.getAttributeValue(ATTRIBUTE_EMAIL);
		final String uid = e.getAttributeValue(ATTRIBUTE_UID);
		final String firstName = e.getAttributeValue(ATTRIBUTE_FIRST_NAME);
		final String lastName = e.getAttributeValue(ATTRIBUTE_LAST_NAME);
		
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
			switch (groupMembershipValue) {
				case DN: users.add(findUserByDn(ldapConnection, member, false)); break;
				case UID: users.add(findUserByUid(ldapConnection, member, false)); break;
			}
		}
		
		final String name = e.getAttributeValue(ATTRIBUTE_GROUP_NAME);
		final Group group = new Group(e.getDN(), StringUtils.defaultString(name, ""), users);

		return group;
	}
	
	private Group toGroup(ReadOnlyEntry e) {
		final String name = e.getAttributeValue(ATTRIBUTE_GROUP_NAME);
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
	
	private Set<String> findUserGroups(LDAPConnection ldapConnection, User u) throws LDAPException {
		final Filter f = Filter.create(createGroupSearchQuery(u));
		final List<Group> userGroups = findGroups(ldapConnection, f);
		return getGroupNames(userGroups);
	}

	private Set<String> getGroupNames(final List<Group> groups) {
		final Set<String> groupNames = new TreeSet<String>();
		for(final Group g : groups) {
			groupNames.add(g.getGroupName());
		}
		return groupNames;
	}

	private List<Group> findGroups(LDAPConnection ldapConnection, final Filter f) throws LDAPSearchException {
		final SearchRequest searchRequest = new SearchRequest(groupBaseDn, SearchScope.SUB, f);
		final SearchResult r = ldapConnection.search(searchRequest);
		final List<Group> groups = new ArrayList<>();
		for(SearchResultEntry e : r.getSearchEntries()) {
			groups.add(toGroup(e));
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
	
	private String createGroupSearchQuery(String groupName) {
		final StringBuilder sb = new StringBuilder();
		boolean hasAdditionalSearchCriteria = StringUtils.isNotEmpty(groupSearchFilter);
		if (hasAdditionalSearchCriteria) {
			sb.append("(&").append(groupSearchFilter);			
		}
		sb.append("(cn=*").append(groupName).append("*)");
		if (hasAdditionalSearchCriteria) {
			sb.append(")");
		}
		return sb.toString();
	}
	
	private String createGroupExactMatchQuery(String groupName) {
		final StringBuilder sb = new StringBuilder();
		boolean hasAdditionalSearchCriteria = StringUtils.isNotEmpty(groupSearchFilter);
		if (hasAdditionalSearchCriteria) {
			sb.append("(&").append(groupSearchFilter);			
		}
		sb.append("(cn=").append(groupName).append(")");
		if (hasAdditionalSearchCriteria) {
			sb.append(")");
		}
		return sb.toString();
	}
	

	private String getGroupMembershipValue(User u) {
		switch(groupMembershipValue) {
			case DN: return u.getDn();
			case UID: return u.getUid();			
		}
		return u.getDn();
	}
	
	
}
