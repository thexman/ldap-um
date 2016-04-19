package com.a9ski.um.ws;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.a9ski.um.config.ConfigurationProvider;
import com.a9ski.um.config.ConfigurationProviderFactory;
import com.a9ski.um.ldap.LdapClient;
import com.a9ski.um.ldap.exceptions.LdapCustomException;
import com.a9ski.um.model.Group;
import com.a9ski.um.model.User;
import com.a9ski.um.utils.PasswordUtils;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPSDKException;

@Path("/groups")
public class GroupsWebService extends AbstractWebService {

	private final String groupDnPattern;
	
	public GroupsWebService() {
		super();
		
		final String groupBaseDn = configProvider.getGroupBaseDn();
		
		groupDnPattern = StringUtils.replace(StringUtils.replace(configProvider.getGroupDnPattern(), "<group-base-dn>", groupBaseDn), "<group-id>", "%s");
	}
	
	
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray listAllGroups() throws LDAPException, LdapCustomException {
		final JSONArray j = new JSONArray();
		ldapClient.listAllGroups().forEach(g -> j.put(g.toJSON()));
		return j;
	}
	
	@Path("rows")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listAllGroupRows() throws LDAPException, LdapCustomException {		
		return new JSONObject().put("rows", listAllGroups());
	}
	
	@Path("create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject create(@FormParam("group") JSONObject groupJson) { 
//		final Group user = Group.fromJSON(groupJson, dnPattern);
//		try { 
//			final User newUser = ldapClient.createUser(user);
//			final String password = userJson.optString("password");
//			if (StringUtils.isNotEmpty(password)) {
//				ldapClient.changePassword(newUser.getUid(), password);
//			}
//			return createSuccessStatus().put("user", newUser.toJSON());
//		} catch (final LDAPSDKException ex) {
//			return createFailureStatus(ex);
//		}
		return null;
	}
	
	@Path("searchGroups")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject searchGroups(@QueryParam("search")String search) {
		try {
			final Set<String> groups = ldapClient.searchGroupNames(search);
			return createSuccessStatus().put("groups", new JSONArray(groups));
		} catch (final LDAPException ex) {
			return createFailureStatus(ex);
		}
	}
	
	
	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject update(@FormParam("group") JSONObject groupJson)   { 
//		final User user = User.fromJSON(userJson, dnPattern);
//		try { 
//			final User updatedUser = ldapClient.updateUser(user);
//			final String password = userJson.optString("password");
//			if (StringUtils.isNotEmpty(password)) {
//				ldapClient.changePassword(updatedUser.getUid(), password);
//			}
//			return createSuccessStatus().put("user", updatedUser.toJSON());
//		} catch (LDAPSDKException ex) {
//			return createFailureStatus(ex);
//		}
		return null;
	}
}
