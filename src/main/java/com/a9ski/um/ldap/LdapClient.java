package com.a9ski.um.ldap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.a9ski.um.model.User;
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


	private final static String SPECIAL_CHARS = ",+\"\\<>;\r\n=/";
	
	
	private final String host;
	private final int port;
	private final String bindDn;
	private final String password;
	private final String baseDn;
	private final SingleServerSet serverSet;
	
	public LdapClient(String host, int port, String bindDn, String password, String baseDn) {
		super();
		this.host = host;
		this.port = port;
		this.bindDn = bindDn;
		this.password = password;
		this.baseDn = baseDn;
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
		changeAttribute(uid, ATTRIBUTE_PASSWORD, newPassword);
	}
	
	protected void changeAttribute(String uid, String attribute, String value) throws LDAPSDKException {		
		final LDAPConnection ldapConnection = getBindedConnection();
		
		try {
			final User user = findUserByUid(ldapConnection, uid);
			if (user != null) {
				final Modification mod = new Modification(ModificationType.REPLACE, attribute, value); 
				final ModifyRequest modifyRequest = new ModifyRequest(user.getDn(), mod);
				ldapConnection.modify(modifyRequest);
			}
		} finally {
			ldapConnection.close();
		}
	}
	
	public User findUserByUid(String uid) throws LDAPSDKException {
		final LDAPConnection ldapConnection = getBindedConnection();				
		try {
			return findUserByUid(ldapConnection, uid);
		} finally {
			ldapConnection.close();
		}
	}

	private User findUserByUid(final LDAPConnection ldapConnection, String uid) throws LDAPException, LDAPSearchException, LdapCustomException {
		final Filter f = Filter.create("(&(objectClass=organizationalPerson)(uid=" + Filter.encodeValue(uid) + "))");
		final SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.SUB, f);
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

	public List<User> listAllUsers() throws LDAPException {
		final List<User> users = new ArrayList<>();
		
		final LDAPConnection ldapConnection = getBindedConnection();		
		
		//final Filter f = Filter.create("(&(objectClass=organizationalPerson)(uid=john.doe))");
		final Filter f = Filter.create("(&(objectClass=organizationalPerson))");		
		try {			
			final SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.SUB, f);
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
	
	public static String escapeDnLiteral(String uid) {
		if (StringUtils.isNotBlank(uid)) {
			for(int i = 0; i < SPECIAL_CHARS.length(); i++) {
				uid = uid.replace(SPECIAL_CHARS.charAt(i), '.');
			}
		}
		return uid;
	}
	
	public LDAPConnection getBindedConnection() throws LDAPException {
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
