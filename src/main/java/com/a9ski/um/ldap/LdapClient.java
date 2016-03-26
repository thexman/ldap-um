package com.a9ski.um.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.a9ski.um.ldap.exceptions.LdapCustomException;
import com.a9ski.um.ldap.exceptions.LdapUserExistsException;
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
	
	private final static String SPECIAL_CHARS = ",+\"\\<>;\r\n=/";
	
	
	private final String host;
	private final int port;
	private final String bindDn;
	private final String password;
	private final String userBaseDn;
	private final String userSearchFilter;
	private final String[] userObjectClasses;
	private final SingleServerSet serverSet;
	
	
	public static String escapeDnLiteral(String uid) {
		if (StringUtils.isNotBlank(uid)) {
			for(int i = 0; i < SPECIAL_CHARS.length(); i++) {
				uid = uid.replace(SPECIAL_CHARS.charAt(i), '.');
			}
		}
		return uid;
	}
	
	public LdapClient(String host, int port, String bindDn, String password, String userBaseDn, String userSearchFilter, String[] newUserObjectClasses) {
		super();
		this.host = host;
		this.port = port;
		this.bindDn = bindDn;
		this.password = password;
		this.userBaseDn = userBaseDn;
		this.userSearchFilter = userSearchFilter;
		this.userObjectClasses = newUserObjectClasses;
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
			return findUserByUid(ldapConnection, uid);
		} finally {
			ldapConnection.close();
		}
	}

	public List<User> listAllUsers() throws LDAPException {
		final List<User> users = new ArrayList<>();
		
		final LDAPConnection ldapConnection = getBindedConnection();		
				
//		final Filter f = Filter.create("(&(objectClass=organizationalPerson))");		
		final Filter f = Filter.create(userSearchFilter);
		try {			
			final SearchRequest searchRequest = new SearchRequest(userBaseDn, SearchScope.SUB, f);
			final SearchResult r = ldapConnection.search(searchRequest);			
			for(SearchResultEntry e : r.getSearchEntries()) {
				final User user = toUser(e);
				users.add(user);
			}
			return users;
		} finally {
			ldapConnection.close();
		}		
	}
	
	public User updateUser(User u) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final User existingUser = findUserByUid(ldapConnection, u.getUid());
			if (existingUser == null) {
				throw new LdapCustomException("User doesn't exists");
			}
			final String dn = existingUser.getDn(); 
			changeAttribute(ldapConnection, dn, ATTRIBUTE_FULL_NAME, u.getFullName());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_DISPLAY_NAME, u.getDisplayName());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_EMAIL, u.getEmail());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_FIRST_NAME, u.getFirstName());
			changeAttribute(ldapConnection, dn, ATTRIBUTE_LAST_NAME, u.getLastName());
			return findUserByUid(ldapConnection, u.getUid());
		} finally {
			ldapConnection.close();
		}
	}
	
	public User createUser(User u) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			final User existingUser = findUserByUid(ldapConnection, u.getUid());
			if (existingUser != null) {				
				throw new LdapUserExistsException(String.format("User with '%s' already exists", u.getUid()));
			}
			createUser(ldapConnection, u);
			return findUserByUid(ldapConnection, u.getUid());
		} finally {
			ldapConnection.close();
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
		final User user = findUserByUid(ldapConnection, uid);
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
	
	private User findUserByUid(final LDAPConnection ldapConnection, String uid) throws LDAPException, LDAPSearchException, LdapCustomException {
		final Filter f = Filter.create("(&(uid=" + Filter.encodeValue(uid) + ")" + userSearchFilter + ")");
		final SearchRequest searchRequest = new SearchRequest(userBaseDn, SearchScope.SUB, f);
		final SearchResult r = ldapConnection.search(searchRequest);
		final List<SearchResultEntry> results = r.getSearchEntries();
		if (results.size() > 1) {
			throw new LdapCustomException("Too many users with given uid");
		}
		User u = null;
		if (results.size() > 0) {
			u = toUser(results.get(0));
		}
		return u;
	}

	private User toUser(final ReadOnlyEntry e) {
		final String fullName = e.getAttributeValue(ATTRIBUTE_FULL_NAME);
		final String displayName = e.getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
		final String email = e.getAttributeValue(ATTRIBUTE_EMAIL);
		final String uid = e.getAttributeValue(ATTRIBUTE_UID);
		final String firstName = e.getAttributeValue(ATTRIBUTE_FIRST_NAME);
		final String lastName = e.getAttributeValue(ATTRIBUTE_LAST_NAME);
		
		final User user = new User(e.getDN(), uid, firstName, lastName, fullName, displayName, email);
		return user;
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
}
