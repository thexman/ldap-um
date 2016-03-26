package com.a9ski.um.ws;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.a9ski.um.config.ConfigurationProvider;
import com.a9ski.um.config.ConfigurationProviderFactory;
import com.a9ski.um.ldap.GroupMembershipValue;
import com.a9ski.um.ldap.LdapClient;
import com.a9ski.um.model.User;
import com.a9ski.um.utils.PasswordUtils;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPSDKException;

@Path("/users")
public class UsersWebService {

	private final ConfigurationProvider configProvider = ConfigurationProviderFactory.createConfigurationProvider();
	private final String dnPattern;	
	private final LdapClient ldapClient;
	
	public UsersWebService() {
		super();
		
		final String host = configProvider.getLdapHost();
		final int port = configProvider.getLdapPort();
		final String bindDn = configProvider.getLdapBindDn();
		final String password = configProvider.getLdapPassword();
		final String userBaseDn = configProvider.getUserBaseDn();
		final String userSearchFilter = configProvider.getUserSearchFilter();
		final String[] userObjectClasses = configProvider.getNewUserObjectClasses();
		final String groupBaseDn = configProvider.getGroupBaseDn();
		final String groupSearchFilter = configProvider.getGroupSearchFilter();
		final String groupAttribute = configProvider.getGroupAttribute();
		final GroupMembershipValue groupMembershipValue = configProvider.getGroupMembershipValue(); 
		
		
		ldapClient = new LdapClient(host, port, bindDn, password, userBaseDn, userSearchFilter, userObjectClasses, groupBaseDn, groupSearchFilter, groupAttribute, groupMembershipValue);
		dnPattern = StringUtils.replace(StringUtils.replace(configProvider.getUserDnPattern(), "<user-base-dn>", userBaseDn), "<user-id>", "%s");
	}
	
	
	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "It works!";
	}
	
	@Path("json")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject json() {
		final JSONObject j = new JSONObject("{ 'status' : 'It works!' }");
		return j;
	}
	
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray listAllUsers() throws LDAPException {
		final JSONArray j = new JSONArray();
		for(final User u : ldapClient.listAllUsers()) {
			j.put(u.toJSON());
		}
		return j;
	}
	
	@Path("rows")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listAllUserRows() throws LDAPException {		
		return new JSONObject().put("rows", listAllUsers());
	}
	
	@Path("create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject create(@FormParam("user") JSONObject userJson) { 
		final User user = User.fromJSON(userJson, dnPattern);
		try { 
			final User newUser = ldapClient.createUser(user);
			final String password = userJson.optString("password");
			if (StringUtils.isNotEmpty(password)) {
				ldapClient.changePassword(newUser.getUid(), password);
			}
			return createSuccessStatus().put("user", newUser.toJSON());
		} catch (LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}
	
	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject update(@FormParam("user") JSONObject userJson)   { 
		final User user = User.fromJSON(userJson, dnPattern);
		try { 
			final User updatedUser = ldapClient.updateUser(user);
			final String password = userJson.optString("password");
			if (StringUtils.isNotEmpty(password)) {
				ldapClient.changePassword(updatedUser.getUid(), password);
			}
			return createSuccessStatus().put("user", updatedUser.toJSON());
		} catch (LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
		
	}
	
	@Path("password")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject setPassword(@QueryParam("uid")String uid, @QueryParam("newPassword")String newPassword, @QueryParam("oldPassword")String oldPassword) {
		try {
			if (ldapClient.checkPassword(String.format(dnPattern, LdapClient.escapeDnLiteral(uid)), oldPassword)) {
				ldapClient.changePassword(uid, PasswordUtils.encryptPassword(newPassword));
				return createSuccessStatus();
			} else {
				return createFailureStatus("Old password doesn't match");
			}
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
		
	}		
	
	private JSONObject createStatus(String status) {
		return new JSONObject().put("status", status);
	}
	
	private JSONObject createSuccessStatus() {
		return createStatus("success");
	}
	
	private JSONObject createFailureStatus() {
		return createStatus("failure");
	}
	
	private JSONObject createFailureStatus(Exception ex) {
		return createFailureStatus(ex.getMessage()).put("exception", true);
	}
	
	private JSONObject createFailureStatus(String message) {
		return createFailureStatus().put("message", message);
	}
	
}
