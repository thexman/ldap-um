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
package com.a9ski.um.ws;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.a9ski.um.ldap.LdapClient;
import com.a9ski.um.ldap.exceptions.LdapCustomException;
import com.a9ski.um.model.User;
import com.a9ski.um.security.PasswordPolicyChecker;
import com.a9ski.um.utils.PasswordUtils;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPSDKException;

@Path("/users")
public class UsersWebService extends AbstractWebService {

	private final String userDnPattern;
	
	private final PasswordPolicyChecker passwordPolicyChecker;
	
	
	public UsersWebService() {
		super();
		
		
		final String userBaseDn = configProvider.getUserBaseDn();
		
		try {
			@SuppressWarnings("unchecked")
			final Class<? extends PasswordPolicyChecker> c = (Class<? extends PasswordPolicyChecker>) ClassUtils.getClass(configProvider.getPasswordPolicyCheckerClass());
			passwordPolicyChecker = ConstructorUtils.invokeConstructor(c, configProvider.getPasswordPolicyCheckerParams());
		} catch (final ReflectiveOperationException ex) {
			throw new RuntimeException("Cannot initialize password policy checker", ex);
		}
		
		userDnPattern = StringUtils.replace(StringUtils.replace(configProvider.getUserDnPattern(), "<user-base-dn>", userBaseDn), "<user-id>", "%s");
	}
	
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"UmAdmin"})
	public JSONArray listAllUsers() throws LDAPException, LdapCustomException {
		final JSONArray j = new JSONArray();
		for(final User u : ldapClient.listAllUsers()) {
			j.put(u.toJSON());
		}
		return j;
	}
	
	@Path("rows")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"UmAdmin"})
	public JSONObject listAllUserRows() throws LDAPException, JSONException, LdapCustomException {		
		return new JSONObject().put("rows", listAllUsers());
	}
	
	@Path("create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"UmAdmin"})
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
	@RolesAllowed({"UmAdmin"})
	public JSONObject update(@FormParam("user") JSONObject userJson)   { 
		final User user = User.fromJSON(userJson, userDnPattern);
		try { 
			final User updatedUser = ldapClient.updateUser(user);
			final String password = userJson.optString("password");
			if (StringUtils.isNotEmpty(password)) {				
				changePassword(updatedUser, password);
			}
			return createSuccessStatus().put("user", updatedUser.toJSON());
		} catch (LDAPSDKException | UnsupportedEncodingException | NoSuchAlgorithmException ex) {
			return createFailureStatus(ex);
		}
	}
	
	@Path("password")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"UmAdmin", "UmUser"})
	public JSONObject setPassword(@FormParam("newPassword")String newPassword, @FormParam("oldPassword")String oldPassword) {
		try {
			final User user = getCurrentUser();
			if (user != null) {
				if (ldapClient.checkPassword(String.format(userDnPattern, LdapClient.escapeDnLiteral(user.getUid())), oldPassword)) {
					return changePassword(user, newPassword);
				} else {
					return createFailureStatus("Old password doesn't match");
				}
			} else {
				return createFailureStatus("User is not logged");
			}
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}

	private JSONObject changePassword(final User user, String newPassword) throws LDAPSDKException, UnsupportedEncodingException, NoSuchAlgorithmException {
		if (passwordPolicyChecker.checkPassword(user.toJSON().toString(), newPassword)) {
			ldapClient.changePassword(user.getUid(), PasswordUtils.encryptPassword(newPassword));
			return createSuccessStatus();
		} else {
			return createFailureStatus("The password does not meet the password policy requirements.");
		}
	}
	
	@Path("searchUsers")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"UmAdmin"})
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
	@RolesAllowed({"UmAdmin", "UmUser"})
	public JSONObject currentUsers() throws LDAPSDKException {
		final User u = getCurrentUser();
		if (u != null) {
			return createSuccessStatus().put("currentUser", u.toJSON());
		} else {
			return createFailureStatus("Cannot determine current user");
		}
	}
	
	@Path("isAdmin")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"UmAdmin", "UmUser"})
	public JSONObject isAdmin() {
		return createSuccessStatus().put("isAdmin", securityContext.isUserInRole("UmAdmin"));
	}
	
	@Path("logout")
	@GET
	@Produces(MediaType.TEXT_HTML)
	@PermitAll
	public Response logout() throws URISyntaxException {
		final URI target = uri.getBaseUriBuilder().path("..").build();
		getSession().invalidate();
		return Response.seeOther(target).build();
	}
	
}
