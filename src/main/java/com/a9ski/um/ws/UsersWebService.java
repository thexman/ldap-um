package com.a9ski.um.ws;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

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

import com.a9ski.um.ldap.LdapClient;
import com.a9ski.um.model.User;
import com.a9ski.um.utils.PasswordUtils;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPSDKException;

@Path("/users")
public class UsersWebService extends AbstractWebService {

	private final String userDnPattern;	
	public UsersWebService() {
		super();
		
		
		final String userBaseDn = configProvider.getUserBaseDn();
		
		userDnPattern = StringUtils.replace(StringUtils.replace(configProvider.getUserDnPattern(), "<user-base-dn>", userBaseDn), "<user-id>", "%s");
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
		final User user = User.fromJSON(userJson, userDnPattern);
		try { 
			final User newUser = ldapClient.createUser(user);
			final String password = userJson.optString("password");
			if (StringUtils.isNotEmpty(password)) {
				ldapClient.changePassword(newUser.getUid(), password);
			}
			return createSuccessStatus().put("user", newUser.toJSON());
		} catch (final LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}
	
	
	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject update(@FormParam("user") JSONObject userJson)   { 
		final User user = User.fromJSON(userJson, userDnPattern);
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
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject setPassword(@FormParam("uid")String uid, @FormParam("newPassword")String newPassword, @FormParam("oldPassword")String oldPassword) {
		try {
			if (ldapClient.checkPassword(String.format(userDnPattern, LdapClient.escapeDnLiteral(uid)), oldPassword)) {
				ldapClient.changePassword(uid, PasswordUtils.encryptPassword(newPassword));
				return createSuccessStatus();
			} else {
				return createFailureStatus("Old password doesn't match");
			}
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}
	
	@Path("searchUsers")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject searchUsers(@QueryParam("search")String search) {
		try {
			final List<User> users = ldapClient.searchUsers(search);
			final JSONArray jarr = new JSONArray();
			users.forEach(u -> jarr.put(u.toJSON()));			
			return createSuccessStatus().put("users", jarr);
		} catch (final LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}
	
	@Path("currentUser")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject currentUsers() {
		final User currentUser = new User("dn", "uid", "firstName", "lastName", "fullName", "displayName", "email", null);		
		return createSuccessStatus().put("currentUser", currentUser.toJSON());
	}
	
}
